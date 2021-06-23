package com.example.downloadingdemov1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private TextView textView;

    private static final int IMAGE_MSG_NEW_PIC = 2;
    //使用网上下载图片
    private static final int IMAGE_MSG_CACHE_PIC = 1;
    //使用缓存图片
    private static final int IMAGE_ERROR = 3;
    //图片请求失败
    private static final int IMAGE_EXCEPTION = 4;
    //图片发生异常，请求失败

    protected static final int TEXT_SUCCESS = 5;
    //文本请求成功
    protected static final int TEXT_ERROR = 6;
    //文本请求失败
    protected static final int FILE_SUCCESS = 7;
    //文件下载完毕

    private Handler handler;

    class DoHandler extends Handler {
        private final WeakReference<Activity> reference;

        public DoHandler(Activity activity) {
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case IMAGE_MSG_CACHE_PIC:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    imageView.setImageBitmap(bitmap);
                    Toast.makeText(reference.get(), "使用缓存图", Toast.LENGTH_SHORT).show();
                    break;
                case IMAGE_MSG_NEW_PIC:
                    Bitmap bitmap2 = (Bitmap) msg.obj;
                    imageView.setImageBitmap(bitmap2);
                    Toast.makeText(reference.get(), "下载图片完毕", Toast.LENGTH_SHORT).show();
                    break;
                case IMAGE_ERROR:
                    Toast.makeText(reference.get(), "图片请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case IMAGE_EXCEPTION:
                    Toast.makeText(reference.get(), "图片发生异常，请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case TEXT_ERROR:
                    Toast.makeText(reference.get(), "文本请求失败", Toast.LENGTH_SHORT).show();
                    break;
                case TEXT_SUCCESS:
                    String text = (String) msg.obj;
                    textView.setText(text);
                    Toast.makeText(reference.get(), "文本请求成功", Toast.LENGTH_SHORT).show();
                    break;
                case FILE_SUCCESS:
                    Toast.makeText(reference.get(), "文件下载完毕", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_downloadImage).setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        findViewById(R.id.btn_downloadFile).setOnClickListener(this);
        findViewById(R.id.btn_downloadText).setOnClickListener(this);
        textView = (TextView) findViewById(R.id.textView);
        handler = new DoHandler(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_downloadImage:
                downloadBitmap();
                //下图片
                break;
            case R.id.btn_downloadFile:
                downloadFile();
                //下文件apk
                break;
            case R.id.btn_downloadText:
                downloadText();
                //下载html网页
                break;
        }
    }


    /**
     * 下文件
     */
    private void downloadFile() {
        final String urlStr = "https://downloads.jianshu.io/apps/haruki/JianShu-2.2.3-17040111.apk";
        String state = Environment.getExternalStorageState();
        //MEDIA_MOUNTED SD卡正常挂载,因此可以读取或者向SD卡写入
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "无法获取SDCard", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                //String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                ///storage/emulated/0/Download,sd卡的download目录
                String filename = urlStr.substring(urlStr.lastIndexOf("/"));
                File file = new File(getExternalFilesDir(null), filename);
                //创建文件路径（/storage/emulated/0/Download/app.apk）
                try {
                    if (file.createNewFile()){
                        Log.d("downloadFile","创建成果");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("downloadFile", file.getPath());
                InputStream input = null;
                HttpURLConnection connection = null;
                FileOutputStream fos = null;
                try {
                    URL url = new URL(urlStr);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    //允许输出流，即允许上传
                    connection.setUseCaches(false);
                    //不使用缓冲
                    connection.setRequestMethod("GET");
                    //使用get请求
                    connection.connect();
                    Log.d("downloadFile", "" + connection.getResponseCode());
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        Log.d("downloadFile", "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage());
                    }
                    int fileLength = connection.getContentLength();
                    Log.d("downloadFile", "" + fileLength);
                    input = connection.getInputStream();
                    if (input == null) {
                        return;
                    }
                    fos = new FileOutputStream(file);
                    byte[] data = new byte[1024];
                    long total = 0;
                    int len;
                    while ((len = input.read(data)) != -1) {
                        fos.write(data, 0, len);
                        total += len;
                        if (fileLength > 0) {
                            Log.d("downloadFile", "" + (int) (total * 100 / fileLength)+" %");
                        }
                    }
                    Log.d("downloadFile", "" + file.length());
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null && fos != null) {
                            input.close();
                            Message msg = new Message();
                            msg.what = FILE_SUCCESS;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (connection != null)
                        connection.disconnect();
                }
            }
        }).start();
    }

    /**
     * 下图片
     */
    private void downloadBitmap() {
        final String path = "http://www.hinews.cn/pic/003/002/462/00300246261_07308d48.jpg";
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(getCacheDir(), path.substring(path.lastIndexOf("/")));
                if (file.exists() && file.length() > 0) {
                    Log.d("downloadBitmap", "图片存在，拿缓存");
                    Log.d("downloadBitmap", file.getPath());
                    Bitmap bitmap = BitmapFactory.decodeFile(file
                            .getAbsolutePath());
                    Message msg = new Message();
                    //声明消息
                    msg.what = IMAGE_MSG_CACHE_PIC;
                    msg.obj = bitmap;
                    //设置数据
                    handler.sendMessage(msg);
                } else {
                    Log.d("downloadBitmap", "图片不存在，获取数据生成缓存");
                    try {
                        URL url = new URL(path);
                        HttpURLConnection conn = (HttpURLConnection) url
                                .openConnection();
                        if (conn.getResponseCode() == 200) {
                            InputStream is = conn.getInputStream();
                            FileOutputStream fos = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            is.close();
                            fos.close();
                            Log.d("downloadBitmap", file.getPath());
                            Bitmap bitmap = BitmapFactory.decodeFile(file
                                    .getAbsolutePath());
                            Message msg = new Message();
                            msg.obj = bitmap;
                            msg.what = IMAGE_MSG_NEW_PIC;
                            handler.sendMessage(msg);
                        } else {
                            Message msg = new Message();
                            msg.what = IMAGE_ERROR;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        Message msg = Message.obtain();
                        msg.what = IMAGE_EXCEPTION;
                        handler.sendMessage(msg);
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 下载文本html网页
     */
    private void downloadText() {
        final String path = "https://www.baidu.com";
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        String result = response.toString();
                        Message msg = Message.obtain();
                        msg.obj = result;
                        msg.what = TEXT_SUCCESS;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Message msg = Message.obtain();
                    //减少消息创建的数量
                    msg.what = TEXT_ERROR;
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        // 外部类Activity生命周期结束时，同时清空消息队列&结束Handler生命周期
    }
}









