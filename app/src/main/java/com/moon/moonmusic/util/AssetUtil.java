package com.moon.moonmusic.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetUtil {

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
            sb.append("（未找到歌词文件：").append(assetPath).append("）");
        }
        return sb.toString();
    }
}
