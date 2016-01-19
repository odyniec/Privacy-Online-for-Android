package online.privacy;
/**
 * HTTPS API communication class.
 *
 * Talks to the Privacy Online API over HTTPS using JSON payloads.
 * Used to verify user credentials and obtain available VPN location node lists.
 *
 * Copyright © 2016, privacy.online
 * All rights reserved.
 *
 * This file is part of Privacy Online for Android.
 *
 * Privacy Online for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Online for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Online for Android.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author James Ronan <jim@dev.uk2.net>
 */
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class PrivacyOnlineApiRequest {

    private static final String LOG_TAG = "p.o.api.request";
    private Context context;

    PrivacyOnlineApiRequest(Context context) {
        this.context = context;
    }

    /**
     * Uses HTTPS to verify the supplied credentials against the Privacy Online user account
     * API. Returns true/false indicating whether or not the supplied credentials are valid.
     *
     * @param  username Username for account.
     * @param  password Password for account.
     * @return boolean  Validity of account credentials.
     */
    public boolean verifyUserAccount(String username, String password) {

        JSONObject responseData;
        try {
            JSONObject requestData = new JSONObject();
            requestData.put("username", username);
            requestData.put("password", password);
            responseData = makeAPIRequest("PUT", "/user/verify", requestData.toString());
            return (responseData.getString("ok").equals("1"));

        } catch (JSONException je) {
            Log.e(LOG_TAG, je.toString());
            return false;

        } catch (IOException ioe) {
            Log.e(LOG_TAG, ioe.toString());
            return false;
        }

    }

    /**
     * Retrieves a list of the currently available VPN locations from the Privacy Online API.
     *
     * *Not Currently Used*
     *
     * @return ArrayList of VPNLocations
     */
    public ArrayList<VPNLocation> getLocationList() {
        JSONObject responseData;
        ArrayList<VPNLocation> locationList;
        try {
            responseData = makeAPIRequest("GET", "/location", "");
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
            Log.e(LOG_TAG, je.toString());
            return null;
        } catch (IOException ioe) {
            Log.e(LOG_TAG, ioe.toString());
            return null;
        }
    }

    // Private worker method that actually communicates with the Privacy Online API.
    private JSONObject makeAPIRequest(String method, String endPoint, String jsonPayload)
            throws IOException, JSONException {
        InputStream  inputStream  = null;
        OutputStream outputStream = null;
        String       apiUrl       = "https://api.privacy.online";
        String       apiKey       = this.context.getString(R.string.privacy_online_api_key);
        String       keyString    = "?key=" + apiKey;

        int payloadSize = jsonPayload.length();

        try {
            URL url = new URL(apiUrl + endPoint + keyString);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            // Sec 5 second connect/read timeouts
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");

            if (payloadSize > 0) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setFixedLengthStreamingMode(payloadSize);
            }

            // Initiate the connection
            connection.connect();

            // Write the payload if there is one.
            if (payloadSize > 0) {
                outputStream = connection.getOutputStream();
                outputStream.write(jsonPayload.getBytes("UTF-8"));
            }

            // Get the response.
            int responseCode = connection.getResponseCode();
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

    // Util method for reading the IO stream in a buffered manner.
    private String readInputStream(InputStream inputStream, int contentLength) throws IOException {
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        char[] buffer = new char[contentLength];
        reader.read(buffer);
        return new String(buffer);
    }
}
