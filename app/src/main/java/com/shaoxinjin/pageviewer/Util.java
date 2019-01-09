package com.shaoxinjin.pageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Util {
    public static final String PREFIX = "PageViewer_";
    private static final String TAG = PREFIX + Util.class.getSimpleName();

    public static Document getDocument(String path) throws Exception {
        return Jsoup.parse(new URL(path), 5000);
    }

    public static String getCommonPageUrl(String firstPicUrl, int pageNum) {
        String currentPage;
        if (pageNum == 1) {
            currentPage = firstPicUrl;
        } else {
            currentPage = firstPicUrl.replace(".html", "_" + String.valueOf(pageNum) + ".html");
        }
        return currentPage;
    }

    public static String getCommonName(String originName) {
        if (originName.split("：|之").length >= 2) {
            return originName.split("：|之")[1];
        } else {
            return originName;
        }
    }

    static void setPicFromUrl(Context context, String url, ImageView imageView) {
        Glide.with(context).load(url).placeholder(R.drawable.ic_loading).into(imageView);
    }

    static void downloadPic(final ViewPage viewPage, String url) {
        Glide.with(viewPage).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.CHINA);
                Calendar calendar = Calendar.getInstance();
                final String name = Environment.getExternalStorageDirectory().getPath() + "/PageViewer/" + df.format(calendar.getTime()) + ".jpg";
                Log.d(TAG, "name is " + name);
                try {
                    FileOutputStream fos = new FileOutputStream(name);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                } catch (Exception e) {
                    Log.d(TAG, "exception in downloadPic is " + e.getMessage());
                    e.printStackTrace();
                }
                viewPage.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast toast = Toast.makeText(viewPage, name, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });
    }
}
