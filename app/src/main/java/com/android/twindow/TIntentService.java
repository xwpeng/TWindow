package com.android.twindow;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Administrator on 2018/2/7.
 */

public class TIntentService {

    public static void t(Context context){
        Intent service = new Intent(context, LocalIntentService.class);
        service.putExtra("task_action", "com.xwpeng.action.TASK1");
        context.startService(service);
        service.putExtra("task_action", "com.xwpeng.action.TASK2");
        context.startService(service);
        service.putExtra("task_action", "com.xwpeng.action.TASK3");
        context.startService(service);
    }
}
