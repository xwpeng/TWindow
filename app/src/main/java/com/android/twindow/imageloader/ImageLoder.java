package com.android.twindow.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 二级缓存图片加载器
 * Created by Administrator on 2018/2/22.
 */

public class ImageLoder {
    private static final String TAG = ImageLoder.class.getSimpleName();
    private static final int MESSAGE_POST_RESULT = 1;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;//50M
    private static final int DISK_CACHE_INDEX = 0;//50M
    private static final int IO_BUFFER_SIZE = 2048;
    private static final int TAG_KEY_URL = "tag_key_url".hashCode();
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private Context mContext;
    private boolean mIsDiskLruCacheCreated;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };

    private final static Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), sThreadFactory);

    //写成内部类不知道会不会又问题，内存泄露的问题
    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            String url = (String) imageView.getTag(TAG_KEY_URL);
            if (url.equals(result.url)) imageView.setImageBitmap(result.bitmap);
            else Log.w(TAG, "set image bitmap, but url has changed, ignored!");
        }
    };


    public ImageLoder(Context context) {
        mContext = context.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
        if (!diskCacheDir.exists()) diskCacheDir.mkdirs();
        if (getUsableSpace(diskCacheDir) <= DISK_CACHE_SIZE) return;
        try {
            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
            mIsDiskLruCacheCreated = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStrorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStrorageAvailable) cachePath = context.getExternalCacheDir().getPath();
        else cachePath = context.getCacheDir().getPath();
        return new File(cachePath + File.separator + uniqueName);
    }

    private long getUsableSpace(File path) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            return path.getUsableSpace();
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) mMemoryCache.put(key, bitmap);
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    @Nullable
    private Bitmap loadBitmapFromHttp(String url, int reqW, int reqH) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper())
            throw new RuntimeException("can not visit network from UI Thread");
        if (mDiskLruCache == null) return null;
        String key = hashKeyFromUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null) {
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downloadUrlToStream(url, outputStream)) editor.commit();
            else editor.abort();
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url, reqW, reqH);
    }

    @Nullable
    private Bitmap loadBitmapFromDiskCache(String url, int reqW, int reqH) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper())
            Log.w(TAG, "load bitmap from  UI Thread, it's not recommended!");
        if (mDiskLruCache == null) return null;
        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = ImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqW, reqH);
            if (bitmap != null) addBitmapToMemoryCache(key, bitmap);
        }
        return bitmap;
    }


    private String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = byteToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
            e.printStackTrace();
        }
        return cacheKey;
    }

    private String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }


    /**
     * 网络下载图片
     *
     * @param urlString
     * @param outputStream
     * @return
     */
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) out.write(b);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            MyUtils.close(out);
            MyUtils.close(in);
        }
        return false;
    }

    private Bitmap downloadBitmapFromUrl(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            MyUtils.close(in);
        }
        return bitmap;
    }


    /**
     * 同步加载图片
     */
    public Bitmap loadBitmap(String url, int reqW, int reqH) {
        Bitmap bitmap = getBitmapFromMemCache(hashKeyFromUrl(url));
        if (bitmap != null) {
            Log.d(TAG, "loadBitmapFromMemCache,url:" + url);
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(url, reqW, reqH);
            if (bitmap != null) {
                Log.d(TAG, "loadBitmapFromDisk,url:" + url);
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(url, reqW, reqH);
            Log.d(TAG, "loadBitmapFromHttp,url:" + url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap == null && !mIsDiskLruCacheCreated) {
            Log.w(TAG, "encounter error, DiskLruCache is not created");
            bitmap = downloadBitmapFromUrl(url);
        }
        return bitmap;
    }

    public void bindBitmap(final String url, final ImageView imageView, final int reqW, final int reqH){
        imageView.setTag(TAG_KEY_URL, url);
        Bitmap bitmap = getBitmapFromMemCache(hashKeyFromUrl(url));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url, reqW, reqH);
                LoaderResult result = new LoaderResult(imageView, url, bitmap);
                mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }

    private static class LoaderResult {
        public ImageView imageView;
        public String url;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String url, Bitmap bitmap) {
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.url = url;
        }
    }

}
