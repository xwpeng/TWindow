package com.android.twindow;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.currentThread;

/**
 * 实验AsyncTask
 * Created by Administrator on 2018/2/5.
 */

public class TAsyncTaskActivity extends AppCompatActivity {
   private final  static String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_t_asynctask);
        findViewById(R.id.t_asynctask_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TIntentService.t(TAsyncTaskActivity.this);
            }
        });
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(1);

            @Override
            public Thread newThread(@NonNull Runnable r) {
                return new Thread("xwpeng #" +  count);
            }
        };
        ThreadPoolExecutor t = new ThreadPoolExecutor(2, 4, 60, TimeUnit.SECONDS
                , new LinkedBlockingQueue<Runnable>(64), threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

            }
        });
    }

    private void t(){
        new MyAsyncTask("AsyncTask#1").execute();
        new MyAsyncTask("AsyncTask#2").execute();
        new MyAsyncTask("AsyncTask#3").execute();
        new MyAsyncTask("AsyncTask#4").execute();
        new MyAsyncTask("AsyncTask#5").execute();

    }

    private void t2() {
        new MyAsyncTask("AsyncTask#1").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new MyAsyncTask("AsyncTask#2").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new MyAsyncTask("AsyncTask#3").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new MyAsyncTask("AsyncTask#4").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new MyAsyncTask("AsyncTask#5").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void t3() {
         Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, currentThread().getName());

            }
        }, "Thread t3");
        t3.start();
        t3.start();//java.lang.IllegalThreadStateException: Thread already started

    }


    private void t4(){
        ScheduledExecutorService s = Executors.newScheduledThreadPool(2);
        ExecutorService fixed = Executors.newFixedThreadPool(2);
        ExecutorService cached =  Executors.newCachedThreadPool();
    }

    private static class MyAsyncTask extends AsyncTask<String, Integer, String> {
    private String mName = "AsyncTask";
    public MyAsyncTask(String name) {
        super();
        mName = name;
    }
        @Override
        protected String doInBackground(String... strings) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return mName;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            Log.e(TAG, s + "execute finish at " + df.format(new Date()));
        }
    }
}