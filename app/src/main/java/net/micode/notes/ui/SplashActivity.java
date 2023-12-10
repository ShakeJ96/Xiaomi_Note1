package net.micode.notes.ui;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import net.micode.notes.R;

public class SplashActivity extends AppCompatActivity {
    private static final int ANIMATION_DURATION = 2500; // 动画持续时间，单位为毫秒
    private static final int SPLASH_DURATION = 3000; // 欢迎页展示时间，单位为毫秒

    private Handler mHandler = new Handler();
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // 创建 MediaPlayer 对象，并指定要播放的音频文件
//        playAudio(R.raw.testmusic);


        // 获取 TextView 的引用
        TextView textView = findViewById(R.id.fullscreen_content);

        // 创建透明度动画对象，从完全透明到不透明
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(ANIMATION_DURATION); // 设置动画持续时间

        // 应用动画效果到 TextView 的背景上
        textView.startAnimation(alphaAnimation);

        /**
         * 版本问题，目前主流已经废弃了~
         */
        /*//自主点击跳转
        Button skipButton = findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, NotesListActivity.class);
                startActivity(intent);
                finish(); // 销毁欢迎页
            }
        });*/

        // 当计时结束时，跳转至 NotesListActivity
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // 在 Activity 销毁时停止播放音频并释放 MediaPlayer 资源
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }

                Intent intent = new Intent(SplashActivity.this, NotesListActivity.class);
                startActivity(intent);
                finish(); // 销毁欢迎页
            }
        }, SPLASH_DURATION);
    }
    //测试音频BGM
    private void playAudio(int audioResId) {
        // 创建 MediaPlayer 对象，并指定要播放的音频文件
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, audioResId);
        // 开始播放音频
        mediaPlayer.start();
        // 在播放完成后停止播放并释放资源
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
        });
    }

}
