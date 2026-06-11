package com.moon.moonmusic.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecommendationDetailActivityTest {

    @Test
    public void legacyIntentExtraKeysStayStableForCompatibility() {
        assertEquals("recommend_title", RecommendationDetailActivity.EXTRA_TITLE);
        assertEquals("recommend_reason", RecommendationDetailActivity.EXTRA_REASON);
        assertEquals("recommend_source", RecommendationDetailActivity.EXTRA_SOURCE);
        assertEquals("recommend_tag", RecommendationDetailActivity.EXTRA_TAG);
    }

    @Test
    public void artistProfileContentMatchesNicholasTse() {
        assertEquals("艺人档案", RecommendationDetailActivity.PAGE_TITLE);
        assertEquals("谢霆锋", RecommendationDetailActivity.ARTIST_NAME);
        assertEquals("Nicholas Tse", RecommendationDetailActivity.ARTIST_EN_NAME);
        assertTrue(RecommendationDetailActivity.BIRTHDAY_AND_PLACE.contains("1980"));
        assertTrue(RecommendationDetailActivity.BIRTHDAY_AND_PLACE.contains("香港"));
    }

    @Test
    public void artistProfileContainsWorksAndAchievement() {
        assertTrue(RecommendationDetailActivity.REPRESENTATIVE_WORKS.contains("活着Viva"));
        assertTrue(RecommendationDetailActivity.REPRESENTATIVE_WORKS.contains("玉蝴蝶"));
        assertTrue(RecommendationDetailActivity.ACHIEVEMENT.contains("香港电影金像奖"));
        assertFalse(RecommendationDetailActivity.isBlank(RecommendationDetailActivity.ACHIEVEMENT));
    }

    @Test
    public void artistProfileLoadsMusicGuideMaterial() {
        assertEquals("更多音乐资料", RecommendationDetailActivity.MUSIC_GUIDE_TITLE);
        assertEquals("file:///android_asset/h5/nicholas_tse.html", RecommendationDetailActivity.H5_ASSET_URL);
        assertTrue(RecommendationDetailActivity.H5_ASSET_URL.endsWith("nicholas_tse.html"));
    }
}
