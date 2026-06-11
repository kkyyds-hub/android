package com.moon.moonmusic.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.moon.moonmusic.R;

/**
 * 艺人档案页：独立展示谢霆锋的基本信息、音乐经历、代表作品和主要成就。
 * 页面底部嵌入本地 H5，用 WebView 加载 assets 网页资源。
 */
public class RecommendationDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "recommend_title";
    public static final String EXTRA_REASON = "recommend_reason";
    public static final String EXTRA_SOURCE = "recommend_source";
    public static final String EXTRA_TAG = "recommend_tag";
    public static final String PAGE_TITLE = "艺人档案";
    public static final String ARTIST_NAME = "谢霆锋";
    public static final String ARTIST_EN_NAME = "Nicholas Tse";
    public static final String BIRTHDAY_AND_PLACE = "1980.08.29 · 香港";
    public static final String REPRESENTATIVE_WORKS = "因为爱所以爱 · 谢谢你的爱1999 · 活着Viva · 玉蝴蝶 · 黄种人";
    public static final String ACHIEVEMENT = "2011 年凭电影《线人》获得第 30 届香港电影金像奖最佳男主角。";
    public static final String MUSIC_GUIDE_TITLE = "更多音乐资料";
    public static final String H5_ASSET_URL = "file:///android_asset/h5/nicholas_tse.html";

    private ImageButton btnBack;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendation_detail);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        btnBack = findViewById(R.id.btn_detail_back);
        webView = findViewById(R.id.wv_artist_material);
    }

    private void initData() {
        initWebView();
    }

    private void initListener() {
        btnBack.setOnClickListener(v -> finish());
    }

    public static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings settings = webView.getSettings();
        // 这里加载 assets/h5 下的本地网页，不依赖外网也能显示图文资料。
        settings.setJavaScriptEnabled(true);
        // 开启 DOM Storage，避免 H5 里使用 localStorage 等能力时被 WebView 默认限制。
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        // 固定缩放比例，减少不同手机系统字体设置对 H5 排版的影响。
        settings.setTextZoom(100);
        webView.loadUrl(H5_ASSET_URL);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            // WebView 内部资源较重，页面销毁时主动释放，避免返回后继续占用内存。
            webView.destroy();
        }
        super.onDestroy();
    }
}
