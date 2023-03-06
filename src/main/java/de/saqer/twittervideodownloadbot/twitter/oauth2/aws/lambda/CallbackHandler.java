package de.saqer.twittervideodownloadbot.twitter.oauth2.aws.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import com.google.gson.Gson;
import com.twitter.clientlib.ApiException;
import com.twitter.clientlib.TwitterCredentialsOAuth2;
import com.twitter.clientlib.api.TwitterApi;
import com.twitter.clientlib.auth.TwitterOAuth20Service;
import com.twitter.clientlib.model.Get2UsersMeResponse;
import com.twitter.clientlib.model.User;
import de.saqer.twittervideodownloadbot.twitter.oauth2.model.OAuth2Credentials;
import de.saqer.twittervideodownloadbot.twitter.oauth2.repository.DynamoDBOAuth2CredentialsRepository;
import de.saqer.twittervideodownloadbot.twitter.oauth2.repository.OAuth2CredentialsRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class CallbackHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static Properties properties;

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

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        PKCEService pkceService = new PKCEService();
        PKCE pkce;
        try {
            pkce = pkceService.generatePKCE(readPropertyFromPropertiesFile("twitter.randomBytes").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        AmazonDynamoDB client;
        DynamoDB dynamoDBInstance;
        OAuth2CredentialsRepository credentialsRepository;

        client = AmazonDynamoDBClientBuilder.defaultClient();

        dynamoDBInstance = new DynamoDB(client);
        credentialsRepository = new DynamoDBOAuth2CredentialsRepository(dynamoDBInstance, System.getenv("TWITTER_OAUTH20_CREDENTIALS_DYNAMODB_TABLE"), new Gson());

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        try (TwitterOAuth20Service twitterOAuth20Service = new TwitterOAuth20Service(readPropertyFromPropertiesFile("twitter.clientId"), readPropertyFromPropertiesFile("twitter.clientSecret"), readPropertyFromPropertiesFile("twitter.oauth20RedirectUri"), readPropertyFromPropertiesFile("twitter.oauth20Scope"))) {
            Map<String, String> queryStringParameters = event.getQueryStringParameters();
            String code = queryStringParameters.get("code");
            OAuth2AccessToken accessToken = twitterOAuth20Service.getAccessToken(pkce, code);

            User user = getUser(accessToken);

            OAuth2Credentials oAuth2Credentials = new OAuth2Credentials(0, accessToken.getAccessToken(), accessToken.getRefreshToken(), accessToken.getExpiresIn(), accessToken.getScope(), user.getId(), user.getUsername());
            credentialsRepository.add(oAuth2Credentials);

            logger.log(String.format("access token retrieved and saved - username: %s", user.getUsername()));

            response.setStatusCode(200);
            response.setBody(String.format("Done - username: %s", user.getUsername()));

            return response;
        } catch (IOException | InterruptedException | ExecutionException | ApiException e) {
            throw new RuntimeException(e);
        }
    }

    private User getUser(OAuth2AccessToken accessToken) throws IOException, ApiException {
        TwitterApi twitterApi = new TwitterApi(new TwitterCredentialsOAuth2(readPropertyFromPropertiesFile("twitter.clientId"), readPropertyFromPropertiesFile("twitter.clientSecret"), accessToken.getAccessToken(), accessToken.getRefreshToken(), true));
        Get2UsersMeResponse response = twitterApi.users().findMyUser()
                .execute();
        if (response.getErrors() != null && response.getErrors().size() > 0) {
            throw new RuntimeException("Error while getting user info");
        }

        return response.getData();
    }
}