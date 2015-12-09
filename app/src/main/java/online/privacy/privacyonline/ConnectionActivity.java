package online.privacy.privacyonline;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
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


public class ConnectionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "p.o.connection";
    private Activity activityConnection = this;
//    private GetLocationListReceiver locationReceiver;
    private VPNStatusReceiver vpnStatusReceiver;
    private VPNByteCountReceiver vpnByteCountReceiver;

    // This is online.privacy.VpnProfile, not de.blinkt.openvpn.VpnProfile, as I don't need the
    // profile management, nor half the guff in there.
    private ProfileManager profileManager;
    private VpnProfile openVPNProfile;
    private String vpnProfileName = "privacy-online";
    private File vpnCACertFile;
    private String vpnStatus;
    private boolean headerImageExpanded = false;
    private int headerTransitionChange = 0;

    private final int START_VPN_PROFILE = 100;
    private final int VPN_DISCONNECT    = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        Toolbar customToolBar = (Toolbar) findViewById(R.id.toolbar_connection);
        setSupportActionBar(customToolBar);
        customToolBar.setNavigationIcon(null);
        customToolBar.setNavigationContentDescription(null);

        vpnCACertFile = new File(getCacheDir(), "privacy-online-ca.crt");
        unpackCAFile();

        // Get the VPN Profile, or create one if we don't have one.
        profileManager = ProfileManager.getInstance(this);
        openVPNProfile = profileManager.getProfileByName(vpnProfileName);
        if (openVPNProfile == null) {
            PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
            utility.createVPNProfile(this, vpnProfileName);
        }

        // Populate the Location list.
        VPNLocations vpnLocations = new VPNLocations(this);
        ArrayList<VPNLocation> locationList = vpnLocations.getArrayList();
        final VPNLocationAdapter locationAdapter
                = new VPNLocationAdapter(this, R.layout.spinner_layout_full, locationList);
        PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
        utility.updateSpinnerValues(activityConnection, R.id.input_spinner_vpn_location,
                locationAdapter, new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                        VPNLocation vpnLocation = locationAdapter.getItem(position);
                        ImageView headerImageView = (ImageView) activityConnection.findViewById(R.id.header_image);

                        PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
                        utility.updateHeaderImage(activityConnection, headerImageView, vpnLocation);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {
                    }
                });

        // Add a dirty testing hack, to listen for a click on teh header image and expand it using
        // the expand animation.
        // TODO: Remove this, it's for testing the animation that should fire on connection
        headerImageExpanded = false; // Make sure we start with it set to false.
        ImageView headerImage = (ImageView) findViewById(R.id.header_image);
        headerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideHeaderImage(false, null);
            }
        });

        // Wire up the Dis/Connect buttons.
        final Button connectionButton = (Button) findViewById(R.id.button_connection);
        final Button disconnectButton = (Button) findViewById(R.id.button_disconnect);

        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchConnectionButtons(connectionButton, disconnectButton);

                SharedPreferences preferences
                        = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
                String username = preferences.getString("username", "");
                String password = preferences.getString("password", "");

                Spinner vpnLocationSpinner = (Spinner) findViewById(R.id.input_spinner_vpn_location);
                VPNLocation vpnServer = (VPNLocation) vpnLocationSpinner.getSelectedItem();

                openVPNProfile.mCaFilename = vpnCACertFile.getPath();
                openVPNProfile.mUsername = username;
                openVPNProfile.mPassword = password;
                openVPNProfile.mCipher = "AES-256-CBC";
                openVPNProfile.mAuth = "SHA256";

                Connection conn = new Connection();
                conn.mServerName = vpnServer.getHostname();
                openVPNProfile.mConnections[0] = conn;

                profileManager.addProfile(openVPNProfile);
                profileManager.saveProfileList(activityConnection);
                profileManager.saveProfile(activityConnection, openVPNProfile);

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

    }

    private void switchConnectionButtons(Button buttonToHide, Button buttonToShow) {
        Log.i("ConnectionActivity", "Dis/Connect button clicked");
        buttonToHide.setEnabled(false);
        buttonToHide.setVisibility(View.GONE);
        buttonToShow.setEnabled(true);
        buttonToShow.setVisibility(View.VISIBLE);
    }


    /**
     * Toggles the state of the header imeage. Goes from greyscale "small", to coloured big.
     * Note: Also hides the selection spinner when it slides out, and reveals it when it slides in.
     */
    private void slideHeaderImage(boolean forceState, String forcedState) {
        final ImageView view = (ImageView) findViewById(R.id.header_image);
        final Spinner vpnLocation = (Spinner) findViewById(R.id.input_spinner_vpn_location);

        if (forceState) {
            switch (forcedState) {
                case "closed":
                    if (!headerImageExpanded) {
                        return; // If it's already closed, do nothing.
                    }
                    headerImageExpanded = true;
                    break;
                case "open":
                    headerImageExpanded = false;
                    break;
            }
        }

        if (!headerImageExpanded) {
            int startHeight = view.getHeight();
            headerTransitionChange = getHeaderTransitionChange(); // (int) (startHeight * 0.5); // 50% bigger
            ExpandAnimation expandAnimation = new ExpandAnimation(view, headerTransitionChange, startHeight, 800);
            expandAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    headerImageExpanded = true;
                    ShrinkAnimation shrinkVpnSpinner = new ShrinkAnimation(vpnLocation, headerTransitionChange, headerTransitionChange, 800);
                    vpnLocation.startAnimation(shrinkVpnSpinner);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
                    utility.unsetGreyScale(view);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            view.startAnimation(expandAnimation);

        } else {
            int startHeight = view.getHeight();
            ShrinkAnimation shrinkAnimation = new ShrinkAnimation(view, headerTransitionChange, startHeight, 1000);
            shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    headerImageExpanded = false;

                    // This Kludge is apparently required, because animating from 0 fails in a fiery ball of death.
                    vpnLocation.getLayoutParams().height = 1;
                    ExpandAnimation expandVpnSpinner = new ExpandAnimation(vpnLocation, (headerTransitionChange-1), 1, 800);
                    vpnLocation.startAnimation(expandVpnSpinner);

                    PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
                    utility.setGreyScale(view);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            view.startAnimation(shrinkAnimation);
        }
    }

    private int getHeaderTransitionChange() {

        // If there isn't a value assigned to headerTransitionChange, then we need to get the height
        // of the location selection spinner, as the idea is to slide out to hide that.
        if (headerTransitionChange == 0) {
            Spinner vpnLocation = (Spinner) findViewById(R.id.input_spinner_vpn_location);
            headerTransitionChange = vpnLocation.getHeight();
        }
        return headerTransitionChange;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check for settings, and spew the set-up activity if we don't have any.
        if (!havePreferences()) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }

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

/*
        // Register the listener for the Spinner content update.
        IntentFilter locationFilter = new IntentFilter(ConnectionActivity.GetLocationListReceiver.API_RESPONSE);
        locationFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationReceiver = new GetLocationListReceiver();
        registerReceiver(locationReceiver, locationFilter);

        // Populate the Location list.
        Intent apiLocationIntent = new Intent(this, PrivacyOnlineAPIService.class);
        apiLocationIntent.setAction(PrivacyOnlineAPIService.ACTION_GET_LOCATIONS);
        apiLocationIntent.putExtra(PrivacyOnlineAPIService.EXTRA_CALLER, ConnectionActivity.GetLocationListReceiver.API_RESPONSE);
        startService(apiLocationIntent);
*/
    }

    @Override
    public void onStop() {
        super.onStop();
//        unregisterReceiver(locationReceiver);
        unregisterReceiver(vpnStatusReceiver);
        unregisterReceiver(vpnByteCountReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() { super.onDestroy(); }

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

    private void unpackCAFile() {

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


    private boolean havePreferences() {
        SharedPreferences preferences = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
        String locationDefault = preferences.getString("default_vpn_location", "");
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        return !(locationDefault.equals("") || username.equals("") || password.equals(""));
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == START_VPN_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                new startOpenVpnThread().start();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User does not want us to start, so we just vanish
                VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
                        VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED);
                finish();
            }

        } else if (requestCode == VPN_DISCONNECT) {
            if (resultCode == Activity.RESULT_OK) {
                Button connectionButton = (Button) findViewById(R.id.button_connection);
                Button disconnectButton = (Button) findViewById(R.id.button_disconnect);
                switchConnectionButtons(disconnectButton, connectionButton);

                slideHeaderImage(true, "closed");

                LinearLayout infoArea = (LinearLayout) findViewById(R.id.info_area_status);
                infoArea.setVisibility(View.GONE);

                Toast toast = Toast.makeText(activityConnection, "VPN Disconnected", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    private void updateConnectionStatusText(String status) {
        LinearLayout infoArea = (LinearLayout) findViewById(R.id.info_area_status);
        if (infoArea.getVisibility() != View.VISIBLE) {
            infoArea.setVisibility(View.VISIBLE);
        }
        TextView vpnStatus = (TextView) findViewById(R.id.vpn_status);
        vpnStatus.setText(status);
    }

    private void updateByteCountDisplay(String in, String out, String diffIn, String diffOut) {
        LinearLayout infoArea = (LinearLayout) findViewById(R.id.info_area_status);
        if (infoArea.getVisibility() != View.VISIBLE) {
            return; // Do nothing if we're not visible.
        }

        String byteCount = String.format("Up: %1$s/s %2$s Down: %3$ss/s %4$s", diffIn, in, diffOut, out);
        TextView vpnByteCount = (TextView) findViewById(R.id.vpn_bytecount);
        vpnByteCount.setText(byteCount);
    }

    private class startOpenVpnThread extends Thread {
        @Override
        public void run() {
            VPNLaunchHelper.startOpenVpn(openVPNProfile, getBaseContext());
            finish();

        }
    }
/*
    // Implement a receiver so we can use the APIService to check login details.
    public class GetLocationListReceiver extends BroadcastReceiver {

        public static final String API_RESPONSE =
                "online.privacy.privacyonline.intent.action.RESPONSE_GET_LOCATION";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Received Get Location Service Broadcast");
            ArrayList<VPNLocation> locationList
                    = intent.getParcelableArrayListExtra(PrivacyOnlineAPIService.CHECK_RESULT);
            // Stop the App crashing if we can't get stuff from the API
            // TODO - Connectivity checking, and graceful handling.
            if (locationList == null) {
                locationList = new ArrayList<>();
            }

            final VPNLocationAdapter locationAdapter
                    = new VPNLocationAdapter(context, android.R.layout.simple_spinner_item, locationList);

            PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
            utility.updateSpinnerValues(activityConnection, R.id.input_spinner_vpn_location,
                    locationAdapter, new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapter) {
                }
            });
        }
    }
*/

    public class VPNStatusReceiver extends BroadcastReceiver {

        public static final String ACTION = "de.blinkt.openvpn.VPN_STATUS";

        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("status");
            Log.e("ConnectionActivity", "VPN Status: "+status);
            vpnStatus = intent.getStringExtra("detailstatus");
            Log.e("ConnectionActivity", "VPN Status Detail: "+vpnStatus);
            updateConnectionStatusText(getString(VpnStatus.getLocalizedState(vpnStatus)));

            if (vpnStatus.equals("CONNECTED")) {
                slideHeaderImage(false, null);
            }
        }
    }

    public class VPNByteCountReceiver extends BroadcastReceiver {

        public static final String ACTION = "de.blinkt.openvpn.VPN_BYTECOUNT";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!vpnStatus.equals("CONNECTED")) {
                return;
            }
            String in = intent.getStringExtra("in");
            String out = intent.getStringExtra("out");
            String diffIn = intent.getStringExtra("diffin");
            String diffOut = intent.getStringExtra("diffout");
            updateByteCountDisplay(in, out, diffIn, diffOut);
        }
    }
}
