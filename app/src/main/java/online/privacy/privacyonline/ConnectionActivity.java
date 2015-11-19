package online.privacy.privacyonline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;


public class ConnectionActivity extends AppCompatActivity {

    private static final String PRIVACYONLINE_PREFERENCES = "online.privacy.privacyonline.PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for settings, and spew the set-up activity if we don't have any.
        if (!havePreferences()) {
            Intent intent = new Intent(this, SetupActivity.class);
            startActivity(intent);
        }
    }

    private boolean havePreferences() {
        SharedPreferences preferences = getSharedPreferences(PRIVACYONLINE_PREFERENCES, MODE_PRIVATE);
        String locationDefault = preferences.getString("locationDefault", "");
        String username        = preferences.getString("username", "");
        String password        = preferences.getString("password", "");
        return !(locationDefault.equals("") && username.equals("") && password.equals(""));
    }


//    // Implement a receiver so we can use the APIService to check login details.
//    public class LoginDetailsCheckReceiver extends BroadcastReceiver {
//
//        public static final String ACTION_REPONSE =
//                "online.privacy.privacyonline.intent.action.USERCHECK_RESPONSE";
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.i(LOG_TAG_HOME, "Received Service Broadcast");
//            boolean checkResult = intent.getBooleanExtra(PrivacyOnlineAPIService.CHECK_RESULT, false);
//
//            // If the details were good, launch the ConnectionActivity.
//            if (checkResult == true) {
//                Log.i(LOG_TAG_HOME, "User Account verified");
//                startLocationActivity();
//            } else {
//                Log.i(LOG_TAG_HOME, "User Account verification failed");
//                EditText inputTextUsername = (EditText) findViewById(R.id.input_text_username);
//                EditText inputTextPassword = (EditText) findViewById(R.id.input_password_password);
//
//                inputTextUsername.setBackgroundColor(Color.RED);
//                inputTextPassword.setBackgroundColor(Color.RED);
//            }
//        }
//    }

}
