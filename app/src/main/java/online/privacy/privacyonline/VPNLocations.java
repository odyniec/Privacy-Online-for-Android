package online.privacy.privacyonline;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Data representation of the embedded VPN Location list.
 */
public class VPNLocations {

    private Context context;
    private final JSONArray locations;
    private final String assetFileName = "locations.json";

    public VPNLocations(Context context) {
        this.context = context;
        locations = readJSONFromAssetFile(assetFileName);
    }

    public ArrayList<VPNLocation> getArrayList() {
        ArrayList<VPNLocation> vpnLocations = new ArrayList<>(locations.length());
        for (int i = 0; i < (locations.length()-1); i++) {
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
