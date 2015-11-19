package online.privacy.privacyonline;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom SpinnerAdapter for the Spinners allowing VPN location selection
 */
public class VPNLocationAdapter extends ArrayAdapter<VPNLocation> {

    private Context                context;
    private ArrayList<VPNLocation> values = new ArrayList<>();

    public VPNLocationAdapter(Context context, int textViewResourceId, ArrayList<VPNLocation> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values  = values;
    }

    public int getCount(){
       return this.values.size();
    }

    public VPNLocation getItem(int position) {
       return this.values.get(position);
    }

    public VPNLocation getItemByHostname(String hostname) {
        VPNLocation matchingLocation = new VPNLocation("Unknown", "Unknown");
        for (VPNLocation location : this.values) {
            if (location.getHostname().equals(hostname)) {
                matchingLocation = location;
            }
        }
        return matchingLocation;
    }

    public int getEntryLocationByHostname(String hostname) {
        int locationId = 0;
        for (int i = 0; i < this.values.size(); i++) {
            if (this.values.get(i).getHostname().equals(hostname)) {
                locationId = i;
            }
        }
        return locationId;
    }

    public long getItemId(int position){
       return position;
    }

    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getLabel());
        return label;
    }

    // And here is when the "chooser" is popped up
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values.get(position).getLabel());
        return label;
    }
}
