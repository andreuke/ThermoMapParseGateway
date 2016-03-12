package com.blukii.android.blukii_android_widgetdemo.model;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Data object for blukii information
 */
public class InputElement implements Parcelable, Comparable<InputElement> {

    private String TagID;
    private short rssi;
    private long deviceFoundDate;

    private Boolean isConnected;

    private String name;


    private int airPressure = Integer.MIN_VALUE;
    private int light = Integer.MIN_VALUE;
    private int humidity = Integer.MIN_VALUE;
    private float temperature = Float.MIN_VALUE;


    public InputElement(String tagID) {
        TagID = tagID;
    }

    // Copy Constructor
    public InputElement(InputElement other) {
        copy(other);
    }

    public void copy(InputElement other){
        TagID = other.getTagID();
        rssi = other.getRssi();
        deviceFoundDate = other.getDeviceFoundDate();
        isConnected = other.getIsConnected();
        name = other.getName();
        airPressure = other.getAirPressure();
        light = other.getLight();
        humidity = other.getHumidity();
        temperature = other.getTemperature();
    }

    public String getTagID() {

        return TagID;
    }

    public void setTagID(String tagID) {
        TagID = tagID;
    }

    public short getRssi() {
        return rssi;
    }

    public void setRssi(short rssi) {
        this.rssi = rssi;
    }

    public long getDeviceFoundDate() {
        return deviceFoundDate;
    }

    public void setDeviceFoundDate(long deviceFoundDate) {
        this.deviceFoundDate = deviceFoundDate;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    // Sensor data

    public int getAirPressure() {
        return airPressure;
    }
    public void setAirPressure(int airPressure) {
        this.airPressure = airPressure;
    }

    public int getLight() {
        return light;
    }
    public void setLight(int light) {
        this.light = light;
    }

    public int getHumidity() {
        return humidity;
    }
    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public float getTemperature() {
        return temperature;
    }
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }



    // Interface Comparable

    @Override
    public int compareTo(InputElement inputElement) {
        return Long.compare(getDeviceFoundDate(), inputElement.getDeviceFoundDate());
    }

    // Interface Parcelable

    protected InputElement(Parcel in) {
        TagID = in.readString();
        rssi = (short) in.readValue(short.class.getClassLoader());
        deviceFoundDate = in.readLong();
        byte isConnectedVal = in.readByte();
        isConnected = isConnectedVal == 0x02 ? null : isConnectedVal != 0x00;
        name = in.readString();

        airPressure = in.readInt();
        light = in.readInt();
        humidity = in.readInt();
        temperature = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(TagID);
        dest.writeValue(rssi);
        dest.writeLong(deviceFoundDate);
        if (isConnected == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (isConnected ? 0x01 : 0x00));
        }
        dest.writeString(name);

        dest.writeInt(airPressure);
        dest.writeInt(light);
        dest.writeInt(humidity);
        dest.writeFloat(temperature);
    }

    @SuppressWarnings("unused")
    public static final Creator<InputElement> CREATOR = new Creator<InputElement>() {
        @Override
        public InputElement createFromParcel(Parcel in) {
            return new InputElement(in);
        }

        @Override
        public InputElement[] newArray(int size) {
            return new InputElement[size];
        }
    };
}