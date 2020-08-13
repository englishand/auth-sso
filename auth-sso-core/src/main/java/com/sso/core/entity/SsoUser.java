package com.sso.core.entity;

import java.io.Serializable;

public class SsoUser implements Serializable {

    private static final long serialVersionUID=42L;

    private String userId;
    private String username;
    private String password;
    private String version;
    private int expireMinite;
    private long expireFreshMinite;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getExpireMinite() {
        return expireMinite;
    }

    public void setExpireMinite(int expireMinite) {
        this.expireMinite = expireMinite;
    }

    public long getExpireFreshMinite() {
        return expireFreshMinite;
    }

    public void setExpireFreshMinite(long expireFreshMinite) {
        this.expireFreshMinite = expireFreshMinite;
    }
}
