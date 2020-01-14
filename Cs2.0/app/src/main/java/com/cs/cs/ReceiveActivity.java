package com.cs.cs;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveActivity extends AppCompatActivity {
    public static  String savePath = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
// 服务器端用于监听Socket的线程
        Thread listener = new Thread(new Runnable() {
            @Override
            public void run() {

                int port = 9999;
                while (true) {
                    try {
                        ServerSocket server = new ServerSocket(port);
                        Log.i("TAG","接收中");
                        if (server != null) {
                            receiveFile(server);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        });
        listener.start();

        Button return_share =(Button)findViewById(R.id.return_share);
        //1.返回
        return_share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                WifiApAdmin.closeWifiAp(ReceiveActivity.this);
                finish();
            }
        });

    }
    private void receiveFile(final ServerSocket server){

        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int fileState = CsFile.ReceiveFile(server);
                if (fileState==1){
                    Message msg = msgHandler.obtainMessage();
                    msgHandler.sendMessage(msg);
                    Log.i("TAG","接收路径："+savePath);
                }
            }
        });
        sendThread.start();
    }


    private final Handler msgHandler = new Handler(){
        public void handleMessage(Message msg) {
            Toast.makeText(ReceiveActivity.this,"接收成功",Toast.LENGTH_SHORT).show();
        }
    };
}
