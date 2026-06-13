package com.moon.moonmusic.util;

import java.util.Locale;

/**
 * 时间格式化工具。
 * 将 MediaPlayer 的毫秒级播放进度转换为 "MM:SS" 格式，
 * 供 PlayerActivity 中 SeekBar 两侧的时间文本显示使用。
 */
public class TimeUtil {

    /**
     * 毫秒 → "分:秒" 格式化。
     * 负数会被修正为 0，避免 MediaPlayer 未初始化时传入 -1 导致异常。
     */
    public static String formatMs(int ms) {
        if (ms < 0) ms = 0;
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
