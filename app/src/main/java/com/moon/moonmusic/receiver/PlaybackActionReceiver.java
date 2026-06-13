package com.moon.moonmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.moon.moonmusic.constant.AppConstants;
import com.moon.moonmusic.service.PlayerService;

/**
 * 通知栏按钮与播放服务之间的桥接广播接收器。
 * 前台通知上的"上一首 / 播放暂停 / 下一首 / 停止"按钮通过 PendingIntent 发出广播，
 * 本接收器将广播中的 action 原样转发给 PlayerService，由 Service 统一处理播放命令。
 * PlayerService 的 buildNotification 方法构建通知时会通过 pendingIntent 静态方法生成对应的 PendingIntent。
 */
public class PlaybackActionReceiver extends BroadcastReceiver {

    /**
     * 收到通知栏按钮发出的广播后，将 action 转发给 PlayerService。
     * 使用 startForegroundService 确保 Android 8+ 上前台服务可以正常接收命令。
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        Intent svc = new Intent(context, PlayerService.class);
        svc.setAction(intent.getAction());
        ContextCompat.startForegroundService(context, svc);
    }

    /**
     * 根据 action 构造一个指向本接收器的 PendingIntent。
     * 通知栏按钮点击后系统会触发这个 PendingIntent，进而走到 onReceive 再转发给 PlayerService。
     * Android 6+ 需要额外声明 FLAG_IMMUTABLE，否则 PendingIntent 创建会抛出异常。
     */
    public static android.app.PendingIntent pendingIntent(Context context, String action) {
        Intent it = new Intent(context, PlaybackActionReceiver.class);
        it.setAction(action);
        // 用 action 的 hashCode 作为 requestCode，不同按钮的 PendingIntent 可以独立更新。
        int requestCode = action.hashCode();
        int flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= 23) flags |= android.app.PendingIntent.FLAG_IMMUTABLE;
        return android.app.PendingIntent.getBroadcast(context, requestCode, it, flags);
    }
}
