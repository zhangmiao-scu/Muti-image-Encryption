package com.cs.cs;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ShareActivity extends AppCompatActivity {
    private WifiAdmin mWifiAdmin;
    private Context mContext = null;
    public static final String TAG = "ShareActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        mContext = this;
        ImageView imageView = (ImageView) findViewById(R.id.imageView_share);
        if(EncActivity.upServer){
                /* 将Bitmap设定到ImageView */
            imageView.setImageBitmap(encodeAsBitmap("http://www.szpc-society.cn/img/encrypted"+EncActivity.time+".png"));
        }else{
            imageView.setImageDrawable(null);
        }


        Button return_enc =(Button)findViewById(R.id.return_share);
        //1.返回
        return_enc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });


        Button builde =(Button)findViewById(R.id.builde);
        //2.建立WIFI
        builde.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                try {
                    WifiApAdmin wifiAp = new WifiApAdmin(mContext);
                    wifiAp.startWifiAp("Cs", "12345678");
                    Intent intent2=new Intent(ShareActivity.this,ReceiveActivity.class);
                    startActivity(intent2);
                }catch (Exception e){

                }

            }
        });
        Button content = (Button)findViewById(R.id.content);
        //3.连接WIFI
        content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentWifi contentWifi = new ContentWifi("Cs","12345678");
                        contentWifi.start();
                        Intent intent3=new Intent(ShareActivity.this,SendActivity.class);
                        startActivity(intent3);
            }
        });

}
    //连接WIFI异步线程
    protected class ContentWifi extends Thread{
        private String SSID;
        private String Password;
        public ContentWifi(String SSID,String Password){
            this.SSID = SSID;
            this.Password = Password;
        }
        @Override
        public void run(){
            mWifiAdmin = new WifiAdmin(mContext) {

                @Override
                public void myUnregisterReceiver(BroadcastReceiver receiver) {
                    // TODO Auto-generated method stub
                    ShareActivity.this.unregisterReceiver(receiver);
                }

                @Override
                public Intent myRegisterReceiver(BroadcastReceiver receiver,
                                                 IntentFilter filter) {
                    // TODO Auto-generated method stub
                    ShareActivity.this.registerReceiver(receiver, filter);
                    return null;
                }

                @Override
                public void onNotifyWifiConnected() {
                    // TODO Auto-generated method stub
                    Message msg = msgHandler.obtainMessage();
                    msgHandler.sendMessage(msg);
                    Log.v(TAG, "have connected success!");
                    Log.v(TAG, "###############################");
                }
                @Override
                public void onNotifyWifiConnectFailed() {
                    // TODO Auto-generated method stub
                    Log.v(TAG, "have connected failed!");
                    Log.v(TAG, "###############################");
                }
            };
            mWifiAdmin.openWifi();
            mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(SSID, Password, WifiAdmin.TYPE_WPA));
        }
    }

    //生成二维码
    Bitmap encodeAsBitmap(String str){
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 200, 200);
            // 使用 ZXing Android Embedded
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e){
            e.printStackTrace();
        } catch (IllegalArgumentException iae){ // ?
            return null;
        }
        return bitmap;
    }

    private final Handler msgHandler = new Handler(){
        public void handleMessage(Message msg) {
            Toast.makeText(ShareActivity.this,"连接WIFI成功",Toast.LENGTH_SHORT).show();
        }
    };

}
