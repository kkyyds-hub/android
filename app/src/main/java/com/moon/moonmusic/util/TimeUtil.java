package com.moon.moonmusic.util;

import java.util.Locale;

public class TimeUtil {

    public static String formatMs(int ms) {
        if (ms < 0) ms = 0;
        int totalSeconds = ms / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
