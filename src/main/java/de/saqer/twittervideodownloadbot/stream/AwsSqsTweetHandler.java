package de.saqer.twittervideodownloadbot.stream;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.twitter.clientlib.model.Tweet;

import java.util.logging.Logger;

public class AwsSqsTweetHandler implements TweetHandler {
    private final AmazonSQS sqs;
    private final String sqsQueueUrl;
    private final Logger logger;

    public AwsSqsTweetHandler(AmazonSQS sqs, String sqsQueueUrl, Logger logger) {
        this.sqs = sqs;
        this.sqsQueueUrl = sqsQueueUrl;
        this.logger = logger;
    }

    @Override
    public void handle(Tweet tweet) {
        SendMessageRequest send_msg_request = new SendMessageRequest()
                .withQueueUrl(sqsQueueUrl)
                .withMessageBody(tweet.toJson());
        sqs.sendMessage(send_msg_request);
        logger.info(String.format("tweet sent to sqs - tweet id: %s", tweet.getId()));
    }
}
