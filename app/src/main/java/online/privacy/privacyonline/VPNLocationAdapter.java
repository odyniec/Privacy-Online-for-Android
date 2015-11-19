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
    private ArrayList<VPNLocation> values;

    public VPNLocationAdapter(Context context, int textViewResourceId, ArrayList<VPNLocation> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values  = values;
    }

    public int getCount(){
       return values.size();
    }

    public VPNLocation getItem(int position) {
       return values.get(position);
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
