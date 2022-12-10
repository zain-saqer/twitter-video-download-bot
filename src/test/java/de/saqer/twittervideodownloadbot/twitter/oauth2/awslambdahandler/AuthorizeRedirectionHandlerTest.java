package de.saqer.twittervideodownloadbot.twitter.oauth2.awslambdahandler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import de.saqer.twittervideodownloadbot.twitter.oauth2.http.Authorization;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AuthorizeRedirectionHandlerTest {

    @ParameterizedTest
    @Event(value = "lambda-events/AuthorizeRedirectionHandler-event.json", type = APIGatewayV2HTTPEvent.class)
    public void testHandlerReturnsRedirectResponse(APIGatewayV2HTTPEvent event) {
        String redirectionUrl = "redirection-url";
        Authorization authorization = Mockito.mock(Authorization.class);
        when(authorization.generateAuthorizeUrl()).thenReturn(redirectionUrl);

        AuthorizeRedirectionHandler handler = new AuthorizeRedirectionHandler(authorization);

        Context context = Mockito.mock(Context.class);
        when(context.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        Mockito.verify(authorization).generateAuthorizeUrl();

        assertEquals(307, response.getStatusCode());
        assertEquals(redirectionUrl, response.getHeaders().get("Location"));
    }
}