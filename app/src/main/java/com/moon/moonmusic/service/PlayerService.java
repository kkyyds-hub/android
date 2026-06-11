package com.moon.moonmusic.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.moon.moonmusic.R;
import com.moon.moonmusic.constant.AppConstants;
import com.moon.moonmusic.data.SongRepository;
import com.moon.moonmusic.model.Song;
import com.moon.moonmusic.receiver.PlaybackActionReceiver;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 播放服务（前台服务）：
 * - MediaPlayer 在 Service 中统一管理，Activity 通过 bind + 调用控制。
 * - 适配 Android 13+ 的后台限制：用前台通知维持播放。
 */
public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }

    private final IBinder binder = new PlayerBinder();

    private MediaPlayer mediaPlayer;
    private List<Song> playlist;
    private int currentIndex = 0;
    private boolean currentFromDownload = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Service 创建时准备播放列表、播放器对象和通知通道。
     * Activity 绑定服务后会复用这里初始化出的 MediaPlayer。
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Service 创建时先准备播放列表和 MediaPlayer，后续多个 Activity 入口都能复用同一套播放逻辑。
        playlist = SongRepository.getPlayableSongList();
        ensurePlayer();
        createNotificationChannelIfNeeded();
    }

    /**
     * 接收页面、通知栏或广播转发过来的播放命令。
     * 前台通知会先启动起来，避免前台服务启动后没有及时显示通知。
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 兼容 Android 8+ 的 startForegroundService：无论当前是否播放，都先立刻拉起前台通知
        //（后续根据播放状态更新文案/ongoing，避免 5 秒内未 startForeground 的异常）
        startForeground(AppConstants.NOTI_ID, buildNotification());

        if (intent != null && intent.getAction() != null) {
            handleAction(intent);
        }
        return START_STICKY;
    }

    /**
     * 根据 Intent action 分发播放命令。
     * 播放页按钮和通知栏按钮最终都会转成这里能识别的动作。
     */
    private void handleAction(Intent intent) {
        String action = intent.getAction();
        // 通知栏按钮、播放器按钮、页面跳转都转成这些 action，Service 只需要集中处理播放命令。
        if (AppConstants.ACTION_SET_SONG.equals(action)) {
            int songId = intent.getIntExtra(AppConstants.EXTRA_SONG_ID, 1);
            boolean fromDownload = intent.getBooleanExtra(AppConstants.EXTRA_FROM_DOWNLOAD, false);
            setSongById(songId, fromDownload, true);
        } else if (AppConstants.ACTION_PLAY_PAUSE.equals(action)) {
            togglePlayPause();
        } else if (AppConstants.ACTION_NEXT.equals(action)) {
            next();
        } else if (AppConstants.ACTION_PREV.equals(action)) {
            prev();
        } else if (AppConstants.ACTION_SEEK_TO.equals(action)) {
            int ms = intent.getIntExtra(AppConstants.EXTRA_SEEK_TO_MS, 0);
            seekTo(ms);
        } else if (AppConstants.ACTION_STOP.equals(action)) {
            stopPlayback();
            stopForeground(true);
            stopSelf();
        }
    }

    /**
     * 确保 MediaPlayer 已经创建，并设置播放完成监听。
     * 其它播放控制方法调用前会先走这里，避免空对象导致播放失败。
     */
    private void ensurePlayer() {
        if (mediaPlayer == null) {
            // MediaPlayer 是音频播放核心对象，放在 Service 中可以让离开播放器页面后音乐继续播放。
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    public Song getCurrentSong() {
        if (playlist == null || playlist.isEmpty()) return null;
        if (currentIndex < 0 || currentIndex >= playlist.size()) currentIndex = 0;
        return playlist.get(currentIndex);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getDuration() {
        try {
            return mediaPlayer == null ? 0 : mediaPlayer.getDuration();
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    public int getCurrentPosition() {
        try {
            return mediaPlayer == null ? 0 : mediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            return 0;
        }
    }

    /**
     * 跳转到指定播放进度。
     * PlayerActivity 的进度条松手后会把毫秒值传到这里。
     */
    public void seekTo(int ms) {
        try {
            // SeekBar 拖动后的毫秒位置最终会走到这里，交给 MediaPlayer 跳转播放进度。
            if (mediaPlayer != null) mediaPlayer.seekTo(ms);
        } catch (IllegalStateException ignored) {
        }
    }

    /**
     * 在播放和暂停之间切换。
     * 播放页按钮和通知栏按钮都会调用这个方法，执行后会同步更新前台通知。
     */
    public void togglePlayPause() {
        if (mediaPlayer == null) return;
        try {
            // 同一个按钮控制播放/暂停：根据 MediaPlayer 当前状态决定下一步操作。
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
            updateForegroundNotification();
        } catch (IllegalStateException ignored) {
        }
    }

    public void play() {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.start();
            updateForegroundNotification();
        } catch (IllegalStateException ignored) {
        }
    }

    public void pause() {
        if (mediaPlayer == null) return;
        try {
            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
            updateForegroundNotification();
        } catch (IllegalStateException ignored) {
        }
    }

    /**
     * 切到播放列表中的下一首。
     * 播放完成回调和下一首按钮都会复用这个方法。
     */
    public void next() {
        if (playlist == null || playlist.isEmpty()) return;
        // 取模让最后一首的下一首回到第一首，形成简单循环播放列表。
        currentIndex = (currentIndex + 1) % playlist.size();
        setSongInternal(getCurrentSong(), currentFromDownload, true);
    }

    /**
     * 切到播放列表中的上一首。
     * 当前已经是第一首时会回到列表最后一首，保持循环列表体验。
     */
    public void prev() {
        if (playlist == null || playlist.isEmpty()) return;
        // 加上 playlist.size() 再取模，避免第一首点上一首时出现负数下标。
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        setSongInternal(getCurrentSong(), currentFromDownload, true);
    }

    /**
     * 根据歌曲编号定位播放列表中的歌曲。
     * 首页、专区和下载页进入播放器时，都会通过歌曲 id 让服务切到对应音频。
     */
    public void setSongById(int songId, boolean fromDownload, boolean autoPlay) {
        if (playlist == null || playlist.isEmpty()) return;
        for (int i = 0; i < playlist.size(); i++) {
            if (playlist.get(i).getId() == songId) {
                currentIndex = i;
                break;
            }
        }
        setSongInternal(getCurrentSong(), fromDownload, autoPlay);
    }

    /**
     * 切换 MediaPlayer 的数据源并按需开始播放。
     * 如果从下载页进入且本地文件存在，就优先播放下载文件，否则回退到 assets 音频。
     */
    private void setSongInternal(Song song, boolean fromDownload, boolean autoPlay) {
        if (song == null) return;
        ensurePlayer();
        currentFromDownload = fromDownload;
        try {
            // 切歌前必须 reset，否则旧数据源还在 MediaPlayer 里，新音频无法正常 prepare。
            mediaPlayer.reset();

            boolean dataSourceSet = false;
            if (fromDownload) {
                File dir = SongRepository.getDownloadDir(this);
                if (dir != null) {
                    File f = new File(dir, "song" + song.getId() + ".mp3");
                    if (f.exists()) {
                        // 下载页进入播放器时优先播放已保存的 mp3，体现本地文件播放流程。
                        mediaPlayer.setDataSource(f.getAbsolutePath());
                        dataSourceSet = true;
                    }
                }
            }

            // 如果没有下载文件，就回退到 assets，保证未下载的歌曲也能正常播放。
            if (!dataSourceSet) {
                AssetFileDescriptor afd = getAssets().openFd(song.getAssetMusicPath());
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }

            // prepare 会读取音频元信息；音频来自本地资源，同步准备即可。
            mediaPlayer.prepare();
            if (autoPlay) {
                mediaPlayer.start();
            }
            updateForegroundNotification();
        } catch (IOException e) {
            // 准备失败：停止播放
            stopPlayback();
        }
    }

    public void stopPlayback() {
        if (mediaPlayer == null) return;
        try {
            mediaPlayer.stop();
        } catch (IllegalStateException ignored) {
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 一首歌播放结束后自动下一首，和 next() 复用同一段切歌逻辑。
        next();
    }

    /**
     * Service 销毁时释放 MediaPlayer。
     * 这里是音频资源回收的位置，避免页面退出后仍占用解码器和文件句柄。
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                // Service 销毁时释放播放器资源，避免音频解码器和文件句柄一直占用。
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    /**
     * 创建播放通知需要使用的通知通道。
     * Android 8 及以上必须先有通道，前台通知才能正常显示。
     */
    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8 以后通知必须属于某个 Channel，前台播放通知也不例外。
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    AppConstants.NOTI_CHANNEL_ID,
                    "MOON MUSIC 播放",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("播放音乐时的前台通知");
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private void updateForegroundNotification() {
        // 只有播放时才拉起前台；暂停也保持通知（避免被系统杀）
        startForeground(AppConstants.NOTI_ID, buildNotification());
    }

    /**
     * 构建播放中的前台通知。
     * 通知里的上一首、播放暂停、下一首和停止按钮会通过广播回到 PlayerService。
     */
    private Notification buildNotification() {
        Song song = getCurrentSong();
        String title = song == null ? "MOON MUSIC" : song.getTitle();
        String text = song == null ? "正在播放" : (song.getArtist() + " · " + song.getAlbum());

        // 通知栏四个按钮通过 PendingIntent 发广播，再由 BroadcastReceiver 转成 Service action。
        PendingIntent piPrev = PlaybackActionReceiver.pendingIntent(this, AppConstants.ACTION_PREV);
        PendingIntent piPlayPause = PlaybackActionReceiver.pendingIntent(this, AppConstants.ACTION_PLAY_PAUSE);
        PendingIntent piNext = PlaybackActionReceiver.pendingIntent(this, AppConstants.ACTION_NEXT);
        PendingIntent piStop = PlaybackActionReceiver.pendingIntent(this, AppConstants.ACTION_STOP);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this, AppConstants.NOTI_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(isPlaying())
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_nav_home, "上一首", piPrev)
                .addAction(R.drawable.ic_heart, isPlaying() ? "暂停" : "播放", piPlayPause)
                // 项目已移除“发现”页图标，通知栏这里复用已有图标即可。
                .addAction(R.drawable.ic_nav_home, "下一首", piNext)
                .addAction(R.drawable.ic_nav_my, "停止", piStop)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // 点击通知回到播放器，方便用户从后台通知返回播放页面。
        Intent it = new Intent(this, com.moon.moonmusic.ui.PlayerActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentPi = PendingIntent.getActivity(
                this,
                991,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
        b.setContentIntent(contentPi);

        return b.build();
    }
}
