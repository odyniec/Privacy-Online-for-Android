package online.privacy.privacyonline;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import de.blinkt.openvpn.LaunchVPN;
import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.core.Connection;


public class ConnectionActivity extends AppCompatActivity {

    private static final String LOG_TAG = "p.o.connection";
    private Activity activityConnection = this;
    private GetLocationListReceiver locationReceiver;

    // This is online.privacy.VpnProfile, not de.blinkt.openvpn.VpnProfile, as I don't need the
    // profile management, nor half the guff in there.
    private ProfileManager profileManager;
    private VpnProfile openVPNProfile;
    private String vpnProfileName = "privacy-online";
    private File vpnCACertFile;

    private final int START_VPN_PROFILE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_connection);
        vpnCACertFile = new File(getCacheDir(), "privacy-online-ca.crt");
        unpackCAFile();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check for settings, and spew the set-up activity if we don't have any.
        if (!havePreferences()) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }

        // Get the VPN Profile, or create one if we don't have one.
        profileManager = ProfileManager.getInstance(this);
        openVPNProfile = profileManager.getProfileByName(vpnProfileName);
        if (openVPNProfile == null) {
            PrivacyOnlineUtility utility = new PrivacyOnlineUtility();
            utility.createVPNProfile(this, vpnProfileName);
        }

        // Registrer the listerner for the Spinner content update.
        IntentFilter locationFilter = new IntentFilter(ConnectionActivity.GetLocationListReceiver.API_RESPONSE);
        locationFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationReceiver = new GetLocationListReceiver();
        registerReceiver(locationReceiver, locationFilter);

        // Populate the Location list.
        Intent apiLocationIntent = new Intent(this, PrivacyOnlineAPIService.class);
        apiLocationIntent.setAction(PrivacyOnlineAPIService.ACTION_GET_LOCATIONS);
        apiLocationIntent.putExtra(PrivacyOnlineAPIService.EXTRA_CALLER, ConnectionActivity.GetLocationListReceiver.API_RESPONSE);
        startService(apiLocationIntent);

        // Set the Connect button so it actually conencts.
        Button connectionButton = (Button) findViewById(R.id.button_connection);
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences
                        = getSharedPreferences(getString(R.string.privacyonline_preferences), MODE_PRIVATE);
                String username = preferences.getString("username", "");
                String password = preferences.getString("password", "");

                Spinner vpnLocationSpinner = (Spinner) findViewById(R.id.input_spinner_vpn_location);
                VPNLocation vpnServer = (VPNLocation) vpnLocationSpinner.getSelectedItem();

                openVPNProfile.mCaFilename = vpnCACertFile.getPath();
                openVPNProfile.mUsername = username;
                openVPNProfile.mPassword = password;
                openVPNProfile.mCipher   = "AES-256-CBC";
                openVPNProfile.mAuth     = "SHA256";

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
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(locationReceiver);
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
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//                boolean showLogWindow = prefs.getBoolean("showlogwindow", true);
//                if(!mhideLog && showLogWindow)
//                    showLogWindow();
                new startOpenVpnThread().start();

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User does not want us to start, so we just vanish
                VpnStatus.updateStateString("USER_VPN_PERMISSION_CANCELLED", "", R.string.state_user_vpn_permission_cancelled,
                        VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED);
                finish();
            }
        }
    }


    // Method prepares and attempts to launch the VPN connection.
    void launchVPN() {

        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            VpnStatus.updateStateString("USER_VPN_PERMISSION", "", R.string.state_user_vpn_permission,
                    VpnStatus.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT);
            // Start the query
            try {
                startActivityForResult(intent, START_VPN_PROFILE);
            } catch (ActivityNotFoundException ane) {
                // Shame on you Sony! At least one user reported that
                // an official Sony Xperia Arc S image triggers this exception
                VpnStatus.logError(R.string.no_vpn_support_image);
                //showLogWindow();
            }
        } else {
            onActivityResult(START_VPN_PROFILE, Activity.RESULT_OK, null);
        }
    }

    private class startOpenVpnThread extends Thread {
        @Override
        public void run() {
            VPNLaunchHelper.startOpenVpn(openVPNProfile, getBaseContext());
            finish();

        }
    }

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
}
