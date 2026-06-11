package com.moon.moonmusic.data;

import android.content.Context;

import com.moon.moonmusic.R;
import com.moon.moonmusic.model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 歌曲数据仓库：集中维护歌曲名称、封面资源、音频 assets 路径和歌词 assets 路径。
 * 这样列表页、播放器、下载服务都从同一个地方取数据，减少多处维护同一份歌曲信息。
 */
public class SongRepository {

    // 推荐下载（有音频文件，可播放）。
    public static List<Song> getDownloadSongList() {
        List<Song> list = new ArrayList<>();
        list.add(new Song(
                1,
                "那些花儿",
                "朴树",
                "我去2000年",
                "民谣",
                R.drawable.cover_pushu,
                "music/song1.mp3",
                "lyrics/song1.txt"
        ));
        list.add(new Song(
                2,
                "蝴蝶",
                "王菲",
                "只爱陌生人",
                "流行",
                R.drawable.cover_wangfei,
                "music/song2.mp3",
                "lyrics/song2.txt"
        ));
        list.add(new Song(
                3,
                "小城大事",
                "杨千嬅",
                "Miriam",
                "流行",
                R.drawable.cover_miriam,
                "music/song3.mp3",
                "lyrics/song3.txt"
        ));
        list.add(new Song(
                4,
                "White Album",
                "小木曾雪菜",
                "TVアニメ「WHITE ALBUM2」",
                "日本ACG",
                R.drawable.cover_xiaomu,
                "music/song2.mp3",
                "lyrics/song2.txt"
        ));
        list.add(new Song(
                5,
                "灯トウイライト",
                "平野绫",
                "WHITE ALBUM サウンドステージ",
                "日本ACG",
                R.drawable.cover_pingye,
                "music/song2.mp3",
                "lyrics/song2.txt"
        ));
        list.add(new Song(
                6,
                "素敵だね",
                "Rikki (中野律纪)",
                "FINAL FANTASY X ",
                "日本ACG",
                R.drawable.cover_ff10,
                "music/song2.mp3",
                "lyrics/song2.txt"
        ));
        list.add(new Song(
                7,
                "月光爱人",
                "CoCo李玟",
                "Crouching Tiger, Hidden Dragon ",
                "流行",
                R.drawable.cover_coco,
                "music/song2.mp3",
                "lyrics/song2.txt"
        ));
        list.add(new Song(
                8,
                "dont break my heart",
                "窦唯",
                "黑豹 同名专辑",
                "摇滚",
                R.drawable.cover_heibao,
                "music/song2.mp3",
                "lyrics/song2.txt"
        ));
        return list;
    }

    // 喜欢列表（按文档 5 首命名；此处只做展示，不强制要求本地音频）。
    public static List<Song> getFavoriteList() {
        List<Song> list = new ArrayList<>();
        // 说明：喜欢列表只做 UI 展示（未强制提供音频文件）。
        list.add(new Song(101, "蔷薇", "萧亚轩", "Elva", "喜欢",
                R.drawable.cover_fav_elva, "", ""));
        list.add(new Song(102, "我只在乎你", "邓丽君", "我只在乎你", "喜欢",
                R.drawable.cover_fav_teresa, "", ""));
        list.add(new Song(103, "冷酷到底", "五月天", "冷酷到底", "喜欢",
                R.drawable.cover_fav_cool, "", ""));
        // 注意："幸福了 然后呢" 为一首歌（不是拆成“幸福了”“然后呢”两首）
        list.add(new Song(104, "幸福了 然后呢", "A-Lin", "幸福了 然后呢", "喜欢",
                R.drawable.cover_fav_alin, "", ""));
        return list;
    }

    // 谢霆锋专区歌单：使用公开试听片段作为可播放音频。
    public static List<Song> getNicholasSongList() {
        List<Song> list = new ArrayList<>();
        list.add(new Song(
                201,
                "因为爱所以爱",
                "谢霆锋",
                "了解",
                "公开试听",
                R.drawable.nicholas_album_understand,
                "music/nicholas/because_love.m4a",
                "lyrics/nicholas/because_love.txt"
        ));
        list.add(new Song(
                202,
                "谢谢你的爱1999",
                "谢霆锋",
                "谢谢你的爱1999",
                "公开试听",
                R.drawable.nicholas_album_thanks_1999,
                "music/nicholas/thanks_1999.m4a",
                "lyrics/nicholas/thanks_1999.txt"
        ));
        list.add(new Song(
                203,
                "黄种人",
                "谢霆锋",
                "黄·锋",
                "公开试听",
                R.drawable.nicholas_album_viva,
                "music/nicholas/huang_zhong_ren.m4a",
                "lyrics/nicholas/huang_zhong_ren.txt"
        ));
        list.add(new Song(
                204,
                "活着Viva",
                "谢霆锋",
                "Viva",
                "公开试听",
                R.drawable.nicholas_album_viva,
                "music/nicholas/viva.m4a",
                "lyrics/nicholas/viva.txt"
        ));
        list.add(new Song(
                205,
                "玉蝴蝶",
                "谢霆锋",
                "玉蝴蝶",
                "公开试听",
                R.drawable.nicholas_album_jade_butterfly,
                "music/nicholas/jade_butterfly.m4a",
                "lyrics/nicholas/jade_butterfly.txt"
        ));
        return list;
    }

    public static List<Song> getPlayableSongList() {
        List<Song> list = new ArrayList<>();
        // 播放器和下载服务只使用“可播放列表”，避免拿到没有音频路径的收藏展示数据。
        list.addAll(getDownloadSongList());
        list.addAll(getNicholasSongList());
        return list;
    }

    public static File getDownloadDir(Context context) {
        // App 专属外部目录，无需存储权限，卸载应用时这些下载文件也会被清理。
        File dir = context.getExternalFilesDir("moon_downloads");
        if (dir != null && !dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

    public static boolean isDownloaded(Context context, Song song) {
        File dir = getDownloadDir(context);
        if (dir == null) return false;
        // 下载后保存为：song<ID>.mp3，和 DownloadService 中的写入规则保持一致。
        File f = new File(dir, "song" + song.getId() + ".mp3");
        return f.exists();
    }
}
