package com.comp3617.currencyconverter.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.comp3617.currencyconverter.model.ExchangeRate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;


public class DownloadService extends IntentService {

    public static final String DOWNLOAD_COMPLETE = "DOWNLOAD_COMPLETE";
    public static final String URL_KEY = "url";
    private static final String TAG = "DownloadService";

    public DownloadService() {
        super("DownloadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //do the download
            String url = intent.getStringExtra(URL_KEY);
            Exception exception = null;
            if (url == null) {return;}
            final List<ExchangeRate> items = new ArrayList<>();
            try {
                Collections.addAll(items, new CurrencyDownloader().downloadData(url));

            } catch (IOException e) {
                Log.e(TAG, "Connection Error");
                exception = e;
            } catch (XmlPullParserException e) {
                Log.e(TAG, "XML parse Error");
                exception = e;
            }
            if (exception == null) {
                //mark the previous data as old data
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                RealmResults<ExchangeRate> oldRates = realm
                        .where(ExchangeRate.class)
                        .findAll();
                for (ExchangeRate exr : oldRates) {
                    exr.setLatestData(false);
                }

                realm.copyToRealm(items);
                realm.commitTransaction();
                publishUpdateBroadcast();
            }
        }
    }

    private void publishUpdateBroadcast() {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        Intent broadCastIntent = new Intent(DOWNLOAD_COMPLETE);
        lbm.sendBroadcast(broadCastIntent);
    }


}
