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

public class DownloadService extends Service {

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
                Intent bc = new Intent(AppConstants.ACTION_DOWNLOAD_DONE);
                bc.setPackage(getPackageName());
                bc.putExtra(AppConstants.EXTRA_SONG_ID, songId);
                sendBroadcast(bc);
            } else {
                msg = "下载失败";
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

        // 目标文件名：song<ID>.mp3 / song<ID>.txt
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
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.flush();
        }
    }
}
