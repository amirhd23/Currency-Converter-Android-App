package com.comp3617.currencyconverter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp3617.currencyconverter.network.AlarmReceiver;


/**
 * Created by Owner on 11/3/2017.
 */

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String FREQ_NONE = "none";
    private static final String FREQ_FIFTEEN_MINUTES = "fifteen_minutes";
    private static final String FREQ_THIRTY_MINUTES = "thirty_minutes";
    private static final String FREQ_ONE_HOUR = "one_hour";
    private static final String FREQ_ONE_DAY = "one_day";


    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_settings);
        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        getActivity().setTitle(R.string.settings_title);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.sync_frequency_settings_key))) {
            String updateFreq = sharedPreferences.getString(getString(R.string.sync_frequency_settings_key), null);
            switch (updateFreq) {
                case FREQ_NONE://cancel alarm
                    cancelAlarm();
                    break;
                case FREQ_FIFTEEN_MINUTES:
                    scheduleAlarm(AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                    break;
                case FREQ_THIRTY_MINUTES:
                    scheduleAlarm(AlarmManager.INTERVAL_HALF_HOUR);
                    break;
                case FREQ_ONE_HOUR:
                    scheduleAlarm(AlarmManager.INTERVAL_HOUR);
                    break;
                case FREQ_ONE_DAY:
                    scheduleAlarm(AlarmManager.INTERVAL_DAY);
                    break;
            }
        }
    }

    private void scheduleAlarm(long intervalInMillis) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                intervalInMillis, pIntent);

    }

    public void cancelAlarm() {
        Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(getActivity(), AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

}
