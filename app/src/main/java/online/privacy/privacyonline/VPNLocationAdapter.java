package online.privacy.privacyonline;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * Custom SpinnerAdapter for the Spinners allowing VPN location selection
 */
public class VPNLocationAdapter extends ArrayAdapter<VPNLocation> {

    private Context       context;
    private VPNLocation[] values;

    public VPNLocationAdapter(Context context, int textViewResourceId, VPNLocation[] values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values  = values;
    }

    public int getCount(){
       return values.length;
    }

    public VPNLocation getItem(int position){
       return values[position];
    }

    public long getItemId(int position){
       return position;
    }

    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values[position].getLabel());
        return label;
    }

    // And here is when the "chooser" is popped up
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(values[position].getLabel());
        return label;
    }
}
