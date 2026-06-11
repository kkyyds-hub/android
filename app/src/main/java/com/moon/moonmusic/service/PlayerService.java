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

    @Override
    public void onCreate() {
        super.onCreate();
        playlist = SongRepository.getPlayableSongList();
        ensurePlayer();
        createNotificationChannelIfNeeded();
    }

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

    private void handleAction(Intent intent) {
        String action = intent.getAction();
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

    private void ensurePlayer() {
        if (mediaPlayer == null) {
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

    public void seekTo(int ms) {
        try {
            if (mediaPlayer != null) mediaPlayer.seekTo(ms);
        } catch (IllegalStateException ignored) {
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) return;
        try {
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

    public void next() {
        if (playlist == null || playlist.isEmpty()) return;
        currentIndex = (currentIndex + 1) % playlist.size();
        setSongInternal(getCurrentSong(), currentFromDownload, true);
    }

    public void prev() {
        if (playlist == null || playlist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
        setSongInternal(getCurrentSong(), currentFromDownload, true);
    }

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

    private void setSongInternal(Song song, boolean fromDownload, boolean autoPlay) {
        if (song == null) return;
        ensurePlayer();
        currentFromDownload = fromDownload;
        try {
            mediaPlayer.reset();

            boolean dataSourceSet = false;
            if (fromDownload) {
                File dir = SongRepository.getDownloadDir(this);
                if (dir != null) {
                    File f = new File(dir, "song" + song.getId() + ".mp3");
                    if (f.exists()) {
                        mediaPlayer.setDataSource(f.getAbsolutePath());
                        dataSourceSet = true;
                    }
                }
            }

            // 如果没有下载文件，就回退到 assets
            if (!dataSourceSet) {
                AssetFileDescriptor afd = getAssets().openFd(song.getAssetMusicPath());
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
            }

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
        // 自动下一首（循环）
        next();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    private Notification buildNotification() {
        Song song = getCurrentSong();
        String title = song == null ? "MOON MUSIC" : song.getTitle();
        String text = song == null ? "正在播放" : (song.getArtist() + " · " + song.getAlbum());

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
                // 项目已移除“发现”页图标，通知栏这里复用已有图标即可
                .addAction(R.drawable.ic_nav_home, "下一首", piNext)
                .addAction(R.drawable.ic_nav_my, "停止", piStop)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        // 点击通知回到播放器
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
