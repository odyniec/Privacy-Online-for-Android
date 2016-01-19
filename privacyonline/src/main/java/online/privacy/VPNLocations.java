package online.privacy;
/**
 * Collection handler for sets of VPNLocation objects.
 *
 * Loads the list of locations from the specified locations JSON file, and provides them as an
 * ArrayList.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class VPNLocations {

    private Context context;
    private final JSONArray locations;
    private final String assetFileName = "locations_beta.json";

    public VPNLocations(Context context) {
        this.context = context;
        locations = readJSONFromAssetFile(assetFileName);
    }

    public ArrayList<VPNLocation> getArrayList() {
        ArrayList<VPNLocation> vpnLocations = new ArrayList<>(locations.length());
        for (int i = 0; i < (locations.length()); i++) {
            try {
                JSONObject jsonLocation = (JSONObject) locations.get(i);
                vpnLocations.add(
                    new VPNLocation(
                        jsonLocation.getString("label"),
                        jsonLocation.getString("hostname"),
                        jsonLocation.getString("flag"),
                        jsonLocation.getString("headerimage")
                    )
                );
            } catch (JSONException e) {
                Log.e("VPNLocations", "Unable to set up ArrayList from embedded JSON data: "+e.toString());
            }
        }
        return vpnLocations;
    }

    private JSONArray readJSONFromAssetFile(String assetFileName) {
        JSONArray jsonArray = null;
        try {
            StringBuilder buffer = new StringBuilder();
            InputStream jsonInputStream = context.getAssets().open(assetFileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(jsonInputStream, "UTF-8"));
            String json;
            while ((json = reader.readLine()) != null) {
                buffer.append(json);
            }
            jsonArray = new JSONArray(buffer.toString());
        } catch (IOException | JSONException e) {
            Log.e("VPNLocations", "Unable to read from asset json file: " + assetFileName);

        }
        return jsonArray;
    }
}
