package com.comp3617.currencyconverter.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Owner on 10/26/2017.
 */

public class ExchangeRate extends RealmObject implements Parcelable {
    private String description;
    private Date lastUpdate;
    /**
     * value * baseCurrency = 1 * targetCurrency
     */
    private float value;
    private String baseCurrency;
    private String targetCurrency;
    private boolean isLatestData;

    public ExchangeRate() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public Currency getBaseCurrency() {
        return Currency.valueOf(baseCurrency);
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency.toString();
    }

    public Currency getTargetCurrency() {
        return Currency.valueOf(targetCurrency);
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency.toString();
    }

    public boolean isLatestData() {
        return isLatestData;
    }

    public void setLatestData(boolean latestData) {
        isLatestData = latestData;
    }

    protected ExchangeRate(Parcel in) {
        description = in.readString();
        long tmpLastUpdate = in.readLong();
        lastUpdate = tmpLastUpdate != -1 ? new Date(tmpLastUpdate) : null;
        value = in.readFloat();
        baseCurrency = in.readString();
        targetCurrency = in.readString();
        isLatestData = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeLong(lastUpdate != null ? lastUpdate.getTime() : -1L);
        dest.writeFloat(value);
        dest.writeString(baseCurrency);
        dest.writeString(targetCurrency);
        dest.writeByte((byte) (isLatestData ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Creator<ExchangeRate> CREATOR = new Creator<ExchangeRate>() {
        @Override
        public ExchangeRate createFromParcel(Parcel in) {
            return new ExchangeRate(in);
        }

        @Override
        public ExchangeRate[] newArray(int size) {
            return new ExchangeRate[size];
        }
    };
}
