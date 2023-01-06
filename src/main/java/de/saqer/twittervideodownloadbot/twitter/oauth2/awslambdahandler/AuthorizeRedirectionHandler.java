package de.saqer.twittervideodownloadbot.twitter.oauth2.awslambdahandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import de.saqer.twittervideodownloadbot.twitter.oauth2.http.Authorization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AuthorizeRedirectionHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static Properties properties;

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        LambdaLogger logger = context.getLogger();

        Authorization authorization;
        try {
            authorization = new Authorization(readPropertyFromPropertiesFile("twitter.clientId"), readPropertyFromPropertiesFile("twitter.oauth20_redirection_uri"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        logger.log("redirect to authorize url");

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        response.setStatusCode(307);

        Map<String, String> headers = new HashMap<>();
        headers.put("Location", authorization.generateAuthorizeUrl());
        response.setHeaders(headers);

        return response;
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