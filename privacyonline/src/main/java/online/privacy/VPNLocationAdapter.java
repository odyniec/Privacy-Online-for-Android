package online.privacy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import online.privacy.R;

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

    public VPNLocation getEntryByHostname(String hostname) {
        VPNLocation matchingEntry = null;

        for (int i = 0; i < this.values.size(); i++) {
            VPNLocation entry = this.values.get(i);
            if (entry.getHostname().equals(hostname)) {
                matchingEntry = entry;
            }
        }
        return matchingEntry;
    }

    public int getEntryLocationByLabel(String label) {
        int locationId = 0;
        for (int i = 0; i < this.values.size(); i++) {
            if (this.values.get(i).getLabel().equals(label)) {
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
        return getDropDownView(position, convertView, parent);
    }

    // And here is when the "chooser" is popped up
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(R.layout.spinner_layout_full, parent, false);
        }

        VPNLocation item = values.get(position);
        if (item != null) {
            ImageView flag    = (ImageView) row.findViewById(R.id.location_flag);
            TextView label    = (TextView)  row.findViewById(R.id.location_label);
            TextView hostname = (TextView)  row.findViewById(R.id.location_hostname);

            flag.setImageResource(
                context.getResources().getIdentifier(
                    item.getFlag(), "drawable", context.getPackageName()
                )
            );
            label.setText(item.getLabel());
            hostname.setText(item.getHostname());
        }

        return row;
    }
}
