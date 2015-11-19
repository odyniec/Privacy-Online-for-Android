package online.privacy.privacyonline;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class PrivacyOnlineApiRequest {

    private String apiUrl             = "http://polaris:3000/";
    private static final String LOG_TAG_API_REQUEST = "privacyonlineapirequest";

    public static final int VERIFY_USER_ACCOUNT = 1;
    public static final int GET_LOCATION_LIST   = 2;

    PrivacyOnlineApiRequest() {}

    public boolean verifyUserAccount(String username, String password) {
        Log.i(LOG_TAG_API_REQUEST, "Attempting to Verify User Account");
        JSONObject responseData;
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("username", username);
            requestData.put("password", password);
            responseData = makeAPIRequest("PUT", "user/verify", requestData.toString());
            Log.i(LOG_TAG_API_REQUEST, "Response data: " + responseData.get("ok"));
            return (responseData.getString("ok").equals("1"));

        } catch (JSONException je) {
            Log.e(LOG_TAG_API_REQUEST, je.toString());
            return false;

        } catch (IOException ioe) {
            Log.e(LOG_TAG_API_REQUEST, ioe.toString());
            return false;
        }

    }

    public ArrayList<VPNLocation> getLocationList() {
        Log.i(LOG_TAG_API_REQUEST, "Attempting to get location list.");
        JSONObject responseData;
        ArrayList<VPNLocation> locationList;
        try {
            responseData = makeAPIRequest("GET", "location", null);
            JSONArray locations = responseData.getJSONArray("location");
            locationList = new ArrayList<>(locations.length());
            for (int i = 0; i < locations.length(); i++) {
                JSONObject locationIterator = locations.getJSONObject(i);
                locationList.add(
                        new VPNLocation(locationIterator.getString("label"), locationIterator.getString("hostname"))
                );
            }
            return locationList;
        } catch (JSONException je) {
            Log.e(LOG_TAG_API_REQUEST, je.toString());
            return null;
        } catch (IOException ioe) {
            Log.e(LOG_TAG_API_REQUEST, ioe.toString());
            return null;
        }
    }

    private JSONObject makeAPIRequest(String method, String endPoint, String jsonPayload)
            throws IOException, JSONException {
        Log.i(LOG_TAG_API_REQUEST, "Attempting to verify with data payload: "+jsonPayload);
        InputStream inputStream = null;
        OutputStream outputStream = null;

        byte[] payload = jsonPayload.getBytes();
        int payloadSize = payload.length;

        try {
            URL url = new URL(apiUrl + endPoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Sec 5 second connect/read timeouts
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");

            if (!jsonPayload.isEmpty()) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(payloadSize);
            } else {
                connection.setDoInput(false);
            }

            // Initiate the connection
            connection.connect();

            // Write the payload if there is one.
            if (!jsonPayload.isEmpty()) {
                outputStream = connection.getOutputStream();
                outputStream.write(jsonPayload.getBytes("UTF-8"));
            }

            // Get the response.
            int responseCode = connection.getResponseCode();
            Log.i(LOG_TAG_API_REQUEST, "Got response code: "+responseCode);
            inputStream = connection.getInputStream();

            String responseContent = readInputStream(inputStream, connection.getContentLength());
            return new JSONObject(responseContent);

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private String readInputStream(InputStream inputStream, int contentLength) throws IOException {
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        char[] buffer = new char[contentLength];
        reader.read(buffer);
        return new String(buffer);
    }

}

