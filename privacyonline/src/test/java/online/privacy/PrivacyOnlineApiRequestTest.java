package online.privacy;

import android.content.Context;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.anyString;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PrivacyOnlineApiRequest.class)
public class PrivacyOnlineApiRequestTest {

    @Mock
    Context mockContext;

    @Mock
    JSONObject apiResponse;

    @Spy
    PrivacyOnlineApiRequest partialMockedRequest = new PrivacyOnlineApiRequest(mockContext);

    @Before
    public void setUp() throws Exception {
        PowerMockito.doReturn(apiResponse)
                .when(partialMockedRequest, method(PrivacyOnlineApiRequest.class, "makeAPIRequest", String.class, String.class, String.class))
                .withArguments(anyString(), anyString(), anyString());
    }

    @Test
    public void verifyUserAccount_ok() throws Exception {

        // Mock the private method so we don't go to the API.
        // Check that verifyUserAccount() handles a "that's valid" response from the API.
        PowerMockito.doReturn("1")
                .when(apiResponse, method(JSONObject.class, "getString", String.class))
                    .withArguments(anyString());

        Assert.assertEquals(true, partialMockedRequest.verifyUserAccount("bert", "ernie"));
    }

    @Test
    public void verifyUserAccount_notOK() throws Exception {

        // Mock the private method so we don't go to the API.
        // Check that verifyUserAccount() handles a "lolnope" response from the API.
        PowerMockito.doReturn("0")
                .when(apiResponse, method(JSONObject.class, "getString", String.class))
                .withArguments(anyString());

        Assert.assertEquals(false, partialMockedRequest.verifyUserAccount("cannon", "ball"));
    }

    @Test
    public void verifyUserAccount_malformedJSONException() throws Exception {

        // Mock the private method so we don't go to the API.
        // Check that verifyUserAccount() handles a "ZOMGBADJSON!!!one" exception from the API data.
        PowerMockito.doThrow(new JSONException("FAIL"))
                .when(apiResponse, method(JSONObject.class, "getString", String.class))
                .withArguments(anyString());

        Assert.assertEquals(false, partialMockedRequest.verifyUserAccount("thelma", "louise"));
    }

}