package de.saqer.twittervideodownloadbot.twitter.oauth2.model;

public class OAuth2Credentials {
    private long usedAtTimestamp;
    private final String accessToken;
    private final String refreshToken;
    private final int expiresIn;
    private final String scope;

    public OAuth2Credentials(long usedAtTimestamp, String accessToken, String refreshToken, int expiresIn, String scope) {
        this.usedAtTimestamp = usedAtTimestamp;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.scope = scope;
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
}

