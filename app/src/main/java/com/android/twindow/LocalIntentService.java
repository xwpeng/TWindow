package com.android.twindow;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import static java.lang.Thread.currentThread;

/**
 * 实验LocalIntentService
 * Created by Administrator on 2018/2/7.
 */

public class LocalIntentService extends IntentService {
    private final static String TAG = LocalIntentService.class.getSimpleName();
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LocalIntentService(String name) {
        super(name);
    }

    public LocalIntentService() {
        super(TAG);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getStringExtra("task_action");
        Log.e(TAG, "receive task : " + action);
        SystemClock.sleep(3000);
        if ("com.xwpeng.action.TASK1".equals(action)) Log.e(TAG, "handle task: " + action);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.e(TAG, "onStart: " + currentThread().getName());
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: " + currentThread().getName());
        super.onDestroy();
    }
}
