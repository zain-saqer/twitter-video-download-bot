package de.saqer.twittervideodownloadbot.twitter.oauth2.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorizationTest {
    @Test
    void generateAuthorizeUrlCreatesCorrectUrl() {
        String clientId = "client id";
        String redirectionUrl = "redirection url";
        String expectedUrl = "https://twitter.com/i/oauth2/authorize?response_type=code&client_id=client%20id&redirect_uri=redirection%20url&scope=tweet.read%20tweet.write%20users.read&state=state&code_challenge=challenge&code_challenge_method=plain";

        Authorization authorization = new Authorization(clientId, redirectionUrl);

        assertEquals(expectedUrl, authorization.generateAuthorizeUrl());
    }
}