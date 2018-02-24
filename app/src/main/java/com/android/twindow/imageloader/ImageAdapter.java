package com.android.twindow.imageloader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.twindow.R;

import java.util.List;

/**
 * Created by Administrator on 2018/2/23.
 */

public class ImageAdapter extends BaseAdapter {
    private List<String> mUrlList;
    private ImageLoder mImageLoder;
    private boolean mCanGetBitmapFromNetWork = false;
    private int mImageWidth = 0;
    private boolean mIsWifi = false;
    private boolean mIsGridViewIdle = true;
    private Context mContext;

    public ImageAdapter(Context context, @NonNull List<String> urlList) {
        mContext = context;
        mUrlList = urlList;
        mImageLoder = new ImageLoder(context.getApplicationContext());
        int screenWidth = MyUtils.getScreenMetrics(context).widthPixels;
        int space = (int) MyUtils.dp2px(context, 20f);
        mImageWidth = (screenWidth - space) / 3;
        mIsWifi = MyUtils.isWifi(context);
        if (mIsWifi) mCanGetBitmapFromNetWork = true;
    }

    public void setCanGetBitmapFromNetWork(boolean can) {
        mCanGetBitmapFromNetWork = can;
    }

    public void setmIsGridViewIdle(boolean isGridViewIdle) {
        this.mIsGridViewIdle = mIsGridViewIdle;
    }

    @Override
    public int getCount() {
        return mUrlList == null ? 0 : mUrlList.size();
    }

    @Override
    public String getItem(int position) {
        return mUrlList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_image_loader, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ImageView imageView = holder.imageView;
        final Object tag = imageView.getTag();
        String tagStr = null;
        if (tag instanceof String) tagStr = (String) tag;
        final String url = getItem(position);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(mImageWidth, mImageWidth));
        if (!url.equals(tagStr)) imageView.setImageResource(R.drawable.image_default);
        if (mIsGridViewIdle && mCanGetBitmapFromNetWork) {
            imageView.setTag(url);
            mImageLoder.bindBitmap(url, imageView, mImageWidth, mImageWidth);
        }
        return convertView;
    }


    private static class ViewHolder {
        ImageView imageView;
    }

}
