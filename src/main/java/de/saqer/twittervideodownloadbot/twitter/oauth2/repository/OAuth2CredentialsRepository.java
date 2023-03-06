package de.saqer.twittervideodownloadbot.twitter.oauth2.repository;

import de.saqer.twittervideodownloadbot.twitter.oauth2.model.OAuth2Credentials;

public interface OAuth2CredentialsRepository {
    void add(OAuth2Credentials oAuth2Credentials);
    OAuth2Credentials getFirstUsedCredentials();
}
