package com.moon.moonmusic.model;

public class Song {
    private final int id;
    private final String title;
    private final String artist;
    private final String album;
    private final String type;
    private final int coverResId;
    private final String assetMusicPath;
    private final String assetLyricPath;

    public Song(int id, String title, String artist, String album, String type,
                int coverResId, String assetMusicPath, String assetLyricPath) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.type = type;
        this.coverResId = coverResId;
        this.assetMusicPath = assetMusicPath;
        this.assetLyricPath = assetLyricPath;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getType() { return type; }
    public int getCoverResId() { return coverResId; }
    public String getAssetMusicPath() { return assetMusicPath; }
    public String getAssetLyricPath() { return assetLyricPath; }

    public String getSubtitle() {
        return artist + " · " + album;
    }
}
