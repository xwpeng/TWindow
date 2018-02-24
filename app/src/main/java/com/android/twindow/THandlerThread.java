package com.android.twindow;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 实验HandlerThread
 * Created by Administrator on 2018/2/7.
 */

public class THandlerThread {
    public static void t(){
        HandlerThread handlerThread = new HandlerThread("t");
        handlerThread.run();
        Handler h = new Handler(handlerThread.getLooper());
        h.sendEmptyMessage(1);
        handlerThread.start();
        handlerThread.quit();
    }
}
