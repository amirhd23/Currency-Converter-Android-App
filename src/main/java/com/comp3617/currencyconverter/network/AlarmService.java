package com.comp3617.currencyconverter.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.comp3617.currencyconverter.MainActivity;
import com.comp3617.currencyconverter.R;
import com.comp3617.currencyconverter.model.Currency;
import com.comp3617.currencyconverter.model.RateAlert;
import com.comp3617.currencyconverter.utils.CurrencyConverter;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by amird on 2017-11-14.
 */

public class AlarmService extends IntentService {
    private static final String TAG = "AlarmService";

    public AlarmService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //do the task here
        Log.i(TAG, "Service Running");
        DataManager.getInstance().loadFeed(this);
        checkForRateAlert();
    }

    /**
     * checks if a rate alert is set by user, and publishes notification
     */
    private void checkForRateAlert() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RateAlert> results = realm.where(RateAlert.class).findAll();
        if (results == null || results.size() == 0) {
            return;
        }
        RateAlert alert = results.first();
        if (alert == null) {
            return;
        }
        Currency fromCurrency = alert.getFromCurrency();
        Currency toCurency = alert.getToCurrency();
        CurrencyConverter converter = new CurrencyConverter();
        float currentValue = converter.getExchangeRate(fromCurrency, toCurency);
        float desiredValue = alert.getDesiredValue();
        int requestedTrend = alert.getTrend();
        String message;
        if (requestedTrend == RateAlert.FALLS_BELLOW && currentValue < desiredValue) {
            message = getString(R.string.rate_alert_notification_text,
                    fromCurrency.toString(),
                    toCurency.toString(),
                    currentValue,
                    getString(R.string.below),
                    desiredValue);
            publishNotification(message);
        } else if (requestedTrend == RateAlert.GOES_ABOVE && currentValue >= desiredValue) {
            message = getString(R.string.rate_alert_notification_text,
                    fromCurrency.toString(),
                    toCurency.toString(),
                    currentValue,
                    getString(R.string.above),
                    desiredValue);
            publishNotification(message);
        }
    }

    private void publishNotification(String message) {
        // The id of the channel.
        String CHANNEL_ID = "my_channel_01";
        NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.currency_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message);
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
        stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(1, mBuilder.build());
    }
}
