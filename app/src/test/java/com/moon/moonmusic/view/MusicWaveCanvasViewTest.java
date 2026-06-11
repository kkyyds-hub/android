package com.moon.moonmusic.view;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MusicWaveCanvasViewTest {

    @Test
    public void calculateWaveHeightsCreatesFiveVisibleBars() {
        int[] heights = MusicWaveCanvasView.calculateWaveHeights(200);

        assertEquals(5, heights.length);
        for (int height : heights) {
            assertTrue(height > 0);
            assertTrue(height <= 200);
        }
    }

    @Test
    public void calculateWaveHeightsKeepsMinimumSizeForSmallViews() {
        int[] heights = MusicWaveCanvasView.calculateWaveHeights(20);

        assertTrue(heights[0] >= Math.round(120 * 0.26f));
        assertTrue(heights[2] >= Math.round(120 * 0.62f));
    }

    @Test
    public void calculateAnimatedWaveHeightsChangesAcrossFrames() {
        int[] first = MusicWaveCanvasView.calculateAnimatedWaveHeights(200, 0);
        int[] later = MusicWaveCanvasView.calculateAnimatedWaveHeights(200, 10);

        assertEquals(5, first.length);
        assertEquals(5, later.length);
        assertTrue(first[0] != later[0] || first[1] != later[1] || first[2] != later[2]);
    }

    @Test
    public void calculateAnimatedWaveHeightsHasVisibleBeatRange() {
        int[] lowBeat = MusicWaveCanvasView.calculateAnimatedWaveHeights(200, 9);
        int[] highBeat = MusicWaveCanvasView.calculateAnimatedWaveHeights(200, 0);

        assertTrue(Math.abs(highBeat[0] - lowBeat[0]) >= 30);
        assertTrue(Math.abs(highBeat[2] - lowBeat[2]) >= 20);
    }

    @Test
    public void calculateArcStartAngleRotatesRecordHighlight() {
        float start = MusicWaveCanvasView.calculateArcStartAngle(0);
        float later = MusicWaveCanvasView.calculateArcStartAngle(10);

        assertTrue(later > start);
        assertEquals(-35f, start, 0.01f);
    }

    @Test
    public void calculateCenterPulseChangesAcrossBeat() {
        float low = MusicWaveCanvasView.calculateCenterPulse(9);
        float high = MusicWaveCanvasView.calculateCenterPulse(0);

        assertTrue(high > low);
    }
}
