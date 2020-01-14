#include <iostream>
#include <opencv2/core.hpp>
#include <opencv2/opencv.hpp>
#include <cmath>
#include <fstream>

#include "com_cs_cs_ImgCs.h"

#include "jni.h"

#define SAMPNUM 1024

using namespace cv;
using namespace std;

Mat hadamardEncrypt(int N)
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

const char* jstringTostringEncrypt(JNIEnv* env, jstring jstr)
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

JNIEXPORT jint JNICALL Java_com_cs_cs_ImgCs_csencryp
  (JNIEnv * env, jobject, jstring j_file_path) {
    const char* t_filepath = jstringTostringEncrypt(env, j_file_path);
    string filepath = t_filepath;
    string root_path = filepath;
    root_path.erase(filepath.rfind('/')+1,filepath.length()); // 获得根目录

    Mat hadabase = hadamardEncrypt(SAMPNUM); // 生成Hadamard矩阵

    Mat imagedata(64,64,CV_32FC1);
    Mat imagedata_temp = imread(filepath,0);
    imagedata_temp.convertTo(imagedata,CV_32FC1); // 读取图片

    Mat encryp_imagedata;
    for (int i = 0; i < 2; ++i) {
        for (int j = 0; j < 2; ++j) {

            Mat subhadamat = hadabase.colRange((i*2+j)*256,(i*2+j+1)*256); // 取Hadamard矩阵按列以1024分割的一份

            std::ofstream sensmatFile(root_path + "key" + char(i + '0') + char(j + '0') + ".dat");
            Mat sensmat(256,1024,CV_8UC1);
            randu(sensmat, Scalar::all(0), Scalar::all(2));
            sensmatFile << sensmat;
            sensmatFile.close(); // 生成sensmat文件

            sensmat.convertTo(sensmat,CV_32FC1); // 转换为浮点，方便矩阵相乘

            Mat subimage_temp = imagedata(Range(i*32,(i+1)*32),Range(j*32,(j+1)*32)); // 取图片中的其中一张
            Mat subimage(32,32,CV_32FC1);
            subimage_temp.copyTo(subimage); //  因为直接读取内存不连续，重新申请内存达到连续效果
            subimage = subimage.reshape(0, 1024); // reshape为256*1的矩阵
            subimage.convertTo(subimage,CV_32FC1); // 转换类型，方便相乘

            Mat transsubimage;
            transsubimage = sensmat * subimage;
            encryp_imagedata = encryp_imagedata + subhadamat * transsubimage;
        }
    }
    std::ofstream cryptographtoFile(root_path + "encryp_imagedata.dat");
    for (int k = 0; k < 1024; ++k) {
        cryptographtoFile << int(encryp_imagedata.at<float> (k,0)) << '\n';
    }
    cryptographtoFile.close();
    return 1;
}