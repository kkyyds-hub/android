package com.moon.moonmusic.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences 工具类，负责登录会话的持久化存储。
 * 登录成功后保存 QQ 号、昵称和头像类型；MyFragment 等页面通过 get 方法读取当前用户信息展示在界面上。
 * 退出登录时调用 clearLogin 清空全部数据，回到未登录状态。
 *
 * 两个 saveLogin 重载的区别：
 * - 两参数版本供登录页快速保存，不改变头像类型
 * - 三参数版本在注册时调用，同时记录新注册用户应使用的头像
 */
public class SpUtil {
    private static final String SP_NAME = "moon_music_sp";
    private static final String KEY_LOGIN_QQ = "login_qq";
    private static final String KEY_LOGIN_NICK = "login_nick";
    private static final String KEY_AVATAR_TYPE = "avatar_type";

    // 头像类型常量：已有账号登录用 Aerith，新注册用户用 Zack。
    public static final String AVATAR_AERITH = "aerith";
    public static final String AVATAR_ZACK = "zack";

    /** 保存登录信息（不改变头像类型），已有账号直接登录时使用。 */
    public static void saveLogin(Context context, String qq, String nick) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_LOGIN_QQ, qq)
                .putString(KEY_LOGIN_NICK, nick)
                .apply();
    }

    /** 保存登录信息并记录头像类型，新用户注册时使用。 */
    public static void saveLogin(Context context, String qq, String nick, String avatarType) {
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_LOGIN_QQ, qq)
                .putString(KEY_LOGIN_NICK, nick)
                .putString(KEY_AVATAR_TYPE, avatarType)
                .apply();
    }

    /** 获取当前登录的 QQ 号，未登录时返回空字符串。 */
    public static String getLoginQq(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString(KEY_LOGIN_QQ, "");
    }

    /** 获取当前登录的昵称，未登录时返回空字符串。 */
    public static String getLoginNick(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString(KEY_LOGIN_NICK, "");
    }

    /** 获取当前用户的头像类型，默认为 AVATAR_AERITH。 */
    public static String getAvatarType(Context context) {
        return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .getString(KEY_AVATAR_TYPE, AVATAR_AERITH);
    }

    /** 单独更新头像类型，MyFragment 切换头像时使用。 */
    public static void saveAvatarType(Context context, String avatarType) {
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AVATAR_TYPE, avatarType)
                .apply();
    }

    /** 清空所有登录数据，退出登录时调用。 */
    public static void clearLogin(Context context) {
        context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
