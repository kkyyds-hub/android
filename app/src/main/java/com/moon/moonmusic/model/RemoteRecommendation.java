package com.moon.moonmusic.model;

public class RemoteRecommendation {
    private final String title;
    private final String reason;
    private final String tag;
    private final String source;
    private final boolean fromNetwork;
    private final String artworkUrl;
    private final String localArtworkName;

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
