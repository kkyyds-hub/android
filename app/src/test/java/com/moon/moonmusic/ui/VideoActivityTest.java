package com.moon.moonmusic.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VideoActivityTest {

    @Test
    public void videoTitleMatchesNicholasTseTheme() {
        assertEquals("谢霆锋 2000 Viva Live", VideoActivity.VIDEO_TITLE);
        assertTrue(VideoActivity.VIDEO_TITLE.contains("谢霆锋"));
        assertTrue(VideoActivity.VIDEO_SUBTITLE.contains("活着Viva"));
    }

    @Test
    public void videoSourceMatchesVivaLiveClip() {
        assertTrue(VideoActivity.VIDEO_SOURCE_NAME.contains("Bilibili"));
        assertTrue(VideoActivity.VIDEO_SOURCE_NAME.contains("BV1o64y127G4"));
    }

    @Test
    public void localFallbackUriUsesAndroidResourceScheme() {
        String uri = VideoActivity.buildLocalVideoUriString("com.moon.moonmusic");

        assertTrue(uri.startsWith("android.resource://"));
        assertTrue(uri.contains("com.moon.moonmusic"));
    }

    @Test
    public void playbackStatusCoversRemoteAndFallbackStates() {
        assertTrue(VideoActivity.STATUS_LOADING.contains("加载"));
        assertTrue(VideoActivity.STATUS_PLAYING.contains("Viva Live"));
    }

    @Test
    public void livePageHasIntroAndComments() {
        assertEquals("Viva Live 演唱会", VideoActivity.LIVE_INTRO_TITLE);
        assertEquals("现场音浪", VideoActivity.LIVE_WAVE_TITLE);
        assertEquals("乐迷评论", VideoActivity.COMMENT_SECTION_TITLE);
        assertEquals(3, VideoActivity.COMMENT_COUNT);
        assertEquals(300, VideoActivity.WAVE_SYNC_INTERVAL_MS);
    }
}
