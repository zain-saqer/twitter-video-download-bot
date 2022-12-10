package de.saqer.twittervideodownloadbot.twitter.oauth2.awslambdahandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import de.saqer.twittervideodownloadbot.twitter.oauth2.http.Authorization;

import java.util.HashMap;
import java.util.Map;

public class AuthorizeRedirectionHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private final Authorization authorization;

    public AuthorizeRedirectionHandler(Authorization authorization) {
        this.authorization = authorization;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {

        LambdaLogger logger = context.getLogger();

        logger.log("redirect to authorize url");

        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();

        response.setStatusCode(307);

        Map<String, String> headers = new HashMap<>();
        headers.put("Location", authorization.generateAuthorizeUrl());
        response.setHeaders(headers);

        return response;
    }
}