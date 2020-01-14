package com.cs.cs;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File Cs_dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/");
        if(!Cs_dir.exists()){
            Cs_dir.mkdir();
        }
        if(!new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/wavemat.dat").exists()){
            try{
                copyBigDataToSD(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/wavemat.dat");
            }catch (IOException e){
                e.printStackTrace();
            }
        }


        Button enc =(Button)findViewById(R.id.encrypt);
        //1.匿名内部类
        enc.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent1=new Intent(MainActivity.this,EncActivity.class);
                startActivity(intent1);
            }
        });
        Button dec =(Button)findViewById(R.id.decode);
        //1.匿名内部类
        dec.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent2=new Intent(MainActivity.this,DecActivity.class);
                startActivity(intent2);
            }
        });
    }

    //将wavemat.dat复制进Cs文件夹
    private void copyBigDataToSD(String strOutFileName) throws IOException
    {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = this.getAssets().open("wavemat.dat");
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while(length > 0)
        {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

}
