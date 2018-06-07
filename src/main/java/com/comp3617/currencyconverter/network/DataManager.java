package com.comp3617.currencyconverter.network;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.comp3617.currencyconverter.R;
import com.comp3617.currencyconverter.model.ExchangeRate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Owner on 11/2/2017.
 */

public class DataManager {

    public static final String DATA_REFRESHED = "DATA_REFRESHED";
    public static final String CONNECTION_ERROR = "CONNECTION_ERROR";
    public static final String CONNECTION_ERROR_MESSAGE_KEY = "connection_error_key";

    private static final String TAG = "DataManager";
    //only download USD (or preferred currency unit) rss feed. Other exchange rates are calculated.
    //for example, if USD/AAA = 3 and USD/BBB = 1.5, then BBB/AAA = 2
    private static final String DEFAULT_URL = "http://usd.fxexchangerate.com/rss.xml";
    private static final String URL_PART_1 = "http://";
    private static final String URL_PART_2 = ".fxexchangerate.com/rss.xml";

    //private static Realm sRealm;
    private static DataManager instance;

    private DataManager() {
    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }


    public void loadFeed(Context context) {
        boolean canConnect = checkInternetConnection(context);
        String url = getUrl(context);
        if (!canConnect) {return;}
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.URL_KEY, url);
        context.startService(intent);
    }

    public RealmResults<ExchangeRate> getExchangeRates() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ExchangeRate> results = realm.where(ExchangeRate.class).findAll();
        return results;
    }

    public RealmResults<ExchangeRate> getLatestExchangeRates(Context context) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ExchangeRate> result = realm.where(ExchangeRate.class)
                .equalTo("isLatestData", true)
                .findAll();
        if (result == null || result.size() == 0) {
            loadFeed(context);
        }
        return result;
    }

    public void deleteOldData(Context context) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(ExchangeRate.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        publishUpdateBroadcast(context);
    }

    public List<ExchangeRate> filterByText(String text) {
        Realm realm = Realm.getDefaultInstance();
        List<ExchangeRate> results = realm.where(ExchangeRate.class)
                .equalTo("isLatestData", true)
                .beginGroup()
                .contains("baseCurrency", text.toUpperCase())
                .or()
                .contains("targetCurrency", text.toUpperCase())
                .or()
                .contains("description", text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase())
                .endGroup()
                .findAll();
        return results;
    }

    public boolean checkInternetConnection(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean downloadOnlyOnWifi = sharedPref.getBoolean(context.getString(R.string.wifi_settings_key), true);
        boolean wifiConnected = false;
        boolean mobileConnected = false;
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }

        if (!wifiConnected && !mobileConnected) {
            publishErrorBroadcast(context, context.getString(R.string.no_internet_error));
            return false;
        }

        if (downloadOnlyOnWifi && !wifiConnected) {
            publishErrorBroadcast(context, context.getString(R.string.download_over_mobile_error));
            return false;
        }

        return true;
    }

    private String getUrl(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String preferredUrl = sharedPref.getString(context.getString(R.string.preferred_currency_key), null);
        String url;
        if (preferredUrl == null) {
            url = DEFAULT_URL;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(URL_PART_1)
                    .append(preferredUrl.toLowerCase())
                    .append(URL_PART_2);
            url = sb.toString();
        }
        return url;
    }

    private void publishUpdateBroadcast(Context context) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(DATA_REFRESHED);
        lbm.sendBroadcast(intent);
    }

    private void publishErrorBroadcast(Context context, String message) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(CONNECTION_ERROR);
        intent.putExtra(CONNECTION_ERROR_MESSAGE_KEY, message);
        lbm.sendBroadcast(intent);
    }

}
