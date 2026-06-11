package com.moon.moonmusic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * SQLite 建库帮助类：第一次运行 App 时创建用户表。
 * 登录注册相关的本地数据库表结构集中放在这里维护。
 */
public class UserDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "moon_music.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_USER = "t_user";
    public static final String COL_ID = "_id";
    public static final String COL_NICKNAME = "nickname";
    public static final String COL_QQ = "qq";
    public static final String COL_PASSWORD = "password";
    public static final String COL_GENDER = "gender";
    public static final String COL_HOBBIES = "hobbies";
    public static final String COL_CREATED_AT = "created_at";

    public UserDbHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 用户表保存昵称、QQ、密码、性别和爱好；QQ 设置 UNIQUE 用来保证账号不重复。
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NICKNAME + " TEXT NOT NULL, " +
                COL_QQ + " TEXT NOT NULL UNIQUE, " +
                COL_PASSWORD + " TEXT NOT NULL, " +
                COL_GENDER + " TEXT, " +
                COL_HOBBIES + " TEXT, " +
                COL_CREATED_AT + " INTEGER" +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 当前数据库版本为 1，升级逻辑先采用简单重建表的写法。
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
}
