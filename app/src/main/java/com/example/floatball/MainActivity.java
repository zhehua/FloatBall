package com.example.floatball;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.floatball.windows.DraggableFloatView;
import com.example.floatball.windows.DraggableFloatWindow;

import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DraggableFloatWindow mFloatWindow;
    private static final String TAG = "MainActivity";
    private Button show_window;
    private Button hide_window;
    private boolean isShow = false;
    private static final long CLICK_INTERVAL_TIME = 300;
    private static long lastClickTime = 0;
    private static boolean isRunningForeground=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        setContentView(R.layout.activity_main);
        show_window = findViewById(R.id.show_window);
        show_window.setOnClickListener(this);
        hide_window = findViewById(R.id.hide_window);
        hide_window.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_window:
                if (commonROMPermissionCheck(MainActivity.this)) {
                    showWindow();
                } else {
                    requestAlertWindowPermission();
                }
                break;

            case R.id.hide_window:
                if (mFloatWindow != null && isShow==true) {
                    mFloatWindow.dismiss();
                }
                isShow=false;
                break;
        }
    }

    private void showWindow() {
        mFloatWindow = DraggableFloatWindow.getDraggableFloatWindow(this, null);
        mFloatWindow.show();
        isShow = true;
        mFloatWindow.setOnTouchButtonListener(new DraggableFloatView.OnTouchButtonClickListener() {
            @Override
            public void onClick(View view) {
                //获取从开机到现在的毫秒数(不包括手机睡眠时间)
                long currentTimeMillis = SystemClock.uptimeMillis();
                //连续两次点击间隔小于300ms为双击
                if (currentTimeMillis - lastClickTime < CLICK_INTERVAL_TIME) {
                    setApp2Top(MainActivity.this);
                   // Toast.makeText(MainActivity.this, "双击击了悬浮球", Toast.LENGTH_SHORT).show();
                }else {
                    //Toast.makeText(MainActivity.this, "单击了悬浮球", Toast.LENGTH_SHORT).show();

                }
                lastClickTime = currentTimeMillis;
            }
        });
    }

    private static final int REQUEST_CODE = 1;

    //检查权限
    private boolean commonROMPermissionCheck(Context context) {
        Boolean result = true;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class clazz = Settings.class;
                Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                Settings.canDrawOverlays(context);
                result = (Boolean) canDrawOverlays.invoke(null, context);
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return result;
    }

    //申请权限
    private void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    //处理回调
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Log.i(TAG, "授权成功");
                    showWindow();
                } else {
                    Log.i(TAG, "授权失败");
                }
            }
        }
    }

    /**
     * 重新进入显示悬浮窗
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isShow) {
            showWindow();
        }
    }

    /**
     * 进入后台隐藏悬浮窗
     */
    @Override
    protected void onPause() {
        isRunningForeground=false;
        System.out.println("onPause.........111111............");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mFloatWindow != null&&isShow==true) {
            mFloatWindow.dismiss();
        }
        super.onDestroy();
    }

    /**
     * 切换到前台
     *
     * @param context
     */
    public static void setApp2Top(Context context) {
        if (!isRunningForeground) {
            //获取ActivityManager
            ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            //获得当前运行的task(任务)
            List<ActivityManager.RunningTaskInfo> taskInfoList = activityManager.getRunningTasks(100);
            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                //找到本应用的 task，并将它切换到前台
                if (taskInfo.topActivity.getPackageName().equals(context.getPackageName())) {
                    activityManager.moveTaskToFront(taskInfo.id, 0);
                    break;
                }
            }
        }
    }
}
