package com.comp3617.currencyconverter;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.comp3617.currencyconverter.model.Currency;
import com.comp3617.currencyconverter.model.ExchangeRate;
import com.comp3617.currencyconverter.network.DataManager;
import com.comp3617.currencyconverter.utils.CurrencyConverter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConversionFragment extends Fragment {

    public static final String EXCHANGE_RATE_KEY = "exchange rate key";
    private static final String TAG = "ConversionFragment";

    private ExchangeRate exchangeRate;
    private Spinner fromSpinner;
    private EditText fromEditText;
    private Spinner toSpinner;
    private EditText toEditText;
    private WebView chartWebView;
    private String htmlErrorMessage;
    private CurrencyConverter currencyConverter;

    private Button shareRateButton;

    private float currentExchangeValue;//can change based on changing spinners by user
    private boolean isEditHandled = false;//to prevent endless loop


    public static ConversionFragment newInstance(ExchangeRate exchangeRate) {
        Bundle args = new Bundle();
        args.putParcelable(EXCHANGE_RATE_KEY, exchangeRate);
        ConversionFragment fragment = new ConversionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ConversionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exchangeRate = getArguments().getParcelable(EXCHANGE_RATE_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_conversion, container, false);
        findViews(v);
        addCurrencyToSpinners();
        currencyConverter = new CurrencyConverter();
        init();
        return v;
    }

    private void findViews(View v) {
        fromEditText = (EditText) v.findViewById(R.id.from_input);
        fromSpinner = (Spinner) v.findViewById(R.id.from_currency_spinner);
        toEditText = (EditText) v.findViewById(R.id.to_input);
        toSpinner = (Spinner) v.findViewById(R.id.to_currency_spinner);
        chartWebView = (WebView) v.findViewById(R.id.chart_container);
        shareRateButton = (Button) v.findViewById(R.id.share_rate_button);
    }

    private void init() {
        selectSpinnerItemByValue(fromSpinner, exchangeRate.getTargetCurrency().toString());
        selectSpinnerItemByValue(toSpinner, exchangeRate.getBaseCurrency().toString());
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                resetEditTexts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                resetEditTexts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        fromEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isEditHandled) {
                    isEditHandled = true;
                    try {
                        float value = Float.parseFloat(fromEditText.getText().toString()) * currentExchangeValue;
                        toEditText.setText(Float.toString(value));
                    } catch (Exception ex) {
                        //resetEditTexts();
                    }
                    isEditHandled = false;
                }
            }
        });

        int options = fromEditText.getImeOptions();
        fromEditText.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI);


        toEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!isEditHandled) {
                    isEditHandled = true;
                    try {
                        float value = Float.parseFloat(toEditText.getText().toString()) / currentExchangeValue;
                        fromEditText.setText(Float.toString(value));

                    } catch (Exception ex) {
                        //resetEditTexts();
                    }
                    isEditHandled = false;
                }
            }
        });

        shareRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                Currency fromCurrency = Currency.valueOf(fromSpinner.getSelectedItem().toString());
                Currency toCurrency = Currency.valueOf(toSpinner.getSelectedItem().toString());
                currentExchangeValue = currencyConverter.getExchangeRate(fromCurrency, toCurrency);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_rate_text,
                        fromCurrency.toString(),
                        toCurrency.toString(),
                        currentExchangeValue));
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_rate_subject));
                shareIntent.setType("text/plain");
                shareIntent.createChooser(shareIntent, getString(R.string.send_to));
                startActivity(shareIntent);
            }
        });

        options = toEditText.getImeOptions();
        toEditText.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        chartWebView.getSettings().setJavaScriptEnabled(true);
        chartWebView.addJavascriptInterface(new JavascriptUtils(getActivity()), "AndroidUtil");
        updateChart();

        resetEditTexts();
    }

    private class CustomSpinnerAdapter extends ArrayAdapter {
        private String[] mObjects;

        public CustomSpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
            mObjects = objects;
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_currency_spinner, parent, false);
            TextView currencyName = (TextView) layout.findViewById(R.id.currency_title);
            String name = mObjects[position];
            currencyName.setText(name);
            ImageView currencyImage = (ImageView) layout.findViewById(R.id.currency_thumbnail);
            try {
                int imageResId = getResources().getIdentifier(name.toLowerCase(), "drawable", getActivity().getPackageName());
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

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.app_name));
    }

    private class JavascriptUtils {
        private Context context;

        public JavascriptUtils(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public String getBaseCurrency() {
            String fromCurrency = fromSpinner.getSelectedItem().toString();
            return fromCurrency;
        }

        @JavascriptInterface
        public String getTargetCurrency() {
            String targetCurrency = toSpinner.getSelectedItem().toString();
            return targetCurrency;
        }

        @JavascriptInterface
        public String getErrorMessage() {
            return htmlErrorMessage;
        }
    }

    private void addCurrencyToSpinners() {
        Currency[] currencies = Currency.values();
        String[] currencyNames = new String[currencies.length];
        int index = 0;
        for (Currency c : currencies) {
            currencyNames[index++] = c.toString();
        }
        fromSpinner.setAdapter(new CustomSpinnerAdapter(getActivity(), R.layout.custom_currency_spinner, currencyNames));
        toSpinner.setAdapter(new CustomSpinnerAdapter(getActivity(), R.layout.custom_currency_spinner, currencyNames));
    }

    private void selectSpinnerItemByValue(Spinner spinner, String value) {
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

    private void resetEditTexts() {
        fromEditText.setText(getString(R.string.default_conversion_input));
        Currency fromCurrency = Currency.valueOf(fromSpinner.getSelectedItem().toString());
        Currency toCurrency = Currency.valueOf(toSpinner.getSelectedItem().toString());
        currentExchangeValue = currencyConverter.getExchangeRate(fromCurrency, toCurrency);
        toEditText.setText(Float.toString(currentExchangeValue));
        updateChart();
    }

    private void updateChart() {
        if (fromSpinner.getSelectedItem().toString()
                .equals(toSpinner.getSelectedItem().toString())) {
            htmlErrorMessage = getString(R.string.chart_error_same_currency);
            chartWebView.loadUrl("file:///android_asset/error_page.html");
        } else if (!DataManager.getInstance().checkInternetConnection(getActivity())) {
            htmlErrorMessage = getString(R.string.chart_no_internet_error);
            chartWebView.loadUrl("file:///android_asset/error_page.html");
        } else {
            try {
                chartWebView.loadUrl("file:///android_asset/chart_page.html");
            } catch (Exception ex) {
                htmlErrorMessage = ex.getMessage();
                chartWebView.loadUrl("file:///android_asset/error_page.html");
            }
        }

    }

}
