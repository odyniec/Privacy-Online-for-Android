package online.privacy.privacyonline;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

/**
 * Custom SpinnerAdapter for the Spinners allowing VPN location
 *
 */
public class VPNLocationAdapter extends ArrayAdapter<VPNLocation> {

    private Context       context;
    private VPNLocation[] values;

    public VPNLocationAdapter(Context context, int textViewResourceId, VPNLocation[] values) {

    }

}
