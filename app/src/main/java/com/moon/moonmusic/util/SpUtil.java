package com.moon.moonmusic.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {
    private static final String SP_NAME = "moon_music_sp";
    private static final String KEY_LOGIN_QQ = "login_qq";
    private static final String KEY_LOGIN_NICK = "login_nick";
    private static final String KEY_AVATAR_TYPE = "avatar_type";

    // Avatar types
    public static final String AVATAR_AERITH = "aerith"; // 已有账号登录
    public static final String AVATAR_ZACK = "zack";     // 新账号注册

    public static void saveLogin(Context context, String qq, String nick) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_LOGIN_QQ, qq)
                .putString(KEY_LOGIN_NICK, nick)
                .apply();
    }

    public static void saveLogin(Context context, String qq, String nick, String avatarType) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_LOGIN_QQ, qq)
                .putString(KEY_LOGIN_NICK, nick)
                .putString(KEY_AVATAR_TYPE, avatarType)
                .apply();
    }

    public static String getLoginQq(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString(KEY_LOGIN_QQ, "");
    }

    public static String getLoginNick(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString(KEY_LOGIN_NICK, "");
    }

    public static String getAvatarType(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .getString(KEY_AVATAR_TYPE, AVATAR_AERITH);
    }

    public static void saveAvatarType(Context context, String avatarType) {
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AVATAR_TYPE, avatarType)
                .apply();
    }

    public static void clearLogin(Context context) {
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
