package com.moon.moonmusic.model;

/**
 * 定位歌单推荐数据模型。
 * NicholasFragment 根据设备位置（定位成功时用真实城市，失败时回退到默认城市"香港"）
 * 生成推荐结果，每一项包含标签、歌单标题、推荐理由、来源说明和关联歌曲 id。
 */
public class LocationPlaylistRecommendation {

    private final String tag;          // 标签，如 "感知定位 · 香港"
    private final String title;        // 歌单标题
    private final String reason;       // 推荐理由
    private final String source;       // 数据来源说明
    private final int primarySongId;   // 关联的歌曲编号，点击后可跳转播放器
    private final boolean fromLocation; // 是否来自真实定位（false 表示使用了默认城市兜底）

    public LocationPlaylistRecommendation(String tag, String title, String reason, String source, int primarySongId, boolean fromLocation) {
        this.tag = tag;
        this.title = title;
        this.reason = reason;
        this.source = source;
        this.primarySongId = primarySongId;
        this.fromLocation = fromLocation;
    }

    public String getTag() {
        return tag;
    }

    public String getTitle() {
        return title;
    }

    public String getReason() {
        return reason;
    }

    public String getSource() {
        return source;
    }

    public int getPrimarySongId() {
        return primarySongId;
    }

    public boolean isFromLocation() {
        return fromLocation;
    }
}
