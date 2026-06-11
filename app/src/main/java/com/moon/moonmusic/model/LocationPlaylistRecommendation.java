package com.moon.moonmusic.model;

public class LocationPlaylistRecommendation {

    private final String tag;
    private final String title;
    private final String reason;
    private final String source;
    private final int primarySongId;
    private final boolean fromLocation;

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
