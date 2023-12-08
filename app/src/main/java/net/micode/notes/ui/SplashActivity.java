package net.micode.notes.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.ui.NotesListActivity;

public class SplashActivity extends AppCompatActivity {
    private static final int ANIMATION_DURATION = 1500; // 动画持续时间，单位为毫秒
    private static final int SPLASH_DURATION = 2000; // 欢迎页展示时间，单位为毫秒

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 获取 TextView 的引用
        TextView textView = findViewById(R.id.fullscreen_content);

        // 创建透明度动画对象，从完全透明到不透明
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(ANIMATION_DURATION); // 设置动画持续时间

        // 应用动画效果到 TextView 的背景上
        textView.startAnimation(alphaAnimation);

        // 当计时结束时，跳转至 NotesListActivity
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, NotesListActivity.class);
                startActivity(intent);
                finish(); // 销毁欢迎页
            }
        }, SPLASH_DURATION);
    }
}
