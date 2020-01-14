package com.cs.cs;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.FileNotFoundException;
import java.net.ServerSocket;

public class SendActivity extends AppCompatActivity {
    private Uri uri = null;
    private String filePath = null;
    private Context mContext = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_send);
        Button return_share =(Button)findViewById(R.id.return_send);
        //1.返回
        return_share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                //调用WifiManager的setWifiEnabled方法设置wifi的打开或者关闭，只需把下面的state改为布尔值即可（true:打开 false:关闭）
                mWifiManager.setWifiEnabled(false);
                finish();
            }
        });

        Button choose_share = (Button)findViewById(R.id.choose_image);

        //2.选择传输图片
        choose_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), 0);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(mContext,"Please install a File Manager.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //3.选择传输密钥文件
        Button key = (Button)findViewById(R.id.choose_key);
        key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult( Intent.createChooser(intent, "Select a File to Upload"), 0);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(mContext,"Please install a File Manager.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button start_share = (Button)findViewById(R.id.start_send);
        //4.开始传输
        start_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //android热点主机ip为192.168.43.1
                final String path = filePath;
                final String fileName = filePath.split("/")[path.split("/").length-1];
                final String ipAddress = "192.168.43.1";
                final int port = 9999;

                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int fileState =  CsFile.SendFile(fileName, path, ipAddress, port);
                        if (fileState==1){
                            Message msg = msgHandler.obtainMessage();
                            msgHandler.sendMessage(msg);
                        }
                    }
                });
                sendThread.start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            uri = data.getData();
            filePath = RealPathFromUriUtils.getPath(this, uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final Handler msgHandler = new Handler(){
        public void handleMessage(Message msg) {
            Toast.makeText(SendActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
        }
    };

}
