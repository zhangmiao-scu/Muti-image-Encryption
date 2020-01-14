#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>
#include <iomanip>
#include <fstream>

#include "com_cs_cs_ImgCs.h"

#include "jni.h"

#define SAMPNUM 1024

using namespace cv;
using namespace std;

Mat hadamardDecrypt(int N)
{
    int ii, xx, yy;
    Mat hada(N,N,CV_32SC1);


    char **h = (char**) calloc(N, sizeof(char*));

    for (int i = 0; i < N; i++ ) {
        h[i] = (char*) calloc(N, sizeof(char));
    }

    h[0][0]= 1;

    for(ii=2; ii<=N; ii*=2) {
        //Top right quadrant.
        for(xx=0; xx<(ii/2); ++xx) {
            for(yy=(ii/2); yy<ii; ++yy){
                h[xx][yy]=h[xx][yy-(ii/2)];
            }
        }
        //Bottom left quadrant.
        for(yy=0; yy<(ii/2); ++yy) {
            for(xx=(ii/2); xx<ii; ++xx) {
                h[xx][yy]=h[xx-(ii/2)][yy];
            }
        }
        //Bottom right quadrant, inverse of other quadrants.
        for(xx=(ii/2); xx<ii; ++xx) {
            for(yy=(ii/2); yy<ii; ++yy) {
                h[xx][yy]=h[xx-(ii/2)][yy-(ii/2)];
                if(h[xx][yy]== 1) {
                    h[xx][yy]= -1;
                }
                else {
                    h[xx][yy]= 1;
                }
            }
        }
    }

    for(xx=0; xx<N; ++xx) {
        for(yy=0; yy<N; ++yy) {
            hada.at<int>(xx,yy) = h[xx][yy];
        }
    }

    hada.convertTo(hada,CV_32FC1);
    return hada;
}

Mat OMP(Mat reconsmat,Mat y,int L){
    Mat residual = y; // 暂存y
    int M = y.rows; // M为y的行数
    int indexarr[L]; // int数组，L长度
    Mat tempatocoll;
    Mat estix(81,1,CV_64FC1);
    Mat tranreconsmat ;
    transpose(reconsmat,tranreconsmat);
    tranreconsmat.convertTo(tranreconsmat,CV_64FC1);
    residual.convertTo(residual,CV_64FC1);
    for (int i = 0; i < L-1; i++) {
        Mat product = abs(tranreconsmat * residual);
        Point max;
        minMaxLoc(product.reshape(0,1), NULL, NULL, NULL, &max);
        int maxindex = max.x;
        indexarr[i] = maxindex;
        if(i!=0){
            hconcat(tempatocoll,tranreconsmat.row(maxindex), tempatocoll);
        }else{
            tempatocoll = tranreconsmat.row(maxindex);
        }
        Mat tempatocoll32;
        tempatocoll.convertTo(tempatocoll32,CV_64FC1);
        Mat atocall = Mat::zeros(M,(tempatocoll32.cols * tempatocoll32.rows)/M,CV_32FC1);
        transpose(tempatocoll32,tempatocoll32);
        tempatocoll32 = tempatocoll32.reshape(0,1);
        tempatocoll32.convertTo(tempatocoll32,CV_32FC1);
        tempatocoll = tempatocoll32;
        int index = 0;
        for (int i = 0; i < atocall.cols; ++i) {
            for (int j = 0; j < atocall.rows; ++j) {
                atocall.at<float>(j,i) = tempatocoll.at<float>(0,index);
                index++;
            }
        }
        tempatocoll.convertTo(tempatocoll,CV_64FC1);
        atocall.convertTo(atocall,CV_64FC1);
        reconsmat.col(maxindex) = Mat::zeros(M,1,CV_64FC1);
        y.convertTo(y,CV_64FC1);
        Mat inv = atocall.inv(DECOMP_SVD);
        inv.convertTo(inv,CV_64FC1);
        estix = inv * y;
        residual = y - (atocall * estix);
    }
    Mat x = Mat::zeros(reconsmat.cols,1,CV_64FC1);
    for (int k = 0; k < estix.rows; ++k) {
        x.at<double> (indexarr[k],0) = estix.at<double> (k,0);
    }
    return x;
}

// 读取小波矩阵
Mat genwave2mat(string rootpath){
    ifstream wavefile(rootpath + "wavemat.dat");
    Mat wave(1024,1024,CV_32FC1);
    float t;
    for (int i = 0; i < 1024; ++i) {
        for (int j = 0; j < 1024; ++j) {
            wavefile >> t;
            wave.at<float> (i,j) = t;
        }
    }
    return wave;
}


Mat csrecons(Mat y,Mat sensmat, int L,string rootpath) {

Mat sparmat = genwave2mat(rootpath); // 读取python已经生成的wavemat
sensmat.convertTo(sensmat,CV_32FC1); // 保证类型正确
Mat reconsmat = sensmat * sparmat;
Mat rex = OMP(reconsmat,y,L);
rex.convertTo(rex,CV_32FC1);
Mat imgrecons = sparmat * rex;
imgrecons = imgrecons.reshape(0, 32);
return imgrecons;
}

const char* jstringTostringDecrypt(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

JNIEXPORT jint JNICALL Java_com_cs_cs_ImgCs_csdecryp
  (JNIEnv * env, jobject, jstring j_data_file_path, jstring j_key_file_path) {
    const char* t_data_file_path;
    const char* t_key_file_path;

    t_data_file_path = jstringTostringDecrypt(env, j_data_file_path);
    string data_file_path = t_data_file_path; // 数据文件地址

    t_key_file_path = jstringTostringDecrypt(env, j_key_file_path);
    string key_file_path = t_key_file_path; // 密钥文件地址

    std::string root_path = key_file_path;
    root_path.erase(root_path.rfind('/')+1,root_path.length()); // 获取根目录
    cout << root_path << "root_path";
    string ij = key_file_path.substr(key_file_path.rfind("key")+3,key_file_path.rfind('.')-3);
    int xi = ij[0] - '0' ,xj = ij[1] - '0'; // 确定图片序号
    Mat decryp_imagedata(1024,1,CV_32FC1);
    ifstream cryptographtoFile(data_file_path);
    string temp;
    for (int k = 0; getline(cryptographtoFile,temp); ++k) {
        decryp_imagedata.at<float> (k,0) = atoi(temp.c_str());
    }
    decryp_imagedata.convertTo(decryp_imagedata,CV_32FC1);
    int subsamptime = 256;
    Mat hadabase = hadamardDecrypt(SAMPNUM).colRange(subsamptime * (xi * 2 + xj), subsamptime * (xi * 2 + xj + 1)); // 取Hadamard矩阵按列以1024分割的一份
    transpose(hadabase,hadabase);
    Mat CSreconsimg = Mat::zeros(64,64,CV_32FC1);
    Mat suby = hadabase * decryp_imagedata;
    ifstream sensmatFile(key_file_path);
    Mat sensmat(256,1024,CV_32FC1);
    int j;
    int t2;
    char t1;
    for(int i = 0;i < 256; ++i) {
        j = 0;
        while (j < 1024) {
            sensmatFile >> t1;
            if (t1 != ',' && t1 != '[' &&  t1 != ']' && t1 != ';') {
                t2 = t1 - '0';
                sensmat.at<float> (i,j) = t2;
                j++;
            }
        }
    }
    sensmat.convertTo(sensmat,CV_32FC1);
    Mat imgrecons = csrecons(suby, sensmat, 81, root_path);

    Mat noosimgre = imgrecons;
    Point min, max;
    minMaxLoc(noosimgre,NULL,NULL,&min, &max);
    noosimgre.at<float>(min) = 0;
    Mat tempnooimgre(noosimgre.rows,noosimgre.cols,CV_64FC1);
    imgrecons = noosimgre / noosimgre.at<float>(max) * 255;
    for (int i1 = 0; i1 < imgrecons.rows; ++i1) {
        for (int i2 = 0; i2 < imgrecons.cols; ++i2) {
            imgrecons.at<float>(i1,i2) = cvFloor(imgrecons.at<float>(i1,i2));
        }
    }

    imwrite(root_path + "DECRYPTION.bmp",imgrecons);
    return 1;
}
