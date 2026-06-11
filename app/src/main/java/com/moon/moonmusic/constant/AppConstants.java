package com.moon.moonmusic.constant;

/**
 * 全局常量（Action / Extra / Channel）。
 * 期末项目演示：集中管理字符串，避免散落在各处。
 */
public class AppConstants {

    private AppConstants() {}

    // ===== PlayerService actions =====
    public static final String ACTION_SET_SONG = "com.moon.moonmusic.action.SET_SONG";
    public static final String ACTION_PLAY_PAUSE = "com.moon.moonmusic.action.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.moon.moonmusic.action.NEXT";
    public static final String ACTION_PREV = "com.moon.moonmusic.action.PREV";
    public static final String ACTION_SEEK_TO = "com.moon.moonmusic.action.SEEK_TO";
    public static final String ACTION_STOP = "com.moon.moonmusic.action.STOP";

    // ===== DownloadService actions =====
    public static final String ACTION_DOWNLOAD = "com.moon.moonmusic.action.DOWNLOAD";

    // ===== Broadcast actions =====
    public static final String ACTION_DOWNLOAD_DONE = "com.moon.moonmusic.broadcast.DOWNLOAD_DONE";
    public static final String ACTION_DOWNLOAD_FAILED = "com.moon.moonmusic.broadcast.DOWNLOAD_FAILED";
    public static final String ACTION_FAVORITE_CHANGED = "com.moon.moonmusic.broadcast.FAVORITE_CHANGED";

    // ===== Extras =====
    public static final String EXTRA_SONG_ID = "extra_song_id";
    public static final String EXTRA_FROM_DOWNLOAD = "extra_from_download";
    public static final String EXTRA_SEEK_TO_MS = "extra_seek_to_ms";
    public static final String EXTRA_MESSAGE = "extra_message";

    // ===== Notification =====
    public static final String NOTI_CHANNEL_ID = "moon_music_playback";
    public static final int NOTI_ID = 2201;
}
