package online.privacy;
/**
 * VPNLocation
 *
 * Parcelable data class that represents a single VPN location endpoint. Loaded with the
 * data stored in the locations JSON file, provides accessors for the hostname, header image and flag
 * icon assets, as allows them to be passed around in a Parcelable manner.
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
 */
import android.os.Parcel;
import android.os.Parcelable;

public class VPNLocation implements Parcelable {

    private String _hostname;    // Endpoint connection hostname.
    private String _label;       // Human readable location name.
    private String _flag;        // Asset filename for flag icon.
    private String _headerImage; // Asset filename for the header image.

    public VPNLocation() {}
    public VPNLocation(String label, String hostname) {
        this._label = label;
        this._hostname = hostname;
    }
    public VPNLocation(String label, String hostname, String flag, String headerImage) {
        this._label = label;
        this._hostname = hostname;
        this._flag = flag;
        this._headerImage = headerImage;
    }

    /**
     * setHostname - Setter for the Hostname member.
     *
     * Sets the hostname for this instance of VPNLocation.
     *
     * @param hostname Hostname of the endpoint location.
     */
    public void setHostname(String hostname) {
        this._hostname = hostname;
    }

    /**
     * setLabel - Setter for the Label member.
     *
     * Sets the human readable label for this VPNLocation
     *
     * @param label Human readable label for this endpoing location.
     */
    public void setLabel(String label) {
        this._label = label;
    }

    /**
     * setFlag - Setter for the flag member.
     *
     * Sets the flag member of this VPNLocations. Flag member is the asset filename of the Android
     * drawable to use, sans the file extension.
     *
     * @param flagFile Flag asset filename, sans file type extension.
     */
    public void setFlag(String flagFile) {
        this._flag = flagFile;
    }

    /**
     * setHeaderImage - Setter for the HeaderImage member.
     *
     * Sets the HeaderImage of this VPNLocation. HeaderImage member is the asset filename of the
     * Android drawable to use, sans the file extension.
     *
     * @param headerImageFile HeaderImage asset filename, sans file type extension.
     */
    public void setHeaderImage(String headerImageFile) {
        this._headerImage = headerImageFile;
    }

    /**
     * getHostname - Getter for the Hostname member.
     *
     * Returns the Hostname member of this VPNLocation.
     *
     * @return Hostname of this endpoint location.
     */
    public String getHostname() {
        return this._hostname;
    }

    /**
     * getLabel - Getter for the Label member.
     *
     * Returns the Label member of this VPNLocation
     *
     * @return Human readable Label of this endpoint location.
     */
    public String getLabel() {
        return this._label;
    }
    public String getFlag() {
        return this._flag;
    }
    public String getHeaderImage() {
        return this._headerImage;
    }

    public static final Parcelable.Creator<VPNLocation> CREATOR
            = new Parcelable.Creator<VPNLocation>() {

        public VPNLocation createFromParcel(Parcel in) {
            return new VPNLocation(in);
        }
        public VPNLocation[] newArray(int size) {
            return new VPNLocation[size];
        }
    };

    private VPNLocation(Parcel in) {
        this._hostname    = in.readString();
        this._label       = in.readString();
        this._flag        = in.readString();
        this._headerImage = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this._hostname);
        out.writeString(this._label);
        out.writeString(this._flag);
        out.writeString(this._headerImage);
    }
}
