package com.comp3617.currencyconverter.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by amird on 2017-11-16.
 */

public class RateAlert extends RealmObject {
    public static final int FALLS_BELLOW = 0;
    public static final int GOES_ABOVE = 1;

    private String fromCurrency;
    private String toCurrency;
    private float currentValue;
    private float desiredValue;
    private int trend = FALLS_BELLOW;//FALLS_BELLOW or GOES_ABOVE

    public static RealmResults<RateAlert> getRateAlerts() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RateAlert> result = realm
                .where(RateAlert.class)
                .findAll();

        return result;
    }

    public void delete() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        deleteFromRealm();
        realm.commitTransaction();

    }

    public RateAlert() {

    }

    public Currency getFromCurrency() {
        return Currency.valueOf(fromCurrency);
    }

    public void setFromCurrency(Currency fromCurrency) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        this.fromCurrency = fromCurrency.toString();
        realm.commitTransaction();
    }

    public Currency getToCurrency() {
        return Currency.valueOf(toCurrency);
    }

    public void setToCurrency(Currency toCurrency) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        this.toCurrency = toCurrency.toString();
        realm.commitTransaction();

    }

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        this.currentValue = currentValue;
        realm.commitTransaction();
    }

    public float getDesiredValue() {
        return desiredValue;
    }

    public void setDesiredValue(float desiredValue) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        this.desiredValue = desiredValue;
        realm.commitTransaction();
    }

    public int getTrend() {
        return trend;
    }

    public void setTrend(int trend) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        this.trend = trend;
        realm.commitTransaction();
    }
}
