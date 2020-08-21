package com.hegp.bean;

public class UserBean {

    private String username;

    private long id;

    private String status;

    private String sign;

    private String avatar;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getSign() {
        return sign;
    }

    public String getAvatar() {
        return avatar;
    }
}
