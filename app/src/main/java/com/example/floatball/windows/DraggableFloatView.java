package com.example.floatball.windows;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;

import com.example.floatball.R;

/**
 * 浮动按钮视图
 */

@SuppressLint("ViewConstructor")
public class DraggableFloatView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = DraggableFloatView.class.getSimpleName();

    private Context mContext;
    private TextClock mTextClock;
    private OnFlingListener mOnFlingListener;
    private OnTouchButtonClickListener mTouchButtonClickListener;

    public DraggableFloatView(Context context, OnFlingListener flingListener) {
        super(context);
        mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.floatview, this);
        mTextClock = findViewById(R.id.textClock);
        mOnFlingListener = flingListener;
        mTextClock.setOnTouchListener(new OnTouchListener() {
            //刚按下是起始位置的坐标
            float startDownX, startDownY;
            float downX, downY;
            float moveX, moveY;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "ACTION_DOWN");
                        startDownX = downX = motionEvent.getRawX();
                        startDownY = downY = motionEvent.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "ACTION_MOVE");
                        moveX = motionEvent.getRawX();
                        moveY = motionEvent.getRawY();
                        if (mOnFlingListener != null)
                            mOnFlingListener.onMove(moveX - downX, moveY - downY);
                        downX = moveX;
                        downY = moveY;
                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP");
                        float upX = motionEvent.getRawX();
                        float upY = motionEvent.getRawY();
                        if (upX == startDownX && upY == startDownY)
                            return false;
                        else
                            return true;
                }
                return true;
            }
        });
        mTextClock.setOnClickListener(this);
    }

    public void setTouchButtonClickListener(OnTouchButtonClickListener touchButtonClickListener) {
        mTouchButtonClickListener = touchButtonClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mTouchButtonClickListener != null) {
            mTouchButtonClickListener.onClick(v);
        }
    }

    public interface OnTouchButtonClickListener {
        void onClick(View view);
    }
}
