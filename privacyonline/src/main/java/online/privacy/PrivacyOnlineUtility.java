package online.privacy;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.io.IOException;
import java.io.InputStream;

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
                                    boolean vpnIsConnected,
                                    AdapterView.OnItemSelectedListener onItemSelectedListener) {
        Context context = activity.getApplicationContext();

        SharedPreferences preferences
                = context.getSharedPreferences(activity.getString(R.string.privacyonline_preferences), Context.MODE_PRIVATE);

        final String currentDefaultLocation = preferences.getString("default_vpn_location", "");
        Log.i(LOG_TAG, "Default VPN Location: " + currentDefaultLocation);

        final Spinner vpnLocationSpinner = (Spinner) activity.findViewById(spinnerID);
        vpnLocationSpinner.setAdapter(locationAdapter);

        // Set the current selected item to be the default preference.
        Log.i(LOG_TAG, "vpnIsConnected: "+vpnIsConnected);
        if (!currentDefaultLocation.equals("") || !vpnIsConnected) {
            int currentDefaultItemPosition
                    = locationAdapter.getEntryLocationByHostname(currentDefaultLocation);
            vpnLocationSpinner.setSelection(currentDefaultItemPosition);
        }

        if (vpnIsConnected) {

            // If we've been relaunched from the StatusBar Notification's PendingIntent, then
            // we need to set the location to where ever we're connected too, as it forces a reset
            // of the app.
            VpnProfile openVPNProfile = ProfileManager.getLastConnectedProfile(activity, false);
            String server = openVPNProfile.mServerName;
            Log.e(LOG_TAG, "Restarted from notification, setting image to location: "+server);
            vpnLocationSpinner.setSelection(locationAdapter.getEntryLocationByHostname(server));
        }

        vpnLocationSpinner.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void createVPNProfile(Context context, String name) {
        ProfileManager profileManager = ProfileManager.getInstance(context);
        VpnProfile openVPNProfile = new VpnProfile(name);

        profileManager.addProfile(openVPNProfile);
        profileManager.saveProfileList(context);
        profileManager.saveProfile(context, openVPNProfile);
    }
}