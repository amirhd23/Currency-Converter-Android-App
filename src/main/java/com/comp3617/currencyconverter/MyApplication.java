package com.comp3617.currencyconverter;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by Owner on 11/2/2017.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
