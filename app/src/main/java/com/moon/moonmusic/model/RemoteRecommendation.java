package com.moon.moonmusic.model;

/**
 * 远程推荐数据模型。
 * NicholasFragment 通过网络请求获取 iTunes 或本地兜底的谢霆锋歌曲推荐，
 * 包含标题、推荐理由、标签、来源说明和封面信息。
 * 网络不可用时 RecommendationRepository 会回退到本地默认推荐，此时 fromNetwork 为 false。
 */
public class RemoteRecommendation {
    private final String title;
    private final String reason;
    private final String tag;
    private final String source;
    private final boolean fromNetwork;     // 是否来自网络请求
    private final String artworkUrl;       // iTunes 封面图片地址
    private final String localArtworkName; // 本地兜底封面图片的 drawable 名称

    /** 无封面信息的简便构造，本地兜底推荐使用。 */
    public RemoteRecommendation(String title, String reason, String tag, String source, boolean fromNetwork) {
        this(title, reason, tag, source, fromNetwork, "", "");
    }

    public RemoteRecommendation(String title, String reason, String tag, String source, boolean fromNetwork, String artworkUrl, String localArtworkName) {
        this.title = title;
        this.reason = reason;
        this.tag = tag;
        this.source = source;
        this.fromNetwork = fromNetwork;
        this.artworkUrl = artworkUrl;
        this.localArtworkName = localArtworkName;
    }

    public String getTitle() {
        return title;
    }

    public String getReason() {
        return reason;
    }

    public String getTag() {
        return tag;
    }

    public String getSource() {
        return source;
    }

    public boolean isFromNetwork() {
        return fromNetwork;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public String getLocalArtworkName() {
        return localArtworkName;
    }
}
