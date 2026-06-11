package com.moon.moonmusic.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.moon.moonmusic.constant.AppConstants;
import com.moon.moonmusic.data.SongRepository;
import com.moon.moonmusic.model.Song;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 下载服务：演示“后台任务 + 本地文件保存 + 广播通知”。
 * 这里的下载不是访问网络，而是把 assets 中的音频和歌词复制到 App 专属目录，
 * 这样下载页和播放器可以用同一套歌曲 id 读取本地文件。
 */
public class DownloadService extends Service {

    // 单线程执行复制任务，避免多个下载同时写文件造成顺序混乱。
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (!AppConstants.ACTION_DOWNLOAD.equals(action)) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        final int songId = intent.getIntExtra(AppConstants.EXTRA_SONG_ID, 1);

        executor.execute(() -> {
            String msg;
            boolean ok = downloadSong(songId);
            if (ok) {
                msg = "下载完成";
                // 下载完成后发应用内广播，下载列表页面收到后刷新数据。
                Intent bc = new Intent(AppConstants.ACTION_DOWNLOAD_DONE);
                bc.setPackage(getPackageName());
                bc.putExtra(AppConstants.EXTRA_SONG_ID, songId);
                sendBroadcast(bc);
            } else {
                msg = "下载失败";
                // 失败也发广播，方便页面给出提示；这里没有直接操作 UI，因为 Service 没有界面。
                Intent bc = new Intent(AppConstants.ACTION_DOWNLOAD_FAILED);
                bc.setPackage(getPackageName());
                bc.putExtra(AppConstants.EXTRA_SONG_ID, songId);
                bc.putExtra(AppConstants.EXTRA_MESSAGE, msg);
                sendBroadcast(bc);
            }
            stopSelf(startId);
        });

        return START_NOT_STICKY;
    }

    private boolean downloadSong(int songId) {
        List<Song> list = SongRepository.getPlayableSongList();
        Song target = null;
        for (Song s : list) {
            if (s.getId() == songId) {
                target = s;
                break;
            }
        }
        if (target == null) return false;

        File dir = SongRepository.getDownloadDir(this);
        if (dir == null) return false;

        // 目标文件名固定为 song<ID>.mp3 / song<ID>.txt，播放器就能按歌曲 id 直接查找。
        File mp3 = new File(dir, "song" + target.getId() + ".mp3");
        File lrc = new File(dir, "song" + target.getId() + ".txt");

        try {
            copyAssetToFile(target.getAssetMusicPath(), mp3);
            copyAssetToFile(target.getAssetLyricPath(), lrc);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void copyAssetToFile(String assetPath, File outFile) throws IOException {
        if (assetPath == null || assetPath.isEmpty()) return;
        try (InputStream in = getAssets().open(assetPath);
             FileOutputStream out = new FileOutputStream(outFile)) {
            // 用缓冲区一段一段复制，适合音频这种二进制文件，也不会一次占用太多内存。
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
        }
    }
}
