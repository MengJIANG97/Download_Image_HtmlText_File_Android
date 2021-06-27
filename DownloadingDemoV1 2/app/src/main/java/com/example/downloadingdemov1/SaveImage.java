package com.example.downloadingdemov1;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.downloadingdemov1.utils.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class SaveImage {
    static String name = "";
    static String mimetype = "";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean saveimage(String src, Context context) {
        if (TextUtils.isEmpty(src)) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
        values.put(MediaStore.Images.Media.MIME_TYPE, FileUtils.getMimeType(src));
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        Uri item = resolver.insert(collection, values);

        FileOutputStream out = null;
        InputStream in = null;
        ParcelFileDescriptor pfd = null;
        try {
            pfd = resolver.openFileDescriptor(item, "w", null);
            if (pfd == null)
                return false;
            out = new FileOutputStream(pfd.getFileDescriptor());
            //in = getInputStream(srcPath);
            //MimeUtil.copy(in, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (pfd != null) pfd.close();
                if (in != null) in.close();
            } catch (IOException e) {
                Log.d("TAG", "saveImage finally: " + e.getMessage());
            }
        }
        values.clear();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(item, values, null, null);
        return true;
    }
}
