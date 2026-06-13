package com.moon.moonmusic.model;

/**
 * 用户数据模型，对应 SQLite 中的用户表。
 * 注册时通过 UserDbHelper 写入数据库，登录后由 UserDao 查询并返回给 LoginActivity。
 * 偏好（hobbies）字段以逗号分隔存储多个风格标签，如 "流行,摇滚,民谣"。
 */
public class User {
    private long id;
    private String nickname;
    private String qq;
    private String password;
    private String gender;
    private String hobbies;  // 逗号分隔的风格偏好

    public User() {}

    public User(String nickname, String qq, String password, String gender, String hobbies) {
        this.nickname = nickname;
        this.qq = qq;
        this.password = password;
        this.gender = gender;
        this.hobbies = hobbies;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getQq() { return qq; }
    public void setQq(String qq) { this.qq = qq; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getHobbies() { return hobbies; }
    public void setHobbies(String hobbies) { this.hobbies = hobbies; }
}
