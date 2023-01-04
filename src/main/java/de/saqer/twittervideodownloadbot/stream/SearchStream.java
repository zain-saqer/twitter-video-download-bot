package de.saqer.twittervideodownloadbot.stream;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.StreamingTweetResponse;
import de.saqer.twittervideodownloadbot.stream.exception.InvalidTweetJsonException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class SearchStream {
    public static int TRIES = 10;
    private final TwitterApi apiInstance;

    public SearchStream(TwitterApi apiInstance) {

        this.apiInstance = apiInstance;
    }

    public InputStream connectStream() throws ApiException {
        Set<String> tweetFields = new HashSet<>();
        tweetFields.add("author_id");
        tweetFields.add("id");
        tweetFields.add("created_at");
        tweetFields.add("referenced_tweets");

        return this.apiInstance.tweets().searchStream()
                .backfillMinutes(0)
                .tweetFields(tweetFields)
                .execute(TRIES);
    }

    public boolean hasErrors(@NotNull StreamingTweetResponse streamingTweet) {
        return streamingTweet.getErrors() != null;
    }

    public StreamingTweetResponse getStreamingTweetResponseObject(@NotNull String tweetJson) throws InvalidTweetJsonException {
        try {
            return StreamingTweetResponse.fromJson(tweetJson);
        } catch (IOException e) {
            throw new InvalidTweetJsonException(e);
        }
    }
}
