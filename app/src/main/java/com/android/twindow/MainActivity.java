package com.android.twindow;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

/**
 * 实验window及线程，AysncTask
 */

public class MainActivity extends AppCompatActivity {
    private Button mFloatingButton;
    private LayoutParams mLayoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void tWindow(){
         /*  synchronized (getMainLooper()) {
            String a = A.A;
            a = B.B;
        }

        SparseArray s = new SparseArray();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    wait(100);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            t.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
     /*   try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    private void tAsncyTask(){
        //       AsyncTask<List, Integer, Map> asyncTask = new AsyncTask<List, Integer, Map>() {
//
//           @Override
//           protected void onPreExecute() {
//               super.onPreExecute();
//           }
//
//           @Override
//           protected Map doInBackground(List[] lists) {
//               return null;
//           }
//
//           @Override
//           protected void onProgressUpdate(Integer... values) {
//               super.onProgressUpdate(values);
//           }
//
//           @Override
//           protected void onPostExecute(Map list) {
//               super.onPostExecute(list);
//           }
//
//           @Override
//           protected void onCancelled(Map list) {
//               super.onCancelled(list);
//           }
//
//           @Override
//           protected void onCancelled() {
//               super.onCancelled();
//           }
//
//       };

        //asyncTask.cancel();
        // asyncTask.execute()
        // asyncTask.executeOnExecutor()
    }

    private void initView() {
        addWindow();
        //也要看window type，overlay的不行
//        mFloatingButton.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                int rawX = (int) event.getRawX();
//                int rawY = (int) event.getRawY();
//                Log.e("xwpeng16", "onTouch");
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_MOVE: {
//                        mLayoutParams.x = rawX;
//                        mLayoutParams.y = rawY;
//                        getWindowManager().updateViewLayout(mFloatingButton, mLayoutParams);
//                        break;
//                    }
//                }
//                return false;
//            }
//        });
        mFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                addDialog();
                startActivity(new Intent(MainActivity.this, ImageLoaderActivity.class));
//                startActivity(new Intent(MainActivity.this, TThreadLocalActivity.class));
            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                getApplication().startActivity(new Intent(MainActivity.this, TThreadLocalActivity.class));
//       startActivity(new Intent(MainActivity.this, TAsyncTaskActivity.class));
//            }
//        }).start();
    }

    private void addWindow() {
        mFloatingButton = new Button(this);
        mFloatingButton.setText("button");
        mLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        mLayoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_NOT_TOUCH_MODAL
               | LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParams.x = 100;
        mLayoutParams.y = 300;
        mLayoutParams.type = LayoutParams.TYPE_APPLICATION;
        getWindowManager().addView(mFloatingButton, mLayoutParams);
//        getWindowManager().removeViewImmediate(mFloatingButton);
//        getWindowManager().updateViewLayout();
    }

    private void addDialog() {
       Dialog dialog = new Dialog(getApplicationContext());
       dialog.setTitle("系统Window弹Dialog");
        dialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ERROR);
        dialog.show();
    }



}
