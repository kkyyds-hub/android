package com.moon.moonmusic.model;

public class User {
    private long id;
    private String nickname;
    private String qq;
    private String password;
    private String gender;
    private String hobbies;

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
