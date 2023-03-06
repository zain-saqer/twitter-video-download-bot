package de.saqer.twittervideodownloadbot.twitter.oauth2.aws.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.tests.annotations.Event;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class AuthorizeRedirectionHandlerTest {

    @ParameterizedTest
    @Event(value = "lambda-events/AuthorizeRedirectionHandler-event.json", type = APIGatewayV2HTTPEvent.class)
    public void testHandlerReturnsRedirectResponse(APIGatewayV2HTTPEvent event) {
        AuthorizeRedirectionHandler handler = new AuthorizeRedirectionHandler();

        Context context = Mockito.mock(Context.class);
        when(context.getLogger()).thenReturn(Mockito.mock(LambdaLogger.class));

        APIGatewayV2HTTPResponse response = handler.handleRequest(event, context);

        assertEquals(307, response.getStatusCode());
        assertNotNull(response.getHeaders().get("Location"));
    }
}