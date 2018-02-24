package com.android.twindow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * 实验ThreadLocal
 * Created by Administrator on 2018/1/30.
 */

public class TThreadLocalActivity extends AppCompatActivity {
    private final static String TAG = AppCompatActivity.class.getSimpleName();
    private ThreadLocal<Boolean> mThreadLocal = new ThreadLocal<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threadlocal);
        tThreadLocal();
    }


    private void tThreadLocal(){
        mThreadLocal.set(true);
        Log.d(TAG, "[Thread#main]mThreadLocal=" + mThreadLocal.get());

        new Thread("Thread#1") {
            @Override
            public void run() {
                super.run();
                mThreadLocal.set(false);
                Log.d(TAG, "[Thread#1]mThreadLocal=" + mThreadLocal.get());

            }
        }.start();

        new Thread("Thread#2") {
            @Override
            public void run() {
                super.run();
                Log.d(TAG, "[Thread#2]mThreadLocal=" + mThreadLocal.get());

            }
        }.start();

        new TThread(){
            @Override
            public void run() {
                mString = "aa";
                super.run();
            }
        };
    }
}
