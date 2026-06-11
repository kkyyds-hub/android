package com.moon.moonmusic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.moon.moonmusic.model.User;

public class UserDao {

    private final UserDbHelper helper;

    public UserDao(Context context) {
        helper = new UserDbHelper(context.getApplicationContext());
    }

    public boolean existsQq(String qq) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(UserDbHelper.TABLE_USER,
                new String[]{UserDbHelper.COL_ID},
                UserDbHelper.COL_QQ + "=?",
                new String[]{qq},
                null, null, null);
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public long insertUser(User user) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(UserDbHelper.COL_NICKNAME, user.getNickname());
        cv.put(UserDbHelper.COL_QQ, user.getQq());
        cv.put(UserDbHelper.COL_PASSWORD, user.getPassword());
        cv.put(UserDbHelper.COL_GENDER, user.getGender());
        cv.put(UserDbHelper.COL_HOBBIES, user.getHobbies());
        cv.put(UserDbHelper.COL_CREATED_AT, System.currentTimeMillis());
        return db.insert(UserDbHelper.TABLE_USER, null, cv);
    }

    public User login(String qq, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(UserDbHelper.TABLE_USER,
                null,
                UserDbHelper.COL_QQ + "=? AND " + UserDbHelper.COL_PASSWORD + "=?",
                new String[]{qq, password},
                null, null, null);
        User u = null;
        if (c.moveToFirst()) {
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
