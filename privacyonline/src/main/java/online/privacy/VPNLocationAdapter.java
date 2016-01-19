package online.privacy;
/**
 * Custom Adaptor for managing the list of locations for the Spinner UI element.
 *
 * Copyright © 2016, privacy.online
 * All rights reserved.
 *
 * This file is part of Privacy Online for Android.
 *
 * Privacy Online for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Privacy Online for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Privacy Online for Android.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author James Ronan <jim@dev.uk2.net>
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class VPNLocationAdapter extends ArrayAdapter<VPNLocation> {

    private Context                context;
    private ArrayList<VPNLocation> values = new ArrayList<>();

    public VPNLocationAdapter(Context context, int textViewResourceId, ArrayList<VPNLocation> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values  = values;
    }

    /**
     * Returns the number of VPNLocations currently in the list.
     *
     * @return Number of VPNLocations in the list.
     */
    @Override
    public int getCount(){
       return this.values.size();
    }

    /**
     * Returns the VPNLocation item at the specified position.
     *
     * @param  position Index position of the required VPNLocation item.
     * @return          VPNLoaction data object describing the endpoint.
     */
    @Override
    public VPNLocation getItem(int position) {
       return this.values.get(position);
    }

    /**
     * Returns the VPNLocation of the specified Hostname.
     *
     * @param  hostname Hostname of the desired location endpoint
     * @return          VPNLocation data object describing the endpoint.
     */
    public VPNLocation getEntryByHostname(String hostname) {
        VPNLocation matchingLocation = null;
        for (VPNLocation location : this.values) {
            if (location.getHostname().equals(hostname)) {
                matchingLocation = location;
            }
        }
        return matchingLocation;
    }

    /**
     * Returns the VPNLocation index position in the ArrayAdapter list which has the specified
     * hostname.
     *
     * @param hostname Hostname of the desired location endpoint.
     * @return         VPNLocation data object describing the endpoint.
     */
    public int getEntryLocationByHostname(String hostname) {
        int locationId = 0;
        for (int i = 0; i < this.values.size(); i++) {
            if (this.values.get(i).getHostname().equals(hostname)) {
                locationId = i;
            }
        }
        return locationId;
    }

    /**
     * Returns the VPNLocation index position in the ArrayAdapter list which has the specified
     * label.
     *
     * @param  label Label of the desired location endpoint.
     * @return       VPNLocation data object describing the endpoint.
     */
    public int getEntryLocationByLabel(String label) {
        int locationId = 0;
        for (int i = 0; i < this.values.size(); i++) {
            if (this.values.get(i).getLabel().equals(label)) {
                locationId = i;
            }
        }
        return locationId;
    }

    /**
     * Returns the row id of the specified item.
     *
     * @param  position Index of item to get row id for.
     * @return          Row id - in this case, it's just the index.
     */
    @Override
    public long getItemId(int position){
       return position;
    }

    // This is for the "passive" state of the spinner

    /**
     * Returns the view associated with the VPNLocation at the specified index in the list.
     *
     * @param  position    Index position of the desired VPNLocation
     * @param  convertView An existing View to reuse, if applicable.
     * @param  parent      The parent view this will be attached to.
     * @return             An inflated View containing the data from the VPNLocation at the specified index.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }

    // And here is when the "chooser" is popped up

    /**
     * Returns the View for an element in the Spinner's dropdown list, at the specified index.
     *
     * @param  position    Index position of the desired VPNLocation.
     * @param  convertView An existing view to reuse, if applicable.
     * @param  parent      The parent view this will be attached to.
     * @return             An inflated View containing the data from the VPNLocation at the specified index.
     */
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
