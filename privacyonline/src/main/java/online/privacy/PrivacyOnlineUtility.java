package online.privacy;
/**
 * Utility class containing common code that is used in more that one Activity.
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
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Spinner;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ProfileManager;

public class PrivacyOnlineUtility {

    final private String LOG_TAG = "p.o.utility";

    /**
     * Takes the Android resource view Id and an Adapted array of locations, along with a listener
     * to handle events, and sets up the common Spinner element.
     *
     * @param activity               Activity context for the spinner view.
     * @param spinnerID              Android resource view ID of the Spinner view element.
     * @param locationAdapter        ArrayAdapted VPNLocation list of VPN locations that can be connected to.
     * @param vpnIsConnected         Boolean indication of whether the VPN is currently connected.
     * @param onItemSelectedListener OnItemSeletedListener implementation to attach to the Spinner.
     */
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

    /**
     * Creates a new VpnProfile instance within the ProfileManager for use in connecting the VPN.
     *
     * @param context Activity context for ProfileManager.
     * @param name    The name to give to this profile instance.
     */
    public void createVPNProfile(Context context, String name) {
        ProfileManager profileManager = ProfileManager.getInstance(context);
        VpnProfile openVPNProfile = new VpnProfile(name);

        profileManager.addProfile(openVPNProfile);
        profileManager.saveProfileList(context);
        profileManager.saveProfile(context, openVPNProfile);
    }
}
