package de.saqer.twittervideodownloadbot.twitter.oauth2.model;

public class OAuth2Credentials {
    private String id;
    private long modifiedAt;
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String refreshToken;

    public OAuth2Credentials(String id, long timestamp, String clientId, String clientSecret, String accessToken, String refreshToken) {
        this.id = id;
        this.modifiedAt = timestamp;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getId() {
        return id;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
