package de.saqer.twittervideodownloadbot.stream;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.twitter.clientlib.TwitterCredentialsBearer;
import com.twitter.clientlib.api.TwitterApi;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

public class App {
    private static final Logger logger = Logger.getLogger(App.class.getName());
    private static Properties properties;

    public static void main(String[] args) throws IOException {
        TwitterCredentialsBearer twitterCredentialsOAuth2 = new TwitterCredentialsBearer(readPropertyFromPropertiesFile("twitter.bearer"));
        TwitterApi twitterApi = new TwitterApi(twitterCredentialsOAuth2);
        SearchStream searchStream = new SearchStream(twitterApi);
        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                .withRegion(readPropertyFromPropertiesFile("aws.region"))
                .build();
        AwsSqsTweetHandler awsSqsTweetHandler = new AwsSqsTweetHandler(sqs, readPropertyFromPropertiesFile("stream.sqsQueueUrl"), logger);

        StreamExecutor streamListenersExecutor = new StreamExecutor(awsSqsTweetHandler, searchStream, logger);

        streamListenersExecutor.execute();
    }

    private static String readPropertyFromPropertiesFile(String name) throws IOException {
        String propertyFilename = "config.properties";
        if (properties == null) {
            properties = new Properties();
            properties.load(App.class.getClassLoader().getResourceAsStream(propertyFilename));
        }
        String property = properties.getProperty(name);
        if (property == null) {
            throw new RuntimeException(String.format("property %s not found in %s file", name, propertyFilename));
        }
        return property;
    }
}
