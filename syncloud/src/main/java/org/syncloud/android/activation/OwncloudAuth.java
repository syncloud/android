package org.syncloud.android.activation;


import org.apache.http.client.CookieStore;

public class OwncloudAuth {
    private String requestToken;
    private CookieStore cookie;

    public OwncloudAuth(String requestToken, CookieStore cookie) {
        this.requestToken = requestToken;
        this.cookie = cookie;
    }

    public String getRequestToken() {
        return requestToken;
    }

    public CookieStore getCookieStore() {
        return cookie;
    }
}
