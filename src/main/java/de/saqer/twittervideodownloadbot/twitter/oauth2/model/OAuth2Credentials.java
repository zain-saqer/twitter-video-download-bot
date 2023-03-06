package de.saqer.twittervideodownloadbot.twitter.oauth2.model;

public class OAuth2Credentials {
    private long usedAtTimestamp;
    private final String accessToken;
    private final String refreshToken;
    private final int expiresIn;
    private final String scope;
    private final String userId;
    private final String username;

    public OAuth2Credentials(long usedAtTimestamp, String accessToken, String refreshToken, int expiresIn, String scope, String userId, String username) {
        this.usedAtTimestamp = usedAtTimestamp;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.scope = scope;
        this.userId = userId;
        this.username = username;
    }

    public void setUsedAtTimestamp(long usedAtTimestamp) {
        this.usedAtTimestamp = usedAtTimestamp;
    }

    public long getUsedAtTimestamp() {
        return usedAtTimestamp;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getScope() {
        return scope;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }
}

