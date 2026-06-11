package com.moon.moonmusic.constant;

/**
 * 集中管理跨组件使用的动作、传参键和通知常量。
 */
public class AppConstants {

    private AppConstants() {}

    // 播放服务相关动作。
    public static final String ACTION_SET_SONG = "com.moon.moonmusic.action.SET_SONG";
    public static final String ACTION_PLAY_PAUSE = "com.moon.moonmusic.action.PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.moon.moonmusic.action.NEXT";
    public static final String ACTION_PREV = "com.moon.moonmusic.action.PREV";
    public static final String ACTION_SEEK_TO = "com.moon.moonmusic.action.SEEK_TO";
    public static final String ACTION_STOP = "com.moon.moonmusic.action.STOP";

    // 下载服务相关动作。
    public static final String ACTION_DOWNLOAD = "com.moon.moonmusic.action.DOWNLOAD";

    // 页面之间通知状态变化时使用的广播动作。
    public static final String ACTION_DOWNLOAD_DONE = "com.moon.moonmusic.broadcast.DOWNLOAD_DONE";
    public static final String ACTION_DOWNLOAD_FAILED = "com.moon.moonmusic.broadcast.DOWNLOAD_FAILED";
    public static final String ACTION_FAVORITE_CHANGED = "com.moon.moonmusic.broadcast.FAVORITE_CHANGED";

    // Intent 中传递数据时使用的键。
    public static final String EXTRA_SONG_ID = "extra_song_id";
    public static final String EXTRA_FROM_DOWNLOAD = "extra_from_download";
    public static final String EXTRA_SEEK_TO_MS = "extra_seek_to_ms";
    public static final String EXTRA_MESSAGE = "extra_message";

    // 播放通知使用的通道和通知编号。
    public static final String NOTI_CHANNEL_ID = "moon_music_playback";
    public static final int NOTI_ID = 2201;
}
