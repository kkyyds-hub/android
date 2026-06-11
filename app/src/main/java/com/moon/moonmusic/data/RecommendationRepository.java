package com.moon.moonmusic.data;

import com.moon.moonmusic.model.RemoteRecommendation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 谢霆锋专区网络推荐数据源。
 * 使用 HttpURLConnection 访问 iTunes Search API，无网络或接口异常时回退到本地推荐。
 */
public class RecommendationRepository {

    private static final String RECOMMEND_URL =
            "https://itunes.apple.com/search?term=Nicholas%20Tse&entity=song&limit=10";
    private static final RemoteRecommendation[] FALLBACK_RECOMMENDATIONS = new RemoteRecommendation[]{
            new RemoteRecommendation(
                    "今日推荐：因为爱所以爱",
                    "这首歌旋律直接、情绪明亮，适合作为谢霆锋主题歌单的入口。",
                    "本地精选",
                    "Moon Music",
                    false,
                    "",
                    "nicholas_album_understand"
            ),
            new RemoteRecommendation(
                    "今日推荐：谢谢你的爱1999",
                    "早期流行摇滚代表作，适合从更青涩也更直接的谢霆锋开始听起。",
                    "本地精选",
                    "Moon Music",
                    false,
                    "",
                    "nicholas_album_thanks_1999"
            ),
            new RemoteRecommendation(
                    "今日推荐：黄种人",
                    "节奏更硬朗、舞台感更强，适合想听能量型作品的时候播放。",
                    "本地精选",
                    "Moon Music",
                    false,
                    "",
                    "nicholas_album_me"
            ),
            new RemoteRecommendation(
                    "今日推荐：玉蝴蝶",
                    "旋律辨识度高，能听到谢霆锋粤语作品里更细腻的一面。",
                    "本地精选",
                    "Moon Music",
                    false,
                    "",
                    "nicholas_album_jade_butterfly"
            )
    };
    private static int recommendationCursor = 0;
    private static int fallbackCursor = 0;

    public interface Callback {
        void onResult(RemoteRecommendation recommendation);
    }

    /**
     * 异步加载今日推荐。
     * NicholasFragment 调用后会通过回调拿到推荐对象，再切回主线程刷新页面。
     */
    public static void loadTodayRecommendation(Callback callback) {
        // 网络请求不能放在主线程，否则 Android 会抛异常并卡住界面；这里开子线程请求 iTunes。
        new Thread(() -> {
            RemoteRecommendation recommendation;
            try {
                String json = requestJson(RECOMMEND_URL);
                recommendation = parseRecommendation(json, nextRecommendationIndex());
            } catch (Exception e) {
                // 网络不稳定时不能让页面空着，失败后回退到本地推荐数据。
                recommendation = getFallbackRecommendation();
            }
            if (callback != null) {
                callback.onResult(recommendation);
            }
        }).start();
    }

    public static RemoteRecommendation parseRecommendation(String json) throws Exception {
        return parseRecommendation(json, 0);
    }

    /**
     * 把接口返回的 JSON 解析成页面需要的推荐对象。
     * preferredIndex 用来从结果列表中选择不同歌曲，接口数据缺字段时会补默认值。
     */
    public static RemoteRecommendation parseRecommendation(String json, int preferredIndex) throws Exception {
        JSONObject obj = new JSONObject(json);
        JSONArray results = obj.optJSONArray("results");
        if (results == null || results.length() == 0) {
            return getFallbackRecommendation();
        }

        // preferredIndex 会递增，点击“换一首”时就能从返回列表中轮流取不同歌曲。
        JSONObject selected = results.getJSONObject(normalizeIndex(preferredIndex, results.length()));
        String trackName = optClean(selected, "trackName");
        String collectionName = optClean(selected, "collectionName");
        String artistName = optClean(selected, "artistName");
        String artworkUrl = upgradeArtworkUrl(optClean(selected, "artworkUrl100"));

        if (trackName.isEmpty()) {
            trackName = "活着Viva";
        }
        if (collectionName.isEmpty()) {
            collectionName = "Viva";
        }
        if (artistName.isEmpty()) {
            artistName = "Nicholas Tse";
        }

        return new RemoteRecommendation(
                "今日推荐：" + trackName,
                artistName + "《" + collectionName + "》里的代表曲目，适合从现场感和旋律感进入今天的锋味歌单。",
                "在线精选",
                "Apple Music Preview",
                true,
                artworkUrl,
                localArtworkNameForTrack(trackName, collectionName)
        );
    }

    /**
     * 获取本地兜底推荐。
     * 网络失败或接口没有返回结果时使用它，游标会递增以轮换不同推荐内容。
     */
    public static RemoteRecommendation getFallbackRecommendation() {
        RemoteRecommendation recommendation = FALLBACK_RECOMMENDATIONS[normalizeIndex(fallbackCursor, FALLBACK_RECOMMENDATIONS.length)];
        fallbackCursor++;
        return recommendation;
    }

    static void resetRecommendationCursorForTest() {
        recommendationCursor = 0;
        fallbackCursor = 0;
    }

    private static int nextRecommendationIndex() {
        int index = recommendationCursor;
        recommendationCursor++;
        return index;
    }

    private static int normalizeIndex(int index, int size) {
        if (size <= 0) return 0;
        int normalized = index % size;
        return normalized < 0 ? normalized + size : normalized;
    }

    /**
     * 使用 HttpURLConnection 请求 JSON 字符串。
     * 方法内部负责设置超时、读取响应体，并在完成后关闭网络连接。
     */
    private static String requestJson(String urlText) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlText);
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方法、超时时间和 Accept 头，避免网络异常时一直等待。
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");

            int code = connection.getResponseCode();
            // 2xx 读正常响应，其它状态读错误流，方便调试时看到服务器返回了什么。
            InputStream is = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            String body = readAll(is);
            if (code < 200 || code >= 300) {
                throw new IllegalStateException("HTTP " + code + ": " + body);
            }
            return body;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 读取接口返回的输入流并拼成完整字符串。
     * requestJson 会把正常响应和错误响应都交给这里统一读取。
     */
    private static String readAll(InputStream inputStream) throws Exception {
        if (inputStream == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            // JSON 是文本数据，按 UTF-8 一行一行读出后再交给 JSONObject 解析。
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString().trim();
    }

    private static String optClean(JSONObject obj, String key) throws Exception {
        Object value = obj.opt(key);
        if (value == null) return "";
        if (value instanceof JSONArray) return value.toString();
        return String.valueOf(value).replace('\n', ' ').trim();
    }

    /**
     * 把接口返回的小尺寸封面地址替换成更适合页面显示的尺寸。
     * 如果接口没有封面地址，调用方会继续使用本地封面。
     */
    private static String upgradeArtworkUrl(String artworkUrl) {
        if (artworkUrl.isEmpty()) return "";
        // iTunes 默认图较小，把 100x100 替换成 300x300，封面显示时会更清晰。
        return artworkUrl.replace("100x100bb.jpg", "300x300bb.jpg");
    }

    private static String localArtworkNameForTrack(String trackName, String collectionName) {
        String text = (trackName + " " + collectionName).toLowerCase();
        if (text.contains("玉蝴蝶")) return "nicholas_album_jade_butterfly";
        if (text.contains("1999") || text.contains("謝謝") || text.contains("谢谢")) return "nicholas_album_thanks_1999";
        if (text.contains("viva") || text.contains("活")) return "nicholas_album_viva";
        if (text.contains("黃種人") || text.contains("黄种人") || text.contains("黃‧鋒") || text.contains("黄‧锋")) {
            return "nicholas_album_me";
        }
        if (text.contains("因為愛") || text.contains("因为爱") || text.contains("了解")) return "nicholas_album_understand";
        return "nicholas_album_viva";
    }
}
