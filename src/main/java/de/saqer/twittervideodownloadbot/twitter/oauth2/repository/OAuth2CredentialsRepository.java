package de.saqer.twittervideodownloadbot.twitter.oauth2.repository;

import de.saqer.twittervideodownloadbot.twitter.oauth2.model.OAuth2Credentials;

import java.util.List;

public interface OAuth2CredentialsRepository {
    List<OAuth2Credentials> get();
    void save(List<OAuth2Credentials> oAuth2Credentials);
}
