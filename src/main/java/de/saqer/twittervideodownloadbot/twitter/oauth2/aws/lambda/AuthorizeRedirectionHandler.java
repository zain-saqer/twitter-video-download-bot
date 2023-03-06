package de.saqer.twittervideodownloadbot.twitter.oauth2.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.github.scribejava.core.pkce.PKCE;
import com.github.scribejava.core.pkce.PKCEService;
import com.twitter.clientlib.auth.TwitterOAuth20Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AuthorizeRedirectionHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static Properties properties;

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        LambdaLogger logger = context.getLogger();
        PKCEService  pkceService = new PKCEService();
        PKCE pkce;
        try {
            pkce = pkceService.generatePKCE(readPropertyFromPropertiesFile("twitter.randomBytes").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (TwitterOAuth20Service twitterOAuth20Service = new TwitterOAuth20Service(
                readPropertyFromPropertiesFile("twitter.clientId"),
                readPropertyFromPropertiesFile("twitter.clientSecret"),
                readPropertyFromPropertiesFile("twitter.oauth20RedirectUri"),
                readPropertyFromPropertiesFile("twitter.oauth20Scope")
        )) {
            logger.log("redirect to authorize url");
            APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
            response.setStatusCode(307);
            Map<String, String> headers = new HashMap<>();
            headers.put("Location", twitterOAuth20Service.getAuthorizationUrl(pkce, "state"));
            response.setHeaders(headers);

            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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