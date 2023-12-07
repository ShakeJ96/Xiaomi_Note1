package net.micode.notes.ui;

import android.annotation.SuppressLint;

import android.content.Intent;

import android.support.v7.app.ActionBar;

import android.support.v7.app.AppCompatActivity;

import android.os.Build;

import android.os.Bundle;

import android.os.Handler;

import android.view.MotionEvent;

import android.view.View;

import android.view.WindowInsets;

import net.micode.notes.R;

public class SplashActivity extends AppCompatActivity {
    Handler mHandler=new Handler();



    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //加载启动界面
        setContentView(R.layout.activity_splash); //加载启动图片


// 当计时结束时，跳转至NotesListActivity

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent();
                intent.setClass(SplashActivity.this, NotesListActivity.class);
                startActivity(intent);
                finish(); //销毁欢迎页
            }
        }, 2000); // 2秒后跳转

    }

}