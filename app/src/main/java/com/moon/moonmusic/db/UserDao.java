package com.moon.moonmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.moon.moonmusic.model.User;

/**
 * 用户数据访问类：把注册、查重、登录这些数据库操作集中起来。
 * Activity 不直接写 SQL，页面交互和数据库读写可以分开维护。
 */
public class UserDao {

    private final UserDbHelper helper;

    public UserDao(Context context) {
        helper = new UserDbHelper(context.getApplicationContext());
    }

    /**
     * 查询指定 QQ 是否已经注册。
     * 注册流程会先调用这里做查重，避免重复账号继续写入数据库。
     */
    public boolean existsQq(String qq) {
        SQLiteDatabase db = helper.getReadableDatabase();
        // 注册前先按 QQ 查询，避免 UNIQUE 约束报错后用户不知道原因。
        Cursor c = db.query(UserDbHelper.TABLE_USER,
                new String[]{UserDbHelper.COL_ID},
                UserDbHelper.COL_QQ + "=?",
                new String[]{qq},
                null, null, null);
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    /**
     * 把注册表单中的用户信息写入 SQLite。
     * LoginActivity 根据返回的行号判断注册是否成功。
     */
    public long insertUser(User user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        // ContentValues 可以理解为一行数据的键值对，键对应数据库字段名。
        ContentValues cv = new ContentValues();
        cv.put(UserDbHelper.COL_NICKNAME, user.getNickname());
        cv.put(UserDbHelper.COL_QQ, user.getQq());
        cv.put(UserDbHelper.COL_PASSWORD, user.getPassword());
        cv.put(UserDbHelper.COL_GENDER, user.getGender());
        cv.put(UserDbHelper.COL_HOBBIES, user.getHobbies());
        cv.put(UserDbHelper.COL_CREATED_AT, System.currentTimeMillis());
        return db.insert(UserDbHelper.TABLE_USER, null, cv);
    }

    /**
     * 根据 QQ 和密码查询用户。
     * 登录成功时返回完整 User 对象，页面会把其中的账号和昵称保存为登录态。
     */
    public User login(String qq, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        // 登录时同时匹配 QQ 和密码；使用占位参数可以避免手动拼接字符串带来的问题。
        Cursor c = db.query(UserDbHelper.TABLE_USER,
                null,
                UserDbHelper.COL_QQ + "=? AND " + UserDbHelper.COL_PASSWORD + "=?",
                new String[]{qq, password},
                null, null, null);
        User u = null;
        if (c.moveToFirst()) {
            // Cursor 指向查询结果的一行，把数据库字段再转换回 User 对象给页面使用。
            u = new User();
            u.setId(c.getLong(c.getColumnIndexOrThrow(UserDbHelper.COL_ID)));
            u.setNickname(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_NICKNAME)));
            u.setQq(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_QQ)));
            u.setPassword(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_PASSWORD)));
            u.setGender(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_GENDER)));
            u.setHobbies(c.getString(c.getColumnIndexOrThrow(UserDbHelper.COL_HOBBIES)));
        }
        c.close();
        return u;
    }
}
