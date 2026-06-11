package com.moon.moonmusic.data;

import com.moon.moonmusic.model.RemoteRecommendation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecommendationRepositoryTest {

    @Test
    public void parseRecommendationConvertsHttpJsonToUiModel() throws Exception {
        String json = "{"
                + "\"resultCount\":1,"
                + "\"results\":[{"
                + "\"artistName\":\"Nicholas Tse\","
                + "\"trackName\":\"活著Viva\","
                + "\"collectionName\":\"Viva\","
                + "\"artworkUrl100\":\"https://example.com/100x100bb.jpg\""
                + "}]"
                + "}";

        RemoteRecommendation recommendation = RecommendationRepository.parseRecommendation(json);

        assertTrue(recommendation.isFromNetwork());
        assertTrue(recommendation.getTitle().contains("活著Viva"));
        assertTrue(recommendation.getReason().contains("Nicholas Tse"));
        assertTrue(recommendation.getReason().contains("Viva"));
        assertTrue(recommendation.getTag().contains("在线"));
        assertTrue(recommendation.getSource().contains("Apple"));
        assertTrue(recommendation.getArtworkUrl().contains("300x300bb.jpg"));
        assertTrue(recommendation.getLocalArtworkName().contains("viva"));
    }

    @Test
    public void parseRecommendationCanPickAnotherSongByIndex() throws Exception {
        String json = "{"
                + "\"resultCount\":2,"
                + "\"results\":[{"
                + "\"artistName\":\"Nicholas Tse\","
                + "\"trackName\":\"活著Viva\","
                + "\"collectionName\":\"Viva\","
                + "\"artworkUrl100\":\"https://example.com/viva/100x100bb.jpg\""
                + "},{"
                + "\"artistName\":\"Nicholas Tse\","
                + "\"trackName\":\"香水\","
                + "\"collectionName\":\"Viva\","
                + "\"artworkUrl100\":\"https://example.com/perfume/100x100bb.jpg\""
                + "}]"
                + "}";

        RemoteRecommendation first = RecommendationRepository.parseRecommendation(json, 0);
        RemoteRecommendation second = RecommendationRepository.parseRecommendation(json, 1);
        RemoteRecommendation wrapped = RecommendationRepository.parseRecommendation(json, 2);

        assertTrue(first.getTitle().contains("活著Viva"));
        assertTrue(second.getTitle().contains("香水"));
        assertTrue(wrapped.getTitle().contains("活著Viva"));
        assertTrue(second.getArtworkUrl().contains("perfume"));
    }

    @Test
    public void parseRecommendationFallsBackWhenResultsAreEmpty() throws Exception {
        RecommendationRepository.resetRecommendationCursorForTest();

        RemoteRecommendation recommendation = RecommendationRepository.parseRecommendation("{\"resultCount\":0,\"results\":[]}");

        assertFalse(recommendation.isFromNetwork());
        assertTrue(recommendation.getTitle().contains("因为爱所以爱"));
    }

    @Test
    public void fallbackRecommendationIsMarkedAsLocalData() {
        RecommendationRepository.resetRecommendationCursorForTest();

        RemoteRecommendation recommendation = RecommendationRepository.getFallbackRecommendation();

        assertFalse(recommendation.isFromNetwork());
        assertTrue(recommendation.getTitle().contains("因为爱所以爱"));
        assertTrue(recommendation.getSource().contains("Moon Music"));
    }

    @Test
    public void fallbackRecommendationRotatesWhenNetworkUnavailable() {
        RecommendationRepository.resetRecommendationCursorForTest();

        RemoteRecommendation first = RecommendationRepository.getFallbackRecommendation();
        RemoteRecommendation second = RecommendationRepository.getFallbackRecommendation();

        assertTrue(first.getTitle().contains("因为爱所以爱"));
        assertTrue(second.getTitle().contains("谢谢你的爱1999"));
        assertTrue(first.getLocalArtworkName().contains("understand"));
        assertTrue(second.getLocalArtworkName().contains("thanks"));
    }
}
