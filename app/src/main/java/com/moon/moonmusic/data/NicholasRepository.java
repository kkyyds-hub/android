package com.moon.moonmusic.data;

import java.util.Arrays;
import java.util.List;

/**
 * 谢霆锋专区的静态骨架数据。
 * 后续模块会逐步替换为真实图片、可播放音频、网络推荐和定位推荐。
 */
public class NicholasRepository {

    public static List<String> getAlbumTitles() {
        return Arrays.asList(
                "了解",
                "谢谢你的爱1999",
                "Viva",
                "玉蝴蝶",
                "Me"
        );
    }

    public static List<String> getRecommendedSongTitles() {
        return Arrays.asList(
                "因为爱所以爱",
                "谢谢你的爱1999",
                "黄种人",
                "活着Viva",
                "玉蝴蝶"
        );
    }

    public static String getRecommendationIntro() {
        return "从线上曲库挑一首，再让所在城市给歌单换个角度。";
    }

    public static String getAlbumWallIntro() {
        return "从早期国语专辑到 Viva Live 前后的粤语作品，顺着封面听见不同阶段的谢霆锋。";
    }
}
