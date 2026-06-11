package com.moon.moonmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.moon.moonmusic.constant.AppConstants;
import com.moon.moonmusic.service.PlayerService;


public class PlaybackActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        Intent svc = new Intent(context, PlayerService.class);
        svc.setAction(intent.getAction());
        ContextCompat.startForegroundService(context, svc);
    }

    public static android.app.PendingIntent pendingIntent(Context context, String action) {
        Intent it = new Intent(context, PlaybackActionReceiver.class);
        it.setAction(action);
        int requestCode = action.hashCode();
        int flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= android.app.PendingIntent.FLAG_IMMUTABLE;
        return android.app.PendingIntent.getBroadcast(context, requestCode, it, flags);
    }
}
