package com.moon.moonmusic.data;

import com.moon.moonmusic.model.Song;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SongRepositoryTest {

    @Test
    public void downloadSongListKeepsOriginalHomePlaylistAndPlayableAssets() {
        List<Song> songs = SongRepository.getDownloadSongList();

        assertEquals(8, songs.size());
        assertEquals("那些花儿", songs.get(0).getTitle());
        assertEquals("朴树", songs.get(0).getArtist());
        assertEquals("小城大事", songs.get(2).getTitle());

        for (Song song : songs) {
            assertTrue(song.getAssetMusicPath().startsWith("music/"));
            assertTrue(song.getAssetLyricPath().startsWith("lyrics/"));
            assertAssetExists(song.getAssetMusicPath());
            assertAssetExists(song.getAssetLyricPath());
        }
    }

    @Test
    public void favoriteSongListKeepsOriginalMyPageItems() {
        List<Song> favorites = SongRepository.getFavoriteList();

        assertEquals(4, favorites.size());
        assertEquals("蔷薇", favorites.get(0).getTitle());
        assertEquals("幸福了 然后呢", favorites.get(3).getTitle());
        assertFalse(favorites.get(0).getAssetMusicPath().startsWith("music/"));
        for (Song song : favorites) {
            assertEquals("喜欢", song.getType());
        }
    }

    @Test
    public void nicholasSongListProvidesPlayablePreviewAssets() {
        List<Song> songs = SongRepository.getNicholasSongList();

        assertEquals(5, songs.size());
        assertEquals("因为爱所以爱", songs.get(0).getTitle());
        assertEquals("谢霆锋", songs.get(0).getArtist());
        assertEquals(201, songs.get(0).getId());
        assertEquals(205, songs.get(4).getId());

        for (Song song : songs) {
            assertEquals("谢霆锋", song.getArtist());
            assertTrue(song.getAssetMusicPath().startsWith("music/nicholas/"));
            assertTrue(song.getAssetMusicPath().endsWith(".m4a"));
            assertTrue(song.getAssetLyricPath().startsWith("lyrics/nicholas/"));
            assertAssetExists(song.getAssetMusicPath());
            assertAssetExists(song.getAssetLyricPath());
        }
    }

    @Test
    public void playableSongListContainsHomeAndNicholasSongs() {
        List<Song> songs = SongRepository.getPlayableSongList();

        assertEquals(13, songs.size());
        assertEquals("那些花儿", songs.get(0).getTitle());
        assertEquals("因为爱所以爱", songs.get(8).getTitle());
    }

    private void assertAssetExists(String assetPath) {
        File file = new File("src/main/assets", assetPath);
        assertTrue("Missing asset: " + file.getPath(), file.isFile());
    }
}
