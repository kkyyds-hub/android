package com.moon.moonmusic.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Android assets 目录文件读取工具。
 * 项目中的歌词文本存放在 assets/lyrics 下，本工具按行读取并拼接为字符串。
 * 当前主要被 PlayerActivity 用来加载当前播放歌曲的歌词并展示在页面上。
 */
public class AssetUtil {

    /**
     * 从 assets 目录读取文本文件内容。
     * 按行读取并保留换行，适合歌词这类需要保留原始排版的文本。
     * 文件不存在时返回友好提示，避免页面因 IO 异常而崩溃。
     *
     * @param context   用于访问 assets 的上下文
     * @param assetPath assets 中的相对路径，如 "lyrics/song1.txt"
     * @return 文件完整文本内容
     */
    public static String readAssetText(Context context, String assetPath) {
        if (assetPath == null || assetPath.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        try (InputStream in = context.getAssets().open(assetPath);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            // 歌词文件缺失时不崩溃，返回提示文本让用户知道当前歌曲没有配套歌词。
            sb.append("（未找到歌词文件：").append(assetPath).append("）");
        }
        return sb.toString();
    }
}
