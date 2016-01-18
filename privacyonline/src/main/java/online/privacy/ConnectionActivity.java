package online.privacy;
/**
 * ConnectionAcvitity
 *
 * This is the MAIN launch point of the Privacy Online for Android App.
 * It handles selection of OpenVPN endpoint and starting the OpenVPN service.
 *
 * Is sets defaults for the spinner based on the settings defined in the SetupActivity, along with
 * using the username and password set therein.  If no settings are detected (e.g. on first start)
 * then the SetupActivity is launched.
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
 */
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.activities.DisconnectVPN;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.core.Connection;

//
// TODO - Better connectivity checking, and MOAR graceful handling.
//
// The whole app needs to sense the fact that network is unavailable. And act accordingly.
//
public class ConnectionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "p.o.connection";
    private Activity activityConnection = this;
    private VPNStatusReceiver vpnStatusReceiver;
    private VPNByteCountReceiver vpnByteCountReceiver;

    private PrivacyOnlineUtility poUtility = new PrivacyOnlineUtility();
    private HeaderImageView headerImageView;

    private ProfileManager profileManager;
    private VpnProfile openVPNProfile;
    private String vpnProfileName = "privacy-online";
    private File vpnCACertFile;
    private String vpnStatus;
    private boolean authFailed = false;

    private final int START_VPN_PROFILE = 100;
    private final int VPN_DISCONNECT    = 101;

    /**
     * Nasty-Fucking-Hack-Time!!!!one
     *
     * The AppCompat theme doesn't let you see the menu text color differently to the toolbar
     * action item widgets, so to prevent a shitty menu with white-on-white, we'll pop the
     * soft menu if the hardware menu key is pressed.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent e) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (getSupportActionBar() != null) {
                    getSupportActionBar().openOptionsMenu();
                    return true;
                }
        }

        return super.onKeyUp(keyCode, e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connection);
        Toolbar customToolBar = (Toolbar) findViewById(R.id.toolbar_connection);
        setSupportActionBar(customToolBar);
        customToolBar.setNavigationIcon(null);
        customToolBar.setNavigationContentDescription(null);

        // Make sure we have a status, straight away.
        vpnStatus = VpnStatus.getVpnStatus();

        // Ensure we have the CA unpacked.
        vpnCACertFile = new File(getCacheDir(), "privacy-online-ca.crt");
        unpackCAFile();

        // Get the VPN Profile, or create one if we don't have one.
        profileManager = ProfileManager.getInstance(this);
        openVPNProfile = profileManager.getProfileByName(vpnProfileName);
        if (openVPNProfile == null) {
            poUtility.createVPNProfile(activityConnection, vpnProfileName);
            openVPNProfile = profileManager.getProfileByName(vpnProfileName);
        }

        // Setup the location spinner with the available locations.
        updateLocationSpinner();

        // create an in-memory object for the header image so it can track it's own expaned/collapsed
        // animation state, and tell it which view it'll be working with.
        headerImageView = (HeaderImageView) findViewById(R.id.header_image);
        headerImageView.setCompanionView(R.id.input_spinner_vpn_location);

        // Wire up the Dis/Connect buttons.
        final Button connectionButton = (Button) findViewById(R.id.button_connection);
        final Button disconnectButton = (Button) findViewById(R.id.button_disconnect);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchConnectionButtons(false);

                SharedPreferences preferences
                        = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
                String username = preferences.getString("username", "");
                String password = preferences.getString("password", "");

                Spinner vpnLocationSpinner = (Spinner) findViewById(R.id.input_spinner_vpn_location);
                VPNLocation vpnServer = (VPNLocation) vpnLocationSpinner.getSelectedItem();

                openVPNProfile.mName = vpnProfileName;
                openVPNProfile.mCaFilename = vpnCACertFile.getPath();
                openVPNProfile.mUsername = username;
                openVPNProfile.mPassword = password;
                openVPNProfile.mServerName = vpnServer.getHostname();
                openVPNProfile.mCipher = "AES-256-CBC";
                openVPNProfile.mAuth = "SHA256";
                openVPNProfile.mCheckRemoteCN = false;

                Connection conn = new Connection();
                conn.mServerName = vpnServer.getHostname();
                openVPNProfile.mConnections[0] = conn;

                profileManager.addProfile(openVPNProfile);
                ProfileManager.setConnectedVpnProfile(activityConnection, openVPNProfile);
                profileManager.saveProfileList(activityConnection);
                profileManager.saveProfile(activityConnection, openVPNProfile);

                authFailed = false;
                Intent intent = new Intent(activityConnection, LaunchVPN.class);
                intent.putExtra(LaunchVPN.EXTRA_KEY, openVPNProfile.getUUID().toString());
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);

            }
        });

        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent disconnectVPN = new Intent(activityConnection, DisconnectVPN.class);
                disconnectVPN.setAction("de.blinkt.openvpn.DISCONNECT_VPN");
                startActivityForResult(disconnectVPN, VPN_DISCONNECT);
            }
        });

        // If the VPN is connected, update the status to reflect that.
        if (vpnIsConnected()) {
            Log.e(LOG_TAG, "VPN is connected, restoring connected state.");
            switchConnectionButtons(false);
            headerImageView.setOpen();
            showStatusBox();

        } else {
            // If we're not connected, we need to make sure that the HeaderImageView knows it.
            // An unexpected exit while connected may cause it to think we're connected, which
            // would negate the slideOpen() call.
            headerImageView.setIsExpanded(false);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        // Check for settings, and spew the set-up activity if we don't have any.
        if (!havePreferences()) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register the listener for the VPN Status update.
        IntentFilter vpnStatusFilter = new IntentFilter(VPNStatusReceiver.ACTION);
        vpnStatusFilter.addCategory(Intent.CATEGORY_DEFAULT);
        vpnStatusReceiver = new VPNStatusReceiver();
        registerReceiver(vpnStatusReceiver, vpnStatusFilter);

        // And the ByteCount Broadcast.
        IntentFilter vpnByteCountFilter = new IntentFilter(VPNByteCountReceiver.ACTION);
        vpnByteCountFilter.addCategory(Intent.CATEGORY_DEFAULT);
        vpnByteCountReceiver = new VPNByteCountReceiver();
        registerReceiver(vpnByteCountReceiver, vpnByteCountFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(vpnStatusReceiver);
        unregisterReceiver(vpnByteCountReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connectionlocation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SetupActivity.class);
            startActivity(settingsIntent);
        } else if (id == R.id.action_about) {
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_VPN_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                new startOpenVpnThread().start();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User does not want us to start, so we just vanish... *poof*
                VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
                        VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED);
                finish();
            }

        } else if (requestCode == VPN_DISCONNECT) {
            if (resultCode == Activity.RESULT_OK) {
                switchConnectionButtons(true);
                hideStatusBoxAndCloseHeader();
                Toast toast = Toast.makeText(activityConnection, "VPN Disconnected", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    /**
     *  When the app is re-opened from the notification bar, or rotated, the activity is effectively
     *  restarted. So we need to know whether the VPN is connected or not, and from that set the UI
     *  accordingly so we don't crash in a horrible ball of fire trying to update invisible elements,
     *  and ensure that we present the disconnect button so the VPN can be stopped.
     */
    private boolean vpnIsConnected() {

        // Get the VPN status, if it's "not connected", then do nothing - all other statuses are
        // worthy of the UI being in the "Active" mode.
        vpnStatus = VpnStatus.getVpnStatus();
        return !(  vpnStatus.equals("DISCONNECTED")
                || vpnStatus.equals("EXITING")
                || vpnStatus.equals("NOPROCESS"));
    }

    // Unpacks the CA file from the APK so that the OpenVPN profile stuff can use it.
    // TODO - Should probably have some file version check so we can overwrite it with a newer version if required.
    private void unpackCAFile() {

        // Check to see if we have already unpacked the CA File. If so, then we need not bother.
        if (vpnCACertFile.exists()) {
            return;
        }

        // So we can't use a file in the .apk as a CA file for our VPN connection profile.
        // We'll unpack it here to the cache directory and pass that to the VpnProfile.
        try {
            InputStream inputStream = getAssets().open("privacy-online-ca.crt");
            try {
                FileOutputStream outputStream = new FileOutputStream(vpnCACertFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e("ConnectionActivity", "Unable to write CA cert to cache directory");
        }
    }

    // Checks that we have preferences we can use to make a VPN connection.
    private boolean havePreferences() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
        String locationDefault = preferences.getString("default_vpn_location", "");
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        return !(locationDefault.equals("") || username.equals("") || password.equals(""));
    }

    // Populates the location spinner. The actual population code resides in PrivacyOnlineUtility as
    // it's also used in SetupActivity.
    private void updateLocationSpinner() {
        // Populate the Location list.
        VPNLocations vpnLocations = new VPNLocations(this);
        ArrayList<VPNLocation> locationList = vpnLocations.getArrayList();
        final VPNLocationAdapter locationAdapter
                = new VPNLocationAdapter(this, R.layout.spinner_layout_full, locationList);
        PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
        utility.updateSpinnerValues(activityConnection, R.id.input_spinner_vpn_location,
                locationAdapter, vpnIsConnected(), new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        VPNLocation vpnLocation = locationAdapter.getItem(position);
                        headerImageView.changeImageToAsset(vpnLocation.getHeaderImage(), vpnIsConnected());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {
                    }
                });
    }

    // UI code to change the connection button to reflect the VPN status, and therefore action.
    private void switchConnectionButtons(boolean showConnect) {
        final Button connectionButton = (Button) findViewById(R.id.button_connection);
        final Button disconnectButton = (Button) findViewById(R.id.button_disconnect);

        if (showConnect) {
            disconnectButton.setEnabled(false);
            disconnectButton.setVisibility(View.GONE);
            connectionButton.setEnabled(true);
            connectionButton.setVisibility(View.VISIBLE);

        } else {
            connectionButton.setEnabled(false);
            connectionButton.setVisibility(View.GONE);
            disconnectButton.setEnabled(true);
            disconnectButton.setVisibility(View.VISIBLE);
        }
    }

    // UI code to manipulate the StatusBox, within the header section.
    private void showStatusBox() {
        LinearLayout statusBox = (LinearLayout) findViewById(R.id.status_box);
        if (statusBox.getVisibility() != View.VISIBLE) {
            statusBox.setVisibility(View.VISIBLE);
        }
        setByteCountText("0", "0", "0", "0");
        TextView statusText = (TextView) findViewById(R.id.status_connection_state);
        if (statusText.getText() == null || statusText.getText().toString().equals("")) {
            statusText.setText(VpnStatus.getLocalizedState(vpnStatus));
        }
    }

    // inverse UI code to showStatusBox() - Now with pretty animations! :)
    private void hideStatusBoxAndCloseHeader() {
        final LinearLayout statusBox = (LinearLayout) findViewById(R.id.status_box);

        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                statusBox.setVisibility(View.GONE);
                headerImageView.slideClosed();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        statusBox.startAnimation(fadeOut);
    }

    // UI code for status box updates.
    private void updateConnectionStatusText(String status) {
        showStatusBox();
        TextView vpnStatus = (TextView) findViewById(R.id.status_connection_state);
        vpnStatus.setText(status);
    }

    // UI code for the status box.
    private void setByteCountText(String down, String diffDown, String up, String diffUp) {
        TextView dataInfoDownDiff  = (TextView) findViewById(R.id.status_data_down_diff);
        TextView dataInfoDownTotal = (TextView) findViewById(R.id.status_data_down_total);
        TextView dataInfoUpDiff    = (TextView) findViewById(R.id.status_data_up_diff);
        TextView dataInfoUpTotal   = (TextView) findViewById(R.id.status_data_up_total);

        dataInfoDownDiff.setText(diffDown + "/s");
        dataInfoDownTotal.setText(down);
        dataInfoUpDiff.setText(diffUp + "/s");
        dataInfoUpTotal.setText(up);
    }

    // More UI code for the status box.
    private void updateByteCountDisplay(String down, String up, String diffDown, String diffUp) {
        LinearLayout statusBox = (LinearLayout) findViewById(R.id.status_box);
        if (statusBox.getVisibility() != View.VISIBLE) {
            return; // Do nothing if we're not visible.
        }
        setByteCountText(down, diffDown, up, diffUp);
    }

    // Private Sub-class used to actually launch the VPN connection.
    private class startOpenVpnThread extends Thread {
        @Override
        public void run() {
            VPNLaunchHelper.startOpenVpn(openVPNProfile, getBaseContext());
            finish();

        }
    }

    /**
     * VPNStatusReceiver
     *
     * Public sub-class that handles the Broadcast's from the OpenVPN Status code.
     *
     * Used to update the UI with the current status of the VPN connection. Including the text and
     * HeaderImageView animation state.
     */
    public class VPNStatusReceiver extends BroadcastReceiver {

        public static final String ACTION = "de.blinkt.openvpn.VPN_STATUS";

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("status");
            vpnStatus = intent.getStringExtra("detailstatus");

            if (vpnStatus.equals("AUTH_FAILED")) {
                authFailed = true;
                updateConnectionStatusText(getString(VpnStatus.getLocalizedState(vpnStatus)));
                switchConnectionButtons(true);
            }

            // Only update this if we haven't had an authfailure.
            if (!authFailed) {
                updateConnectionStatusText(getString(VpnStatus.getLocalizedState(vpnStatus)));
            }

            if (vpnStatus.equals("CONNECTED")) {
                headerImageView.slideOpen();
            } else if (status.equals("LEVEL_NOTCONNECTED")) {
                headerImageView.slideClosed();
            }
        }
    }


    /**
     * VPNByteCountReceiver
     *
     * Public sub-class responsible for receiving and handling the Broadcast's from the OpenVPN
     * Status code.
     *
     * Updates the StatusBox UI with the current tracked data up/down statistics.
     */
    public class VPNByteCountReceiver extends BroadcastReceiver {

        public static final String ACTION = "de.blinkt.openvpn.VPN_BYTECOUNT";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!vpnStatus.equals("CONNECTED")) {
                return;
            }
            String down = intent.getStringExtra("in");
            String up = intent.getStringExtra("out");
            String diffDown = intent.getStringExtra("diffin");
            String diffUp = intent.getStringExtra("diffout");
            updateByteCountDisplay(down, up, diffDown, diffUp);
        }
    }
}
