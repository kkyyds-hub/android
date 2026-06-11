package com.moon.moonmusic.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.moon.moonmusic.R;
import com.moon.moonmusic.view.MusicWaveCanvasView;

/**
 * 视频播放页：播放谢霆锋 2000 Viva Live 演唱会片段。
 */
public class VideoActivity extends AppCompatActivity {

    public static final String VIDEO_TITLE = "谢霆锋 2000 Viva Live";
    public static final String VIDEO_SUBTITLE = "《活着Viva》演唱会片段";
    public static final String VIDEO_SOURCE_NAME = "Bilibili · BV1o64y127G4";
    public static final String STATUS_LOADING = "正在加载现场片段";
    public static final String STATUS_PLAYING = "2000 Viva Live · 播放中";
    public static final String LIVE_INTRO_TITLE = "Viva Live 演唱会";
    public static final String LIVE_WAVE_TITLE = "现场音浪";
    public static final String COMMENT_SECTION_TITLE = "乐迷评论";
    public static final int COMMENT_COUNT = 3;
    public static final int WAVE_SYNC_INTERVAL_MS = 300;

    private ImageButton btnBack;
    private TextView tvTitle;
    private TextView tvVideoStatus;
    private VideoView videoView;
    private MusicWaveCanvasView musicWaveView;
    private boolean waveAnimating;

    private final Runnable waveSyncRunnable = new Runnable() {
        @Override
        public void run() {
            syncWaveWithVideoState();
            if (videoView != null) {
                videoView.postDelayed(this, WAVE_SYNC_INTERVAL_MS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_video_back);
        tvTitle = findViewById(R.id.tv_video_title);
        tvVideoStatus = findViewById(R.id.tv_video_status);
        videoView = findViewById(R.id.vv_recommend_video);
        musicWaveView = findViewById(R.id.v_live_music_wave);
    }

    private void initData() {
        tvTitle.setText(VIDEO_TITLE);
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            tvVideoStatus.setText(STATUS_PLAYING);
            videoView.start();
            setWaveAnimating(true);
            startWaveSync();
        });

        playVivaLiveClip();
    }

    private void initListener() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void playVivaLiveClip() {
        tvVideoStatus.setText(STATUS_LOADING);
        videoView.setVideoURI(buildLocalVideoUri(getPackageName()));
    }

    public static Uri buildLocalVideoUri(String packageName) {
        return Uri.parse(buildLocalVideoUriString(packageName));
    }

    public static String buildLocalVideoUriString(String packageName) {
        return "android.resource://" + packageName + "/" + R.raw.nicholas_viva_live_2000;
    }

    private void startWaveSync() {
        videoView.removeCallbacks(waveSyncRunnable);
        videoView.post(waveSyncRunnable);
    }

    private void stopWaveSync() {
        if (videoView != null) {
            videoView.removeCallbacks(waveSyncRunnable);
        }
    }

    private void syncWaveWithVideoState() {
        if (videoView == null) return;
        setWaveAnimating(videoView.isPlaying());
    }

    private void setWaveAnimating(boolean animating) {
        if (musicWaveView == null || waveAnimating == animating) return;
        waveAnimating = animating;
        musicWaveView.setAnimationEnabled(animating);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopWaveSync();
        setWaveAnimating(false);
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        stopWaveSync();
        setWaveAnimating(false);
        if (videoView != null) {
            videoView.stopPlayback();
        }
        super.onDestroy();
    }
}
