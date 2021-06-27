package com.example.downloadingdemov1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.UserDictionary;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.security.Permission;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageView;
    private TextView textView;
    TextView textView1;
    Button button;

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

    @SuppressLint("HandlerLeak")
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint({"Recycle", "SetTextI18n"})
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



        /**
         * 练习registerForActivityResult()方法
         */
        button = findViewById(R.id.button10);
        textView1 = findViewById(R.id.text_view);
        Log.d("create", MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY).toString());
        Log.d("create", MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
        /*
        @SuppressLint("SetTextI18n")
        ActivityResultLauncher<String> launcher = registerForActivityResult(new MyActivityResultContract(), result -> {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            textView1.setText("回传数据: " + result);
        });
        button.setOnClickListener(v -> launcher.launch("Hello,技术最TOP"));

         */
        //自定义方法的官方封装
        Intent intent1 = new Intent(this,MainActivity2.class);
        intent1.putExtra("name","Hello,技术最TOP");
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result -> {
            Toast.makeText(MainActivity.this, result.getData().getStringExtra("result"), Toast.LENGTH_SHORT).show();
            textView1.setText("回传数据: " + result.getData().getStringExtra("result"));
        });
        button.setOnClickListener(v -> {
            launcher.launch(intent1);
        });

        registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result)
                Toast.makeText(MainActivity.this, "BLUETOOTH Permission is granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "BLUETOOTH Permission is denied", Toast.LENGTH_SHORT).show();
        }).launch(Manifest.permission.BLUETOOTH);



        Button button20 = findViewById(R.id.button20);
        button20.setOnClickListener(v -> {
            Intent intent = new Intent(this,MainActivity3.class);
            startActivity(intent);
        });
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
                //下文件
                //downloadFile();
                //正常下载
                downloadFileBreakpoint();
                //断点下载
                break;
            case R.id.btn_downloadText:
                downloadText();
                //下载html网页
                break;
        }
    }

    /**
     * RandomFile，okhttp实现断点下载
     */
    private void downloadFileBreakpoint() {
        final String urlStr = "https://www.kotlincn.net/docs/kotlin-docs.pdf";
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(MainActivity.this, "无法获取SDCard", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;
                RandomAccessFile savedFile = null;
                String filename = urlStr.substring(urlStr.lastIndexOf("/"));
                File file = null;
                try {
                    file = new File(getExternalFilesDir(null), filename);
                    if (file.createNewFile()) {
                        Log.d("downloadFile", "新创建成功");
                    }
                    long downdLength = file.length();
                    //已经下载的文件长度
                    long contentLength = getContentLength(urlStr);
                    if (contentLength == 0) {
                        Toast.makeText(MainActivity.this, "Success下载失败", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (downdLength == contentLength) {
                        //说明已经下载完成了
                        Toast.makeText(MainActivity.this, "Success下载完成", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("RANGE", "bytes=" + downdLength + "-")
                            //断点下载，指定从那个字节开始下载
                            .url(urlStr)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response != null) {
                        in = response.body().byteStream();
                        savedFile = new RandomAccessFile(file, "rw");
                        savedFile.seek(downdLength);
                        //跳过已经下载的字节
                        byte[] bytes = new byte[1024];
                        int total = 0;
                        int len;
                        while ((len = in.read(bytes)) != -1) {
                            total += len;
                            savedFile.write(bytes, 0, len);
                            int progress = (int) ((total + downdLength) * 100 / contentLength);
                            Log.d("downloadFile", "" + progress + "%");
                        }
                    }
                    response.body().close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (savedFile != null && in != null) {
                            savedFile.close();
                            in.close();
                            Message msg = Message.obtain();
                            msg.what = FILE_SUCCESS;
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * 获取下载文件的长度
     *
     * @return
     */
    public long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }


    /**
     * 下文件
     */
    private void downloadFile() {
        final String urlStr = "https://downloads.jianshu.io/apps/haruki/JianShu-2.2.3-17040111.apk";
        /**
         * 返回的状态为 MEDIA_MOUNTED，
         * 那么您就可以在外部存储空间中读取和写入应用专属文件。
         * 如果返回的状态为 MEDIA_MOUNTED_READ_ONLY，您只能读取这些文件。
         */
        String state = Environment.getExternalStorageState();
        //MEDIA_MOUNTED SD卡正常挂载,因此可以读取或者向SD卡写入
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(this, "无法获取SDCard", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ///storage/emulated/0/Android/data/com.example.downloadingdemov1/files/JianShu-2.2.3-17040111.apk目录
                String filename = urlStr.substring(urlStr.lastIndexOf("/"));
                File file = new File(getExternalFilesDir(null), filename);
                //创建文件路径（/storage/emulated/0/Android/data/com.example.downloadingdemov1/files/JianShu-2.2.3-17040111.apk）
                try {
                    if (file.createNewFile()) {
                        Log.d("downloadFile", "创建成果");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("downloadFile", file.getPath());
                ///storage/emulated/0/Android/data/com.example.downloadingdemov1/files/JianShu-2.2.3-17040111.apk
                Log.d("downloadFile", new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename).getPath());
                // /storage/emulated/0/Android/data/com.example.downloadingdemov1/files/Pictures/JianShu-2.2.3-17040111.apk
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
                            Log.d("downloadFile", "" + (int) (total * 100 / fileLength) + " %");
                        }
                    }
                    Log.d("downloadFile", "" + file.length());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null && fos != null) {
                            input.close();
                            fos.close();
                            Message msg = Message.obtain();
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









