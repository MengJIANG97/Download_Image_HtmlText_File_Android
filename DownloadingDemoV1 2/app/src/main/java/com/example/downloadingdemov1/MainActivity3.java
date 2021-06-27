package com.example.downloadingdemov1;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity3 extends AppCompatActivity {
    ActivityResultLauncher<Intent> launcher;
    Intent intent;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Button button = findViewById(R.id.button_doc);
        createFile();
        button.setOnClickListener(v -> launcher.launch(intent));
    }



    // Request code for creating a PDF document.文件选择器
    private static final int CREATE_FILE = 1;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createFile() {
        intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        //请求打开DocumentUi界面
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("txt/*");
        intent.putExtra(Intent.EXTRA_TITLE, "invoice.text");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
        launcher =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Toast.makeText(MainActivity3.this, "Ok", Toast.LENGTH_SHORT).show();
                handleOpenDocumentAction(result.getData());
            }
        });
    }


    /**
     * 文档提供器返回给我们的一个Uri,我们可以通过查询该uri，
     * 来获取文档的信息，例如文档id，名称，大小，minitype等
     * 直接从该uri中获取InputStream,outputSteam，
     * 使用java的io流就可以完成文件读写操作
     * @param data
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("LongLogTag")
    private void handleOpenDocumentAction(Intent data){
        if (data==null){
            Log.d("handleOpenDocumentAction","fail");
            return;
        }
        Uri uri = data.getData();
        //获取文档指向的uri,注意这里是指单个文件

        /**
         * 根据该Uri可以获取该Document的信息，
         * 其数据列的名称和解释可以在DocumentsContact类的内部类Document中找到
         */
        Cursor cursor = getContentResolver().query(uri,null,null,null,null);
        StringBuilder sb = new StringBuilder(" open document Uri ");
        sb.append(uri.toString());
        if (cursor!=null&&cursor.moveToFirst()){
            String documentId = cursor.getString(cursor.getColumnIndex(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID));
            String name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            String size = null;
            if (!cursor.isNull(sizeIndex)){
                size = cursor.getString(sizeIndex);
            }else {
                size = "Unknown";
            }
            sb.append(" name ").append(name).append(" size ").append(size);
        }
        Log.d("handleOpenDocumentAction:file",sb.toString());

        /**
         * 从该uri中获取InputSteam，并读取出文本的内容的操作
         */
        /*
        BufferedReader br = null;
        InputStream in=null;

         */
        OutputStream out=null;
        BufferedWriter writer=null;
        try {
            /*
            in = getContentResolver().openInputStream(uri);
            br = new BufferedReader(new InputStreamReader(in));

             */
            out = getContentResolver().openOutputStream(uri);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            String line = "In many cases, your app creates files that other apps don't need to access, or shouldn't access. The system provides the following locations for storing such app-specific files:\n" +
                    "\n" +
                    "Internal storage directories: These directories include both a dedicated location for storing persistent files, and another location for storing cache data. The system prevents other apps from accessing these locations, and on Android 10 (API level 29) and higher, these locations are encrypted. These characteristics make these locations a good place to store sensitive data that only your app itself can access.\n" +
                    "\n" +
                    "External storage directories: These directories include both a dedicated location for storing persistent files, and another location for storing cache data. Although it's possible for another app to access these directories if that app has the proper permissions, the files stored in these directories are meant for use only by your app. If you specifically intend to create files that other apps should be able to access, your app should store these files in the shared storage part of external storage instead.";
                   writer.write(line);
            /*
            while ((line = br.readLine())!=null){
                sb.append(line);
            }

             */
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                /*
                if (br!=null)
                    br.close();
                if (in!=null)
                    in.close();

                 */
                if (writer!=null)
                    writer.close();
                if (out!=null)
                    out.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}










