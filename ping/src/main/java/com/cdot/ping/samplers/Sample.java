/*
 * Copyright © 2020 C-Dot Consultants
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.cdot.ping.samplers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public class Sample implements Parcelable {
    public static final String TAG = Sample.class.getSimpleName();

    public static final Creator<Sample> CREATOR = new Creator<Sample>() {
        @Override
        public Sample createFromParcel(Parcel in) {
            return new Sample(in);
        }

        @Override
        public Sample[] newArray(int size) {
            return new Sample[size];
        }
    };

    // Number of bytes in a serialised sample
    public static final int BYTES = Long.BYTES // time
            + 2 * Double.BYTES // lat, long
            + Float.BYTES // depth
            + 1; // strength

    public long time;
    public double latitude;
    public double longitude;
    public float depth;
    public int strength; // Range 0..255

    // Remaining fileds are not serialised
    public float temperature;
    public float fishDepth;
    public int fishStrength; // Range 0..15
    public byte battery; // Range 0..6

    public Sample() {
    }

    /**
     * Construct a new sample
     * @param lat latitude
     * @param lon longitude
     * @param d depth
     * @param s strength
     */
    public Sample(double lat, double lon, float d, int s) {
        time = System.currentTimeMillis();
        depth = d;
        latitude = lat;
        longitude = lon;
        strength = s;
    }

    /**
     * Create from a serialised data stream
     */
    Sample(DataInputStream dis) throws IOException {
        time = dis.readLong();
        latitude = dis.readDouble();
        longitude = dis.readDouble();
        depth = dis.readFloat();
        strength = dis.readUnsignedByte();
    }

    // Serialise
    byte[] getBytes() {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bs);
        try {
            dos.writeLong(time);
            dos.writeDouble(latitude);
            dos.writeDouble(longitude);
            dos.writeFloat(depth);
            dos.writeByte(strength);
        } catch (IOException ioe) {
            Log.e(TAG, "getBytes error " + ioe);
        }
        return bs.toByteArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Parcels are used to send samples internally
    protected Sample(Parcel in) {
        time = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        depth = in.readFloat();
        fishDepth = in.readFloat();
        strength = (short) in.readInt();
        fishStrength = (short) in.readInt();
        temperature = in.readFloat();
        battery = in.readByte();
    }

    @Override // implement Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(time);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeFloat(depth);
        parcel.writeFloat(fishDepth);
        parcel.writeInt(strength);
        parcel.writeInt(fishStrength);
        parcel.writeFloat(temperature);
        parcel.writeByte(battery);
    }

    public Element toGPX(Document doc) {
        Element GPX_trkpt = doc.createElementNS(GPX.NS_GPX, "trkpt");
        GPX_trkpt.setAttribute("lat", Double.toString(latitude));
        GPX_trkpt.setAttribute("lon", Double.toString(longitude));

        Element GPX_ele = doc.createElementNS(GPX.NS_GPX, "ele");
        GPX_ele.setTextContent(Double.toString(depth < 0 ? 0 : depth));
        GPX_trkpt.appendChild(GPX_ele);

        Element GPX_time = doc.createElementNS(GPX.NS_GPX, "time");
        Date date = new Date(time);
        GPX.ISO_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("Z"));
        String text = GPX.ISO_DATE_FORMAT.format(date);
        GPX_time.setTextContent(text);
        GPX_trkpt.appendChild(GPX_time);

        Element GPX_extensions = doc.createElementNS(GPX.NS_GPX, "extensions");
        Element GPX_ping = doc.createElementNS(GPX.NS_PING, "ping");
        if (strength > 0)
            GPX_ping.setAttribute("strength", Integer.toString(strength));
        if (fishDepth > 0)
            GPX_ping.setAttribute("fdepth", Double.toString(fishDepth));
        if (fishStrength > 0)
            GPX_ping.setAttribute("fstrength", Integer.toString(fishStrength));
        /*if (location.getAccuracy() > 0)
            GPX_ping.setAttribute("hacc", Float.toString(location.getAccuracy()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (location.getVerticalAccuracyMeters() > 0)
                GPX_ping.setAttribute("vacc", Float.toString(location.getVerticalAccuracyMeters()));
        }*/
        GPX_extensions.appendChild(GPX_ping);
        GPX_trkpt.appendChild(GPX_extensions);
        return GPX_trkpt;
    }
}