package com.moon.moonmusic.ui;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayerActivityTest {

    @Test
    public void nextCoverEffectModeCyclesThroughAllEffects() {
        int mode = PlayerActivity.EFFECT_NORMAL;

        mode = PlayerActivity.nextCoverEffectMode(mode);
        assertEquals(PlayerActivity.EFFECT_ROTATE, mode);

        mode = PlayerActivity.nextCoverEffectMode(mode);
        assertEquals(PlayerActivity.EFFECT_GRAY_SCALE, mode);

        mode = PlayerActivity.nextCoverEffectMode(mode);
        assertEquals(PlayerActivity.EFFECT_NORMAL, mode);
    }
}
