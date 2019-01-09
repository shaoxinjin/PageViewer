package com.shaoxinjin.pageviewer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

public class NetImageView extends ImageView {
    private static final String TAG = Util.PREFIX + NetImageView.class.getSimpleName();
    // Get max available VM memory, exceeding this amount will throw an
    // OutOfMemory exception. Stored in kilobytes as LruCache takes an
    // int in its constructor.
    private static final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/8th of the available memory for this memory cache.
    private static final int cacheSize = maxMemory / 8;
    private static LruCache<String, Bitmap> mLryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than number of items.
            return bitmap.getByteCount() / 1024;
        }
    };

    public NetImageView(Context context) {
        super(context);
    }

    public NetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NetImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mLryCache.get(key);
    }

    public void setImageURL(final Activity mainPage, final String path, ThreadPoolExecutor threadPoolExecutor) {
        Bitmap b = getBitmapFromMemCache(path);
        if (b != null) {
            setImageBitmap(b);
            return;
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(10000);
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        InputStream inputStream = connection.getInputStream();
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        addBitmapToMemoryCache(path, bitmap);
                        mainPage.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (path.equals(getTag())) {
                                    setImageBitmap(bitmap);
                                }
                            }
                        });
                        inputStream.close();
                    } else {
                        Log.d(TAG, "Server error");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Network error");
                }
            }
        });
    }

    public static void downloadPic(ViewPage viewPage, String url) {
        Bitmap b = mLryCache.get(url);
        if (b == null) {
            return;
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.CHINA);
        Calendar calendar = Calendar.getInstance();
        String name = Environment.getExternalStorageDirectory().getPath() + "/PageViewer/" + df.format(calendar.getTime()) + ".jpg";
        Log.d(TAG, "name is " + name);
        try {
            FileOutputStream fos = new FileOutputStream(name);
            b.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (Exception e) {
            Log.d(TAG, "exception in downloadPic is " + e.getMessage());
            e.printStackTrace();
        }
        Toast toast = Toast.makeText(viewPage, name, Toast.LENGTH_SHORT);
        toast.show();
    }
}
