package com.android.twindow;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.twindow.imageloader.MyUtils;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 实验bitmap高效加载与缓存
 *
 * Created by Administrator on 2018/2/12.
 */

public class TBitmap {
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;//50M
    private static final int DISK_CACHE_INDEX = 0;//50M
    private static final int IO_BUFFER_SIZE = 2048;
    private DiskLruCache mDiskLruCache;

    public static void t() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        LruCache memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        //        memoryCache.get();
//        memoryCache.put();
//        memoryCache.remove();
    }

    public void t2(Context context, final String url) {
        File diskCacheDir = new File(context.getCacheDir(), "bitmap");
        if (!diskCacheDir.exists()) diskCacheDir.mkdirs();
        try {
            mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
            String key = hashKeyFromUrl(url);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                if (downloadUrlToStream(url, outputStream)) editor.commit();
                else editor.abort();
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从缓存加载图片
     */
    public void loadCache(String url) {
        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot == null) return;
            FileInputStream fin = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fin.getFD();
//            bitmap = ImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor, reqW, reqH);
//            if (bitmap != null) addBitmapToMemoryCache(key, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 网络下载图片
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



}
