package com.moon.moonmusic.model;

/**
 * 歌曲数据模型 —— 项目最核心的数据类。
 * 每首歌包含编号、歌名、歌手、专辑、风格、封面资源 id，以及 assets 中音频和歌词文件的路径。
 * SongRepository 初始化时会构建全部歌曲列表，Adapter、播放器、下载服务都围绕这个模型工作。
 */
public class Song {
    private final int id;
    private final String title;
    private final String artist;
    private final String album;
    private final String type;          // 风格标签，如 "摇滚"、"粤语经典"
    private final int coverResId;       // drawable 中的封面图片资源 id
    private final String assetMusicPath; // assets 中的音频文件路径，如 "music/song1.mp3"
    private final String assetLyricPath; // assets 中的歌词文件路径，如 "lyrics/song1.txt"

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

    /** 返回 "歌手 · 专辑" 格式的副标题，供列表项和播放器信息区统一使用。 */
    public String getSubtitle() {
        return artist + " · " + album;
    }
}
