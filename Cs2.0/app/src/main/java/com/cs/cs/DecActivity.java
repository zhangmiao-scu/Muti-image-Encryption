package com.cs.cs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.cs.cs.zxing.activity.CaptureActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class DecActivity extends AppCompatActivity {
    private int chooseNum = 0;
    private String keyPath = null;
    private String imgPath = null;
    private Context mContext = null;
    static {        // 加载动态库
        System.loadLibrary("ImgCs");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dec);
        mContext = DecActivity.this;

        if(CaptureActivity.DOWNLOAD_NUM == 1){
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println();
            setContentView(R.layout.activity_dec);
            imgPath = CaptureActivity.downPath;
            ImageView image1 = (ImageView) findViewById(R.id.dec_image);  //获得ImageView对象

            Bitmap bitmap = getLoacalBitmap(imgPath); //从本地取图片(在cdcard中获取)  //
            image1 .setImageBitmap(bitmap); //设置Bitmap
            CaptureActivity.DOWNLOAD_NUM = 0;

        }


        Button return_main_dec =(Button)findViewById(R.id.return_main_dec);
        //1.返回
        return_main_dec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        Button enc_image =(Button)findViewById(R.id.enc_image);
        //2.打开图片
        enc_image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                chooseNum = 1;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
        //3.扫描二维码
        Button camera = (Button)findViewById(R.id.qrcode);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(DecActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DecActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                } else {
                    Intent intent = new Intent(DecActivity.this, CaptureActivity.class);
                    startActivity(intent);
                }

            }
        });
        //4.选择密钥文件
        final Button key = (Button)findViewById(R.id.key);
        key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                chooseNum = 2;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
        Button start_dec = (Button)findViewById(R.id.Start_dec);
        start_dec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File DECRYPTION = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/DECRYPTION.bmp");
                if(DECRYPTION.exists()){
                    DECRYPTION.delete();
                }

                if (keyPath == null){
                    Toast.makeText(mContext,"请选择密钥文件",Toast.LENGTH_SHORT).show();
                }
                else if (imgPath == null){
                    Toast.makeText(mContext,"请选择加密图像",Toast.LENGTH_SHORT).show();
                }
                else{
                    Bitmap encrypted_bitmap = getLoacalBitmap(imgPath);
                    int[] img_data;
                    img_data = readImg(encrypted_bitmap);
                    try {
                        File encryp_imagedata = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encryp_imagedata.dat");
                        if(encryp_imagedata.exists()){
                            encryp_imagedata.delete();
                        }
                        encryp_imagedata.createNewFile();
                        FileWriter encryp_writer = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encryp_imagedata.dat", true);
                        for(int i =0;i<1024;i++){
                            encryp_writer.write(img_data[i]+"\n");
                        }
                        encryp_writer.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    ImgCs ImgCsJNI = new ImgCs();
                    ImgCsJNI.csdecryp(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encryp_imagedata.dat",keyPath);

                    boolean flag = true;
                    while(flag){
                        if(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/DECRYPTION.bmp").exists()){
                            ImageView imageView= (ImageView) findViewById(R.id.dec_image);
                            Bitmap bitmap = getLoacalBitmap(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/DECRYPTION.bmp"); //从本地取图片(在cdcard中获取)  //
                            imageView.setImageBitmap(bitmap);
                            flag = false;
                        }
                    }
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if(chooseNum == 1){
                ContentResolver cr = this.getContentResolver();
                try {
                    imgPath =  RealPathFromUriUtils.getPath(this, uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                    ImageView imageView = (ImageView) findViewById(R.id.dec_image);
                /* 将Bitmap设定到ImageView */
                    imageView.setImageBitmap(bitmap);
                    imageView.setImageURI(uri);

                } catch (FileNotFoundException e) {
                    Log.e("Exception", e.getMessage(),e);
                }
            }
            else if(chooseNum == 2){
                keyPath = RealPathFromUriUtils.getPath(this,uri);

            }


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    //读取图片成数组
    private int[] readImg(Bitmap bitmap){
        int[] data = new int[1024];
        int[] rgbData = new int[1024*12];
        int v=0;
        for(int x=0 ; x < 64 ;x++){
            for (int y = 0;y<64;){
                int color1 =  bitmap.getPixel(x,y);
                int color2 =  bitmap.getPixel(x,y+1);
                int color3 =  bitmap.getPixel(x,y+2);
                int color4 =  bitmap.getPixel(x,y+3);
                rgbData[v] = Color.red(color1);
                rgbData[v+1] = Color.green(color1);
                rgbData[v+2] = Color.blue(color1);
                rgbData[v+3] = Color.red(color2);
                rgbData[v+4] = Color.green(color2);
                rgbData[v+5] = Color.blue(color2);
                rgbData[v+6] = Color.red(color3);
                rgbData[v+7] = Color.green(color3);
                rgbData[v+8] = Color.blue(color3);
                rgbData[v+9] = Color.red(color4);
                rgbData[v+10] = Color.green(color4);
                rgbData[v+11] = Color.blue(color4);
                v+=12;
                y+=4;
            }
        }
        for(int i = 0;i<1024;i++){
            for(int j =0;j<12;j++){
                if(rgbData[j+i*12] == 255){
                    int dataNum = 0;
                    for (int k=j+i*12-1;k>=i*12;k--){
                        dataNum += rgbData[k];
                        if (k!=i*12){
                            dataNum*=254;
                        }
                    }
                    data[i] = dataNum;
                    if (rgbData[i*12+10] <100){
                        data[i] = -data[i];
                    }
                }
            }
        }

        return data;
    }
}
