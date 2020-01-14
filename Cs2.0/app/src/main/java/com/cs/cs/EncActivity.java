package com.cs.cs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.Random;


import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


public class EncActivity extends AppCompatActivity {
    private int imageNum = 1;
    private int shareNum = 0;
    private int reimageNum = 0;
    public static boolean upServer = false;
    private int news = 0;
    private Context mContext = null;
    public static long time;
    private Bitmap bitmap1;
    private Bitmap bitmap2;
    private Bitmap bitmap3;
    private Bitmap bitmap4;
    private AlertDialog alertDialog;
    static {        // 加载动态库
        System.loadLibrary("ImgCs");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enc);

        mContext = EncActivity.this;
        news = 0;
        Button return_main =(Button)findViewById(R.id.return_main_dec);
        //1.返回
        return_main.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        final Button share =(Button)findViewById(R.id.share);
        //1.分享
        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(shareNum == 1){

                    Date currentdate = new Date();
                    time = currentdate.getTime();
                    final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encrypted.png";
                    final String fileName = "encrypted"+time+".png";
                    final String ipAddress = "139.199.198.140";
                    final int port = 9999;
                    Thread sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            news = CsFile.SendFile(fileName, path, ipAddress, port);
                            if(news == 1){
                                upServer=true;
                                Intent intent2=new Intent(EncActivity.this,ShareActivity.class);
                                startActivity(intent2);
                            }
                        }
                    });

                    sendThread.start();
                    while(true){
                        if (news==1){
                            break;
                        }
                        if(news == 2){
                            upServer=false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(EncActivity.this);
                            builder.setTitle("上传服务器失败,是否选择离线分享？");
                            builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.cancel();
                                }
                            });
                            builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent2=new Intent(EncActivity.this,ShareActivity.class);
                                    startActivity(intent2);
                                }
                            });
                            alertDialog = builder.create();
                            alertDialog.show();
                            break;
                        }

                    }

                }else{
                    upServer=false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(EncActivity.this);
                    builder.setTitle("无加密,是否选择离线分享？");
                    builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.cancel();
                        }
                    });
                    builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent2=new Intent(EncActivity.this,ShareActivity.class);
                            startActivity(intent2);
                        }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                }

            }


        });

        Button open_image =(Button)findViewById(R.id.open_image);
        //2.打开图片
        open_image.setOnClickListener(new View.OnClickListener() {

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

        Button start_enc = (Button)findViewById(R.id.start_enc);

        start_enc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageNum == 4){

                    Bitmap merge_bitmap =  mergeThumbnailBitmap(bitmap1,bitmap2,bitmap3,bitmap4);
                    saveBmp(merge_bitmap);
                    ImageView imageView1 = (ImageView) findViewById(R.id.imageView1);
                    ImageView imageView2 = (ImageView) findViewById(R.id.imageView2);
                    ImageView imageView3 = (ImageView) findViewById(R.id.imageView3);
                    ImageView imageView4 = (ImageView) findViewById(R.id.imageView4);
                    ImageView imageView5 = (ImageView) findViewById(R.id.imageView5);
                    /* 将Bitmap设定到ImageView */
                    imageView1.setImageDrawable(null);
                    imageView2.setImageDrawable(null);
                    imageView3.setImageDrawable(null);
                    imageView4.setImageDrawable(null);
                    imageView5.setImageBitmap(merge_bitmap);

                    File key00 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/key00.dat");
                    if(key00.exists()){
                        key00.delete();
                    }
                    File key01 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/key01.dat");
                    if(key01.exists()){
                        key01.delete();
                    }
                    File key10 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/key10.dat");
                    if(key10.exists()){
                        key10.delete();
                    }
                    File key11 = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/key11.dat");
                    if(key11.exists()){
                        key11.delete();
                    }
                    File encryp_imagedata = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encryp_imagedata.dat");
                    if(encryp_imagedata.exists()){
                        encryp_imagedata.delete();
                    }
                    File encrypted = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encrypted.png");
                    if(encrypted.exists()){
                        encrypted.delete();
                    }
                    ImgCs ImgCsJNI = new ImgCs();
                    int inti = ImgCsJNI.csencryp(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cs/target.bmp");
                    Log.i("","+++"+inti);

                    boolean flag = true;
                    while(flag){
                        if(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encryp_imagedata.dat").exists()){
                            try {
                                int data[];
                                data = readF2(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encryp_imagedata.dat");
                                Bitmap bitmap = cryptographto(data);

                                savePng(bitmap);
                            }catch (IOException e){
                                e.printStackTrace();
                            }
                            ImageView imageView6 = (ImageView) findViewById(R.id.imageView5);
                            Bitmap bitmap = DecActivity.getLoacalBitmap(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encrypted.png"); //从本地取图片(在cdcard中获取)  //
                            imageView6.setImageBitmap(bitmap);
                            shareNum = 1;
                            flag = false;
                        }
                    }
               }
            }
        });

        ImageView image1 = (ImageView)findViewById(R.id.imageView1);

        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageNum>=1){
                    reimageNum=1;
                    Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                    intent.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                /* 取得相片后返回本画面 */
                    startActivityForResult(intent, 1);
                }
            }
        });

        ImageView image2 = (ImageView)findViewById(R.id.imageView2);

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageNum>=2){
                    reimageNum=2;
                    Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                    intent.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                /* 取得相片后返回本画面 */
                    startActivityForResult(intent, 1);
                }
            }
        });
        ImageView image3= (ImageView)findViewById(R.id.imageView3);

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageNum>=3){
                    reimageNum=3;
                    Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                    intent.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                /* 取得相片后返回本画面 */
                    startActivityForResult(intent, 1);
                }
            }
        });
        ImageView image4 = (ImageView)findViewById(R.id.imageView4);

        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageNum>=4){
                    reimageNum=4;
                    Intent intent = new Intent();
                /* 开启Pictures画面Type设定为image */
                    intent.setType("image/*");
                /* 使用Intent.ACTION_GET_CONTENT这个Action */
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                /* 取得相片后返回本画面 */
                    startActivityForResult(intent, 1);
                }
            }
        });
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                if(reimageNum==0){
                    if (imageNum == 1){
                        bitmap1=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView1);

                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);
                    }
                    if (imageNum == 2){
                        bitmap2=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView2);
                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);

                    }
                    if (imageNum == 3){
                        bitmap3=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView3);
                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);

                    }
                    if (imageNum == 4){
                        bitmap4=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView4);
                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);
                    }
                    else{
                            imageNum +=1;
                    }
                }
                else{
                    if (reimageNum == 1){
                        bitmap1=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView1);

                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);
                    }
                    if (reimageNum == 2){
                        bitmap2=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView2);
                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);

                    }
                    if (reimageNum == 3){
                        bitmap3=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView3);
                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);

                    }
                    if (reimageNum == 4){
                        bitmap4=bitmap;
                        ImageView imageView = (ImageView) findViewById(R.id.imageView4);
                /* 将Bitmap设定到ImageView */
                        imageView.setImageBitmap(bitmap);
                    }
                    reimageNum =0;
                }

            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private Bitmap getBitmapFromStream(InputStream inputStream) {
        return BitmapFactory.decodeStream(inputStream);
    }

    //合并图片
    private Bitmap mergeThumbnailBitmap(Bitmap Bitmap1, Bitmap Bitmap2, Bitmap Bitmap3, Bitmap Bitmap4) {

        Bitmap bitmap = Bitmap.createBitmap(Bitmap1.getWidth()*2, Bitmap1.getHeight()*2, Bitmap1.getConfig());   // 创建一个 512的图
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        canvas.drawBitmap(Bitmap1, 0,0, null);  //第一张图,在大图的x坐标，用坐标，paint
        canvas.drawBitmap(Bitmap2, Bitmap1.getWidth(), 0, null);
        canvas.drawBitmap(Bitmap3, 0, Bitmap1.getHeight(), null);
        canvas.drawBitmap(Bitmap4, Bitmap1.getWidth(), Bitmap1.getHeight(),null);
        return bitmap;
    }
    /**
     * 将Bitmap存为 .png格式图片
     *
     */
    private void savePng(Bitmap bitmap) throws IOException
    
    {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/encrypted.png");
        if(file.exists()){
            file.delete();
        }
        FileOutputStream out;
        try{
            out = new FileOutputStream(file);
            if(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out))
            {
                out.flush();
                out.close();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 将Bitmap存为 .bmp格式图片
     * @param bitmap
     */
    private void saveBmp(Bitmap bitmap) {
        if (bitmap == null)
            return;
        // 位图大小
        int nBmpWidth = bitmap.getWidth();
        int nBmpHeight = bitmap.getHeight();
        // 图像数据大小
        int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);
        try {
            // 存储文件名
            String filename = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Cs/target.bmp";
            File file = new File(filename);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                }catch (IOException ex){
                    System.out.println(ex);
                }

            }
            FileOutputStream fileos = new FileOutputStream(filename);
            // bmp文件头
            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;
            // 保存bmp文件头
            writeWord(fileos, bfType);
            writeDword(fileos, bfSize);
            writeWord(fileos, bfReserved1);
            writeWord(fileos, bfReserved2);
            writeDword(fileos, bfOffBits);
            // bmp信息头
            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;
            // 保存bmp信息头
            writeDword(fileos, biSize);
            writeLong(fileos, biWidth);
            writeLong(fileos, biHeight);
            writeWord(fileos, biPlanes);
            writeWord(fileos, biBitCount);
            writeDword(fileos, biCompression);
            writeDword(fileos, biSizeImage);
            writeLong(fileos, biXpelsPerMeter);
            writeLong(fileos, biYPelsPerMeter);
            writeDword(fileos, biClrUsed);
            writeDword(fileos, biClrImportant);
            // 像素扫描
            byte bmpData[] = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }

            fileos.write(bmpData);
            fileos.flush();
            fileos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeWord(FileOutputStream stream, int value) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        stream.write(b);
    }

    protected void writeDword(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    protected void writeLong(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    //逐行读取txt,返回int数组
    private static final int[] readF2(String filePath) throws IOException {
        String[] data = new String[1024];
        int[] num = new int[1024];
        File readFile = new File(filePath);
        //输入IO流声明
        InputStream in = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            //用流读取文件
            in = new BufferedInputStream(new FileInputStream(readFile));
            //如果你文件已utf-8编码的就按这个编码来读取，不然又中文会读取到乱码
            ir = new InputStreamReader(in, "utf-8");
            //字符输入流中读取文本,这样可以一行一行读取
            br = new BufferedReader(ir);
            String line = "";
            int i = 0;
            //一行一行读取
            while ((line = br.readLine()) != null) {
                data[i] = line;
                i++;
            }
            for (int j = 0;j<data.length;j++){
                num[j] =Integer.parseInt(data[j]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //一定要关闭流,倒序关闭
            try {
                if (br != null) {
                    br.close();
                }
                if (ir != null) {
                    ir.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
            }
        }
        return num;
    }
    //将数组数据处理，转换成三通bitmap
    private Bitmap cryptographto(int[] data){
        int[] rgbData = new int[1024*12];
        Random rand = new Random();
        for(int i=0;i<data.length;i++){
            int j =0;
            int b = data[i];
            if(data[i]<0){
                b = -b;
            }
            while(b!=0){
                rgbData[j+i*12] = b%254;
                b -=rgbData[j+i*12];
                b /= 254;
                j++;
            }
            rgbData[j+i*12] = 255; //数组以255为隔离

            for (int k = j+i*12+1;k<10+i*12;k++){
                rgbData[k] = rand.nextInt(250)+1;
            }
            if (data[i]>0){
                rgbData[10+i*12] = rand.nextInt(150)+101; //正数在100-150闭区间
            }else{
                rgbData[10+i*12] = rand.nextInt(99)+1; //带负号的在1-99闭区间
            }
            rgbData[11+i*12] = 0;
        }

        Bitmap bitmap = Bitmap.createBitmap(64,64, Bitmap.Config.ARGB_8888);
        int mBitmapWeight = bitmap.getWidth();
        int mBitmapHeight = bitmap.getHeight();
        int k = 0;
        for (int i = 0; i < mBitmapHeight; i++) {
            for (int j = 0; j < mBitmapWeight; ) {
                bitmap.setPixel(i, j, Color.rgb(rgbData[k], rgbData[k+1], rgbData[k+2]));
                bitmap.setPixel(i, j+1, Color.rgb(rgbData[k+3], rgbData[k+4], rgbData[k+5]));
                bitmap.setPixel(i, j+2, Color.rgb(rgbData[k+6], rgbData[k+7], rgbData[k+8]));
                bitmap.setPixel(i, j+3, Color.rgb(rgbData[k+9], rgbData[k+10], rgbData[k+11]));
                k+=12;
                j+=4;
            }
        }
        return bitmap;
    }
}
