package online.privacy.privacyonline;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Spinner;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ProfileManager;

/**
 * PrivacyOnlineUtility - Class holding common code used in more than one location.
 */
public class PrivacyOnlineUtility {

    final private String LOG_TAG = "p.o.utility";

    public void updateSpinnerValues(Activity activity,
                                    int spinnerID,
                                    VPNLocationAdapter locationAdapter,
                                    AdapterView.OnItemSelectedListener onItemSelectedListener) {
        Context context = activity.getApplicationContext();

        SharedPreferences preferences
                = context.getSharedPreferences(activity.getString(R.string.privacyonline_preferences), Context.MODE_PRIVATE);

        final String currentDefaultLocation = preferences.getString("default_vpn_location", "");
        Log.i(LOG_TAG, "Default VPN Location: " + currentDefaultLocation);

        final Spinner defaultVPNLocationSpinner = (Spinner) activity.findViewById(spinnerID);
        defaultVPNLocationSpinner.setAdapter(locationAdapter);

        // Set the current selected item to be the default preference.
        if (!currentDefaultLocation.equals("")) {
            int currentDefaultItemPosition
                    = locationAdapter.getEntryLocationByHostname(currentDefaultLocation);
            defaultVPNLocationSpinner.setSelection(currentDefaultItemPosition);
        }

        defaultVPNLocationSpinner.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void createVPNProfile(Context context, String name) {
        ProfileManager profileManager = ProfileManager.getInstance(context);
        VpnProfile openVPNProfile = new VpnProfile(name);
        profileManager.addProfile(openVPNProfile);
        profileManager.saveProfileList(context);
        profileManager.saveProfile(context, openVPNProfile);
    }
}
