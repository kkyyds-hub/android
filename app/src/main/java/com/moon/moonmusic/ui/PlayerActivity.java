package com.moon.moonmusic.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.moon.moonmusic.R;
import com.moon.moonmusic.constant.AppConstants;
import com.moon.moonmusic.data.SongRepository;
import com.moon.moonmusic.model.Song;
import com.moon.moonmusic.service.DownloadService;
import com.moon.moonmusic.service.PlayerService;
import com.moon.moonmusic.util.AssetUtil;
import com.moon.moonmusic.util.TimeUtil;

import java.util.List;

/**
 * 播放页面：QQ 音乐风格的版式（不做唱片旋转）
 * - SeekBar + 时间显示 + 上一首/播放暂停/下一首/返回
 * - 歌词从 assets/lyrics 读取（可滚动）
 * - “下载”按钮：复制到 App 专属外部目录，下载页可播放
 * - 封面特效按钮演示图片旋转、ColorMatrix 灰度滤镜和缩放动画
 */
public class PlayerActivity extends AppCompatActivity {

    // 这些 key 是页面跳转时的“约定”，列表页、下载页和播放器都靠它们传递同一首歌。
    public static final String EXTRA_SONG_ID = "song_id";
    public static final String EXTRA_PLAY_FROM_DOWNLOAD = "play_from_download";
    public static final int EFFECT_NORMAL = 0;
    public static final int EFFECT_ROTATE = 1;
    public static final int EFFECT_GRAY_SCALE = 2;

    private TextView tvTitle, tvType, tvProgress, tvTotal, tvLyric;
    private android.widget.ImageView ivCover;
    private SeekBar seekBar;
    private ImageButton btnPrev, btnPlayPause, btnNext, btnExit;
    private Button btnDownload, btnCoverEffect;

    private PlayerService playerService;
    private boolean bound = false;

    private int songId = 1;
    private boolean playFromDownload = false;
    private Song currentSong;
    private int coverEffectMode = EFFECT_NORMAL;

    private final Handler handler = new Handler();
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (bound && playerService != null) {
                int pos = playerService.getCurrentPosition();
                int dur = playerService.getDuration();
                if (dur > 0) {
                    seekBar.setMax(dur);
                    seekBar.setProgress(pos);
                    tvProgress.setText(TimeUtil.formatMs(pos));
                    tvTotal.setText(TimeUtil.formatMs(dur));
                }

                // 播放进度由 Service 中的 MediaPlayer 提供，Activity 只负责每 0.5 秒刷新界面。
                btnPlayPause.setImageResource(playerService.isPlaying()
                        ? R.drawable.ic_player_pause
                        : R.drawable.ic_player_play);
            }
            handler.postDelayed(this, 500);
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder b = (PlayerService.PlayerBinder) service;
            playerService = b.getService();
            bound = true;
            // 绑定成功后才能调用 Service 方法；这里根据 Intent 传入的歌曲 id 设置并播放。
            playerService.setSongById(songId, playFromDownload, true);
            renderSong(songId);
            handler.post(ticker);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
            playerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        parseIntent();
        inis();

        // 先启动前台服务保证音乐可持续播放，再 bind 让本页面能控制播放、暂停和进度。
        Intent svc = new Intent(this, PlayerService.class);
        svc.setAction(AppConstants.ACTION_SET_SONG);
        svc.putExtra(AppConstants.EXTRA_SONG_ID, songId);
        svc.putExtra(AppConstants.EXTRA_FROM_DOWNLOAD, playFromDownload);
        startForegroundServiceCompat(this, svc);
        bindService(new Intent(this, PlayerService.class), connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 统一初始化入口。
     */
    private void inis() {
        initView();
        initData();
        initListener();
    }

    private void parseIntent() {
        Intent it = getIntent();
        if (it != null) {
            // 多页面跳转知识点：不同入口只传歌曲 id，播放器内部再统一渲染音频、封面和歌词。
            songId = it.getIntExtra(EXTRA_SONG_ID, 1);
            playFromDownload = it.getBooleanExtra(EXTRA_PLAY_FROM_DOWNLOAD, false);
        }
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_music_title);
        tvType = findViewById(R.id.tv_type);
        tvProgress = findViewById(R.id.tv_progress);
        tvTotal = findViewById(R.id.tv_total);

        // 歌词放在首屏下面，页面整体滚动，比在 TextView 内部再嵌套滚动更适合演示。
        tvLyric = findViewById(R.id.tv_lyric);

        // 首屏至少撑满一个屏幕高度，这样打开播放器时先看到封面和控制区，向下滑才看歌词。
        View firstScreen = findViewById(R.id.cl_first_screen);
        if (firstScreen != null) {
            int screenH = getResources().getDisplayMetrics().heightPixels;
            if (screenH > 0) firstScreen.setMinimumHeight(screenH);
        }

        ivCover = findViewById(R.id.iv_music);
        seekBar = findViewById(R.id.sb);

        btnPrev = findViewById(R.id.btn_play);
        btnPlayPause = findViewById(R.id.btn_pause);
        btnNext = findViewById(R.id.btn_continue_play);
        btnExit = findViewById(R.id.btn_exit);
        btnDownload = findViewById(R.id.btn_download);
        btnCoverEffect = findViewById(R.id.btn_cover_effect);
    }

    private void initData() {
        renderSong(songId);
        // 初始图标先显示播放，真正状态随后由 ticker 根据 MediaPlayer 同步。
        btnPlayPause.setImageResource(R.drawable.ic_player_play);
    }

    private void initListener() {
        btnPrev.setOnClickListener(v -> {
            if (!bound) return;
            playerService.prev();
            Song s = playerService.getCurrentSong();
            if (s != null) renderSong(s.getId());
        });

        btnPlayPause.setOnClickListener(v -> {
            if (!bound) return;
            playerService.togglePlayPause();
            // UI 由 ticker 同步，避免点击后图标和真实播放状态不一致。
        });

        btnNext.setOnClickListener(v -> {
            if (!bound) return;
            playerService.next();
            Song s = playerService.getCurrentSong();
            if (s != null) renderSong(s.getId());
        });

        btnExit.setOnClickListener(v -> finish());

        btnDownload.setOnClickListener(v -> {
            if (currentSong == null) return;
            if (SongRepository.isDownloaded(this, currentSong)) {
                toast("已下载：下载页可直接播放");
                return;
            }
            Intent ds = new Intent(this, DownloadService.class);
            ds.setAction(AppConstants.ACTION_DOWNLOAD);
            ds.putExtra(AppConstants.EXTRA_SONG_ID, currentSong.getId());
            // 下载交给 Service 做，页面只发起命令并提示用户，符合“后台任务不阻塞界面”的思路。
            startService(ds);
            toast("开始下载…");
        });

        btnCoverEffect.setOnClickListener(v -> {
            // 图片特效知识点：每次点击在原图、旋转、灰度放大三种效果之间切换。
            coverEffectMode = nextCoverEffectMode(coverEffectMode);
            applyCoverEffect();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvProgress.setText(TimeUtil.formatMs(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!bound) return;
                int pos = seekBar.getProgress();
                // 用户拖动进度条松手后，把毫秒位置交给 Service 中的 MediaPlayer seekTo。
                playerService.seekTo(pos);
            }
        });
    }

    private void renderSong(int id) {
        List<Song> list = SongRepository.getPlayableSongList();
        Song found = null;
        // 用 id 查找歌曲，保证从首页、专区、下载页进入播放器时都走同一套渲染逻辑。
        for (Song s : list) {
            if (s.getId() == id) {
                found = s;
                break;
            }
        }
        if (found == null && !list.isEmpty()) found = list.get(0);
        currentSong = found;
        if (currentSong == null) return;

        tvTitle.setText(currentSong.getTitle());
        tvType.setText(currentSong.getType());

        if (ivCover != null) {
            // 切歌时先恢复原图，避免上一首歌的灰度滤镜或旋转残留到下一首。
            ivCover.setImageResource(currentSong.getCoverResId());
            coverEffectMode = EFFECT_NORMAL;
            applyCoverEffect();
        }

        // 歌词文件放在 assets 中，和音频一样属于本地资源读取知识点。
        tvLyric.setText(AssetUtil.readAssetText(this, currentSong.getAssetLyricPath()));

        // 根据本地文件是否存在切换按钮文案，方便演示“下载后可在下载页播放”。
        btnDownload.setText(SongRepository.isDownloaded(this, currentSong)
                ? "已下载（下载页可播放）"
                : "下载到本地（下载页可播放）");
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public static int nextCoverEffectMode(int currentMode) {
        if (currentMode == EFFECT_NORMAL) return EFFECT_ROTATE;
        if (currentMode == EFFECT_ROTATE) return EFFECT_GRAY_SCALE;
        return EFFECT_NORMAL;
    }

    private void applyCoverEffect() {
        if (ivCover == null || btnCoverEffect == null) return;
        // 每次应用新特效前先清空旧动画和滤镜，避免多种效果叠在一起难以讲清。
        ivCover.animate().cancel();
        ivCover.clearColorFilter();
        ivCover.setRotation(0f);
        ivCover.setScaleX(1f);
        ivCover.setScaleY(1f);

        if (coverEffectMode == EFFECT_ROTATE) {
            btnCoverEffect.setText("封面特效：旋转");
            ivCover.animate()
                    .rotationBy(360f)
                    .setDuration(1600)
                    .start();
        } else if (coverEffectMode == EFFECT_GRAY_SCALE) {
            btnCoverEffect.setText("封面特效：灰度放大");
            // ColorMatrix 把饱和度设为 0，就是常见的“图片灰度化”处理。
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0f);
            ivCover.setColorFilter(new ColorMatrixColorFilter(matrix));
            ivCover.animate()
                    .scaleX(1.12f)
                    .scaleY(1.12f)
                    .setDuration(220)
                    .start();
        } else {
            btnCoverEffect.setText("封面特效：原图");
        }
    }

    private static void startForegroundServiceCompat(Context context, Intent intent) {
        // Android 8 以后启动前台服务要用 startForegroundService，低版本继续用 startService。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (bound) {
            unbindService(connection);
            bound = false;
        }
    }
}
