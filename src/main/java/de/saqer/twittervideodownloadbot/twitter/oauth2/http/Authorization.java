package de.saqer.twittervideodownloadbot.twitter.oauth2.http;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class Authorization {

    private final String clientId;
    private final String redirectUri;

    public Authorization(String clientId, String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public String generateAuthorizeUrl() {
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme("https");
            builder.setHost("twitter.com");
            builder.setPath("i/oauth2/authorize");
            builder.addParameter("response_type", "code");
            builder.addParameter("client_id", clientId);
            builder.addParameter("redirect_uri", redirectUri);
            builder.addParameter("scope", "tweet.read tweet.write users.read");
            builder.addParameter("state", "state");
            builder.addParameter("code_challenge", "challenge");
            builder.addParameter("code_challenge_method", "plain");

            return builder.build().toASCIIString().replace("+", "%20");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
