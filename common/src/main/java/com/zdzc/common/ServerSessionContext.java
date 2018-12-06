package com.zdzc.common;

public class ServerSessionContext {
    private String token = null;

    private String userid = null;

    public ServerSessionContext() {
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }
}
