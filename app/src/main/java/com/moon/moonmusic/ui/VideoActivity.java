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
 * 这里把 VideoView 视频播放和自定义 Canvas 音浪结合起来，让视频状态驱动绘图刷新。
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
            // 定时检查 VideoView 是否正在播放，再决定自定义音浪是否继续刷新。
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

    /**
     * 初始化视频标题、系统控制条和视频准备完成后的回调。
     * 视频准备好后会自动播放，并启动音浪 View 的状态同步。
     */
    private void initData() {
        tvTitle.setText(VIDEO_TITLE);
        // MediaController 是 Android 自带的视频控制条，提供播放、暂停、拖动进度等基础能力。
        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);

        videoView.setOnPreparedListener(mp -> {
            // 视频资源准备好后再 start，避免文件还没解析完成就播放导致黑屏或无响应。
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

    /**
     * 设置本地视频资源并显示加载状态。
     * VideoView 会异步准备视频，准备完成后触发 initData 中注册的回调。
     */
    private void playVivaLiveClip() {
        tvVideoStatus.setText(STATUS_LOADING);
        // 视频放在 res/raw 中，通过 android.resource:// 形式交给 VideoView 播放。
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

    /**
     * 根据 VideoView 的播放状态同步音浪动画。
     * 定时任务会反复调用这里，让视频暂停时绘图也停止刷新。
     */
    private void syncWaveWithVideoState() {
        if (videoView == null) return;
        setWaveAnimating(videoView.isPlaying());
    }

    /**
     * 开启或关闭自定义音浪动画。
     * 内部会避免重复设置同一个状态，减少不必要的 invalidate 调用。
     */
    private void setWaveAnimating(boolean animating) {
        if (musicWaveView == null || waveAnimating == animating) return;
        waveAnimating = animating;
        // VideoView 播放时开启 Canvas 动画，暂停或离开页面时停止，节省刷新开销。
        musicWaveView.setAnimationEnabled(animating);
    }

    /**
     * 页面进入后台时暂停视频和音浪刷新。
     * 这样离开视频页后不会继续占用播放和绘图资源。
     */
    @Override
    protected void onPause() {
        super.onPause();
        // 离开视频页时暂停视频和音浪，避免看不到页面时还在播放。
        stopWaveSync();
        setWaveAnimating(false);
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    /**
     * 页面销毁时停止视频播放并移除音浪同步任务。
     * 这是 VideoView 和自定义 View 联动关系的收尾位置。
     */
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
