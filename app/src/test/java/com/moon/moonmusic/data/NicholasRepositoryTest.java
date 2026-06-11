package com.moon.moonmusic.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NicholasRepositoryTest {

    @Test
    public void albumTitlesProvideNicholasSectionSkeleton() {
        assertEquals(5, NicholasRepository.getAlbumTitles().size());
        assertTrue(NicholasRepository.getAlbumTitles().contains("Viva"));
        assertTrue(NicholasRepository.getAlbumTitles().contains("玉蝴蝶"));
    }

    @Test
    public void recommendationIntroMatchesCurrentCards() {
        assertTrue(NicholasRepository.getRecommendationIntro().contains("线上曲库"));
        assertTrue(NicholasRepository.getRecommendationIntro().contains("城市"));
    }

    @Test
    public void albumWallIntroDescribesListeningPath() {
        assertTrue(NicholasRepository.getAlbumWallIntro().contains("早期国语专辑"));
        assertTrue(NicholasRepository.getAlbumWallIntro().contains("粤语作品"));
    }

    @Test
    public void recommendedSongsStayInNicholasTheme() {
        assertEquals("因为爱所以爱", NicholasRepository.getRecommendedSongTitles().get(0));
        assertTrue(NicholasRepository.getRecommendedSongTitles().contains("谢谢你的爱1999"));
    }
}
