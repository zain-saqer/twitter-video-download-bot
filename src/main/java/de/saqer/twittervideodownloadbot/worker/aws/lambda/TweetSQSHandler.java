package de.saqer.twittervideodownloadbot.worker.aws.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.google.gson.Gson;
import com.twitter.clientlib.TwitterCredentialsOAuth2;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.model.Tweet;
import de.saqer.twittervideodownloadbot.twitter.oauth2.aws.lambda.AuthorizeRedirectionHandler;
import de.saqer.twittervideodownloadbot.twitter.oauth2.model.OAuth2Credentials;
import de.saqer.twittervideodownloadbot.twitter.oauth2.repository.DynamoDBOAuth2CredentialsRepository;
import de.saqer.twittervideodownloadbot.worker.TweetHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TweetSQSHandler implements RequestHandler<SQSEvent, SQSBatchResponse>{
    private static Properties properties;

    @Override
    public SQSBatchResponse handleRequest(SQSEvent event, Context context)
    {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDBInstance = new DynamoDB(client);
        DynamoDBOAuth2CredentialsRepository credentialsRepository = new DynamoDBOAuth2CredentialsRepository(dynamoDBInstance, System.getenv("TWITTER_OAUTH20_CREDENTIALS_DYNAMODB_TABLE"), new Gson());

        OAuth2Credentials oAuth2Credentials = credentialsRepository.getFirstUsedCredentials();
        TwitterCredentialsOAuth2 twitterCredentialsOAuth2;
        try {
            twitterCredentialsOAuth2 = new TwitterCredentialsOAuth2(
                    readPropertyFromPropertiesFile("twitter.clientId"),
                    readPropertyFromPropertiesFile("twitter.clientSecret"),
                    oAuth2Credentials.getAccessToken(),
                    oAuth2Credentials.getRefreshToken(),
                    true
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TwitterApi apiInstance = new TwitterApi(twitterCredentialsOAuth2);
        TweetHandler tweetHandler = new TweetHandler(apiInstance);
        List<SQSBatchResponse.BatchItemFailure> batchItemFailures = new ArrayList<>();
        for(SQSMessage msg : event.getRecords()){
            try {
                System.out.println("handling tweet - message id: " + msg.getMessageId());
                Tweet tweet = Tweet.fromJson(msg.getBody());
                tweetHandler.handle(tweet);
            } catch (Exception e) {
                e.printStackTrace();
                batchItemFailures.add(new SQSBatchResponse.BatchItemFailure(msg.getMessageId()));
            }
        }

        return new SQSBatchResponse(batchItemFailures);
    }

    private static String readPropertyFromPropertiesFile(String name) throws IOException {
        String propertyFilename = "config.properties";
        if (properties == null) {
            properties = new Properties();
            properties.load(AuthorizeRedirectionHandler.class.getClassLoader().getResourceAsStream(propertyFilename));
        }
        String property = properties.getProperty(name);
        if (property == null) {
            throw new RuntimeException(String.format("property %s not found in %s file", name, propertyFilename));
        }
        return property;
    }
}
