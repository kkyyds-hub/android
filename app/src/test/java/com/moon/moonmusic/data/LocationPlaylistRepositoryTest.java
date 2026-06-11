package com.moon.moonmusic.data;

import com.moon.moonmusic.model.LocationPlaylistRecommendation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocationPlaylistRepositoryTest {

    @Test
    public void greaterBayAreaLocationBuildsCantoneseRockPlaylist() {
        LocationPlaylistRecommendation recommendation = LocationPlaylistRepository.buildFromLocation(22.32, 114.17);

        assertEquals("粤语摇滚现场歌单", recommendation.getTitle());
        assertEquals(204, recommendation.getPrimarySongId());
        assertTrue(recommendation.getTag().contains("粤港澳"));
        assertTrue(recommendation.isFromLocation());
    }

    @Test
    public void beijingLocationBuildsNorthernRockPlaylist() {
        LocationPlaylistRecommendation recommendation = LocationPlaylistRepository.buildFromLocation(39.90, 116.40);

        assertEquals("北方摇滚能量歌单", recommendation.getTitle());
        assertEquals(203, recommendation.getPrimarySongId());
        assertTrue(recommendation.getReason().contains("黄种人"));
    }

    @Test
    public void shanghaiLocationBuildsCityNightPlaylist() {
        LocationPlaylistRecommendation recommendation = LocationPlaylistRepository.buildFromLocation(31.23, 121.47);

        assertEquals("城市夜行歌单", recommendation.getTitle());
        assertEquals(201, recommendation.getPrimarySongId());
        assertTrue(recommendation.getSource().contains("31.23"));
    }

    @Test
    public void unknownLocationBuildsTravelPlaylist() {
        LocationPlaylistRecommendation recommendation = LocationPlaylistRepository.buildFromLocation(35.00, 104.00);

        assertEquals("旅行路上歌单", recommendation.getTitle());
        assertEquals(205, recommendation.getPrimarySongId());
        assertTrue(recommendation.isFromLocation());
    }

    @Test
    public void fallbackRecommendationsDoNotClaimLocationData() {
        LocationPlaylistRecommendation defaultRecommendation = LocationPlaylistRepository.getDefaultRecommendation();
        LocationPlaylistRecommendation deniedRecommendation = LocationPlaylistRepository.getPermissionDeniedRecommendation();

        assertFalse(defaultRecommendation.isFromLocation());
        assertFalse(deniedRecommendation.isFromLocation());
        assertTrue(defaultRecommendation.getSource().contains("默认"));
        assertTrue(deniedRecommendation.getSource().contains("权限"));
    }
}
