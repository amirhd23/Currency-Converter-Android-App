package com.comp3617.currencyconverter.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.comp3617.currencyconverter.R;
import com.comp3617.currencyconverter.model.Currency;

/**
 * Created by amird on 2017-11-17.
 */

public class SpinnerUtils {

    private static final String TAG = "SpinnerUtils";

    private Spinner spinner;

    public SpinnerUtils(Spinner spinner) {
        this.spinner = spinner;
    }


    public void addCurrenciesToSpinner(Context context, Resources resources) {
        Currency[] currencies = Currency.values();
        String[] currencyNames = new String[currencies.length];
        int index = 0;
        for (Currency c : currencies) {
            currencyNames[index++] = c.toString();
        }
        spinner.setAdapter(new CustomSpinnerAdapter(context, resources, R.layout.custom_currency_spinner, currencyNames));
    }

    public void selectSpinnerItemByValue(String value) {
        if (value == null) {
            return;
        }
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (spinner.getItemAtPosition(position).equals(value)) {
                spinner.setSelection(position);
                return;
            }
        }
    }

    private class CustomSpinnerAdapter extends ArrayAdapter {
        private String[] mObjects;
        private Resources resources;
        private Context context;

        public CustomSpinnerAdapter(Context context, Resources resources, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
            mObjects = objects;
            this.resources = resources;
            this.context = context;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_currency_spinner, parent, false);
            TextView currencyName = (TextView) layout.findViewById(R.id.currency_title);
            String name = mObjects[position];
            currencyName.setText(name);
            ImageView currencyImage = (ImageView) layout.findViewById(R.id.currency_thumbnail);
            try {
                int imageResId = resources.getIdentifier(name.toLowerCase(), "drawable", ((Activity)context).getPackageName());
                currencyImage.setImageResource(imageResId);
            } catch (Exception e) {
                Log.e(TAG, "Error setting currency image for " + name);
            }

            return layout;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

    }
}
