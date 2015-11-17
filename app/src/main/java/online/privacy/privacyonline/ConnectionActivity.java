package online.privacy.privacyonline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class ConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_location);



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
