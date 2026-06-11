package com.moon.moonmusic.data;

import com.moon.moonmusic.model.LocationPlaylistRecommendation;

import java.util.Locale;

public class LocationPlaylistRepository {

    private static final int SONG_BECAUSE_LOVE = 201;
    private static final int SONG_HUANG_ZHONG_REN = 203;
    private static final int SONG_VIVA = 204;
    private static final int SONG_JADE_BUTTERFLY = 205;

    private LocationPlaylistRepository() {
    }

    public static LocationPlaylistRecommendation buildFromLocation(double latitude, double longitude) {
        if (isGreaterBayArea(latitude, longitude)) {
            return new LocationPlaylistRecommendation(
                    "感知定位 · 粤港澳",
                    "粤语摇滚现场歌单",
                    "你的位置接近粤港澳音乐文化圈，推荐从《活着Viva》进入谢霆锋更有冲击力的摇滚现场气质。",
                    formatCoordinateSource(latitude, longitude),
                    SONG_VIVA,
                    true
            );
        }
        if (isBeijingArea(latitude, longitude)) {
            return new LocationPlaylistRecommendation(
                    "感知定位 · 北方城市",
                    "北方摇滚能量歌单",
                    "当前位置接近北京一带，推荐《黄种人》这类节奏明确、舞台表达强烈的作品，适合展示力量感。",
                    formatCoordinateSource(latitude, longitude),
                    SONG_HUANG_ZHONG_REN,
                    true
            );
        }
        if (isYangtzeRiverDelta(latitude, longitude)) {
            return new LocationPlaylistRecommendation(
                    "感知定位 · 都会夜行",
                    "城市夜行歌单",
                    "当前位置接近江浙沪城市群，推荐《因为爱所以爱》作为通勤、夜景和流行摇滚之间的平衡入口。",
                    formatCoordinateSource(latitude, longitude),
                    SONG_BECAUSE_LOVE,
                    true
            );
        }
        return new LocationPlaylistRecommendation(
                "感知定位 · 旅途",
                "旅行路上歌单",
                "根据当前位置生成旅行场景推荐，先听《玉蝴蝶》，再从旋律感切入谢霆锋的粤语作品线索。",
                formatCoordinateSource(latitude, longitude),
                SONG_JADE_BUTTERFLY,
                true
        );
    }

    public static LocationPlaylistRecommendation getDefaultRecommendation() {
        return new LocationPlaylistRecommendation(
                "感知定位 · 默认城市",
                "香港经典入门歌单",
                "暂时没有读取到设备位置，使用默认城市香港生成谢霆锋音乐推荐，适合作为课堂演示兜底。",
                "本地默认推荐",
                SONG_VIVA,
                false
        );
    }

    public static LocationPlaylistRecommendation getPermissionDeniedRecommendation() {
        return new LocationPlaylistRecommendation(
                "感知定位 · 权限未开启",
                "手动体验歌单",
                "定位权限未开启，已切换到本地手动推荐。打开定位权限后可根据当前位置生成场景化歌单。",
                "权限兜底推荐",
                SONG_BECAUSE_LOVE,
                false
        );
    }

    static boolean isGreaterBayArea(double latitude, double longitude) {
        return latitude >= 21.5 && latitude <= 24.2 && longitude >= 112.0 && longitude <= 115.6;
    }

    static boolean isBeijingArea(double latitude, double longitude) {
        return latitude >= 39.2 && latitude <= 41.1 && longitude >= 115.4 && longitude <= 117.6;
    }

    static boolean isYangtzeRiverDelta(double latitude, double longitude) {
        return latitude >= 29.5 && latitude <= 32.8 && longitude >= 119.0 && longitude <= 122.8;
    }

    private static String formatCoordinateSource(double latitude, double longitude) {
        return String.format(Locale.US, "设备最近位置 %.2f, %.2f", latitude, longitude);
    }
}
