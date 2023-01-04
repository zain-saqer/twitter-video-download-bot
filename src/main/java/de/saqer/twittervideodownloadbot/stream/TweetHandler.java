package de.saqer.twittervideodownloadbot.stream;

import com.twitter.clientlib.model.Tweet;

public interface TweetHandler {
    void handle(Tweet tweetJson);
}
