package com.cs.cs;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by jon_son_ on 2018/4/3.
 */
public class CsFile {

    //发送文件
    public static int SendFile(String fileName, String path, String ipAddress, int port) {
        try {
            Socket name = new Socket(ipAddress, port);
            OutputStream outputName = name.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
            BufferedWriter bwName = new BufferedWriter(outputWriter);
            bwName.write(fileName);
            bwName.close();
            outputWriter.close();
            outputName.close();
            name.close();

            Socket data = new Socket(ipAddress, port);
            OutputStream outputData = data.getOutputStream();
            FileInputStream fileInput = new FileInputStream(path);
            int size = -1;
            byte[] buffer = new byte[10240000];
            while ((size = fileInput.read(buffer, 0, 10240000)) != -1) {
                outputData.write(buffer, 0, size);
            }
            outputData.close();
            fileInput.close();
            data.close();
            return 1;
        } catch (Exception e) {
            return 2;
        }
    }
    // 文件接收方法
    public static int ReceiveFile(ServerSocket server) {
        try {
            // 接收文件名
            Socket name = server.accept();
            InputStream nameStream = name.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            name.close();

            // 接收文件数据
            Socket data = server.accept();
            InputStream dataStream = data.getInputStream();

            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs"); // 创建文件的存储路径
            if (!dir.exists()) {
                dir.mkdirs();
            }
            ReceiveActivity.savePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/" + fileName; // 定义完整的存储路径
            FileOutputStream file = new FileOutputStream(ReceiveActivity.savePath, false);
            byte[] buffer = new byte[10240000];
            int size = -1;
            while ((size = dataStream.read(buffer)) != -1) {
                file.write(buffer, 0, size);
            }
            file.close();
            dataStream.close();
            data.close();


            return 1;
        } catch (Exception e) {
            return 2;
        }
    }
}
