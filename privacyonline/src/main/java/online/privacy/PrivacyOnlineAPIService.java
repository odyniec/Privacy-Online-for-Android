package online.privacy;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PrivacyOnlineAPIService extends IntentService {

    private static final String LOG_TAG = "p.o.api.service";
    public static final String ACTION_VERIFY_USERNAME
            = "online.privacy.privacyonline.action.VERIFY_USERNAME";
    public static final String ACTION_GET_LOCATIONS
            = "online.privacy.privacyonline.action.GET_LOCATIONS";

    public static final String EXTRA_CALLER   = "onling.privacy.privacyonline.extra.CALLER";
    public static final String PARAM_USERNAME = "online.privacy.privacyonline.extra.USERNAME";
    public static final String PARAM_PASSWORD = "online.privacy.privacyonline.extra.PASSWORD";
    public static final String CHECK_RESULT   = "online.privacy.privacyonline.extra.CHECK_RESULT";

    public PrivacyOnlineAPIService() {
        super("PrivacyOnlineAPIService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.i(LOG_TAG, "Attempting to Handle Service");
            final String action = intent.getAction();
            Log.i(LOG_TAG, "Service action: "+action);

            if (ACTION_VERIFY_USERNAME.equals(action)) {
                Log.i(LOG_TAG, "Action is to verify user account");
                final String username = intent.getStringExtra(PARAM_USERNAME);
                final String password = intent.getStringExtra(PARAM_PASSWORD);
                handleActionVerifyUserAccount(username, password);

            } else if (ACTION_GET_LOCATIONS.equals(action)) {
                Log.i(LOG_TAG, "Action is to get location list.");
                String caller = intent.getStringExtra(EXTRA_CALLER);
                handleActionGetLocationList(caller);
            }
        }
    }

    private void handleActionVerifyUserAccount(String username, String password) {
        Log.i(LOG_TAG, "Verify User handler executed.");
        PrivacyOnlineApiRequest requestPrivacyAPI = new PrivacyOnlineApiRequest();
        boolean checkResult = requestPrivacyAPI.verifyUserAccount(username, password);
        broadcastVerifyAccountResult(checkResult);
    }

    private void handleActionGetLocationList(String caller) {
        Log.i(LOG_TAG, "Get Location List handler executed.");
        PrivacyOnlineApiRequest requestPrivacyAPI = new PrivacyOnlineApiRequest();
        ArrayList<VPNLocation> locationList = requestPrivacyAPI.getLocationList();
        broadcastGetLocationListResult(caller, locationList);
    }

    private void broadcastVerifyAccountResult(boolean checkResult) {
        Log.i(LOG_TAG, "Broadcasting response.");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SetupActivity.VerifyUserAccountReceiver.API_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CHECK_RESULT, checkResult);
        sendBroadcast(broadcastIntent);
    }

    private void broadcastGetLocationListResult(String caller, ArrayList<VPNLocation> checkResult) {
        Log.i(LOG_TAG, "Broadcasting response.");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(caller);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CHECK_RESULT, checkResult);
        sendBroadcast(broadcastIntent);
    }
}
