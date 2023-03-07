package de.saqer.twittervideodownloadbot.worker;

import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class TweetHandler {

    private final TwitterApi apiInstance;

    public TweetHandler(TwitterApi apiInstance) {
        this.apiInstance = apiInstance;
    }

    public void handle(Tweet tweet) throws ApiException {
        TweetReferencedTweets repliedToRefTweet = getRepliedToReferenceTweet(tweet);
        if (null == repliedToRefTweet) {
            return;
        }

        Get2TweetsIdResponse repliedToTweetResp = getRepliedToTweetResponse(repliedToRefTweet.getId());

        if (null == repliedToTweetResp.getIncludes()) {
            return;
        }

        List<Video> videoList = getVideoMedia(repliedToTweetResp.getIncludes());
        
        if (videoList.isEmpty()) {
            return;
        }

        List<Variant> variantList = new ArrayList<>();

        for (Video video :
                videoList) {
            if (null == video.getVariants()) {
                continue;
            }
            video.getVariants().stream()
                    .filter(v -> Objects.equals(v.getContentType(), "video/mp4"))
                    .max(Comparator.comparing(v -> v.getBitRate() != null ? v.getBitRate() : 0))
                    .ifPresent(variantList::add);
        }

        if (variantList.isEmpty()) {
            return;
        }

        replyToTweet(tweet, variantList);
    }

    private void replyToTweet(Tweet tweet, List<Variant> variantList) throws ApiException {
        StringBuilder sb = new StringBuilder();
        for (Variant variant :
                variantList) {
            sb.append(variant.getUrl()).append(" ");
        }
        String tweetText = sb.toString();

        TweetCreateRequestReply reply = new TweetCreateRequestReply();
        reply.setInReplyToTweetId(tweet.getId());

        TweetCreateRequest tweetCreateRequest = new TweetCreateRequest();
        tweetCreateRequest.setText(tweetText);
        tweetCreateRequest.setReply(reply);

        apiInstance.tweets()
                .createTweet(tweetCreateRequest)
                .execute(5);
    }

    private List<Video> getVideoMedia(Expansions expansions) {
        List<Media> mediaList = expansions.getMedia();

        if (null == mediaList) {
            return new ArrayList<>();
        }

        return mediaList.stream()
                .filter(media -> media.getType().equals("video"))
                .map(m -> (Video) m)
                .collect(Collectors.toList());
    }

    private Get2TweetsIdResponse getRepliedToTweetResponse(String id) throws ApiException {
        Set<String> tweetFields = new HashSet<>();
        tweetFields.add("author_id");
        tweetFields.add("id");
        tweetFields.add("created_at");
        Set<String> expansions = new HashSet<>();
        expansions.add("attachments.media_keys");
        expansions.add("author_id");
        Set<String> mediaFields = new HashSet<>();
        mediaFields.add("variants");

        // findTweetById
        Get2TweetsIdResponse result = apiInstance.tweets().findTweetById(id)
                .tweetFields(tweetFields)
                .expansions(expansions)
                .mediaFields(mediaFields)
                .execute();
        if(result.getErrors() != null && result.getErrors().size() > 0) {
            System.err.println("Error:");

            result.getErrors().forEach(e -> System.err.println(e.toString()));

            throw new RuntimeException();
        }

        return result;
    }

    private TweetReferencedTweets getRepliedToReferenceTweet(Tweet tweet) {
        if (tweet.getReferencedTweets() == null) {
            return null;
        }
        for (TweetReferencedTweets referencedTweet:
             tweet.getReferencedTweets()) {
            if (referencedTweet.getType() == TweetReferencedTweets.TypeEnum.REPLIED_TO) {
                return referencedTweet;
            }
        }

        return null;
    }
}
