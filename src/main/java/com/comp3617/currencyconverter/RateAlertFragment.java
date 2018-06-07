package com.comp3617.currencyconverter;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.comp3617.currencyconverter.model.Currency;
import com.comp3617.currencyconverter.model.RateAlert;
import com.comp3617.currencyconverter.utils.CurrencyConverter;
import com.comp3617.currencyconverter.utils.SpinnerUtils;

import io.realm.Realm;
import io.realm.RealmResults;


/**
 * A simple {@link Fragment} subclass.
 */
public class RateAlertFragment extends Fragment {

    private static final String TAG = "RateAlertFragment";

    private RateAlert rateAlert;
    private boolean isRateAlertNew;

    private Spinner fromSpinner;
    private Spinner toSpinner;
    private SpinnerUtils fromSpinnerUtils;
    private SpinnerUtils toSpinnerUtils;

    private CurrencyConverter currencyConverter;
    private TextView currentRateTextView;
    private EditText desiredRateEditText;
    private RadioGroup trendRadioGroup;
    private Button saveButton;
    private Button cancelButton;
    private boolean isInputsValid = false;

    private Callbacks callback;

    public interface Callbacks {
        void onCancelRateAlertRequested();
    }

    public RateAlertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        /*check if a rate alert is already defined
        * if so: load it to edit
        * else, create an empty one*/
        Realm realm = Realm.getDefaultInstance();
        RealmResults<RateAlert> result = realm.where(RateAlert.class).findAll();
        if (result == null || result.size() == 0) {
            isRateAlertNew = true;
            rateAlert = new RateAlert();
            rateAlert.setFromCurrency(Currency.USD);
            rateAlert.setToCurrency(Currency.CAD);
        } else {
            isRateAlertNew = false;
            rateAlert = result.first();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_rate_alert, container, false);
        findViews(v);
        init();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_rate_alert, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_alert:
                showDeleteConfirmationDialog();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void findViews(View v) {
        fromSpinner = (Spinner) v.findViewById(R.id.spinner_from_currency);
        toSpinner = (Spinner) v.findViewById(R.id.spinner_to_currency);
        currentRateTextView = (TextView) v.findViewById(R.id.tv_current_value);
        desiredRateEditText = (EditText) v.findViewById(R.id.et_desired_value);
        trendRadioGroup = (RadioGroup) v.findViewById(R.id.rg_trend);
        saveButton = (Button) v.findViewById(R.id.btn_save_alert);
        cancelButton = (Button) v.findViewById(R.id.btn_cancel_alert);
    }

    private void init() {
        currencyConverter = new CurrencyConverter();
        fromSpinnerUtils = new SpinnerUtils(fromSpinner);
        toSpinnerUtils = new SpinnerUtils(toSpinner);
        fromSpinnerUtils.addCurrenciesToSpinner(getActivity(), getResources());
        toSpinnerUtils.addCurrenciesToSpinner(getActivity(), getResources());
        fromSpinnerUtils.selectSpinnerItemByValue(rateAlert.getFromCurrency().toString());
        toSpinnerUtils.selectSpinnerItemByValue(rateAlert.getToCurrency().toString());
        setCurrentRate();
        if (!isRateAlertNew) {
            desiredRateEditText.setText(Float.toString(rateAlert.getDesiredValue()));
            int trendOption = rateAlert.getTrend();
            if (trendOption == RateAlert.FALLS_BELLOW) {
                trendRadioGroup.check(R.id.rdBtn_falls_bellow);
            } else if (trendOption == RateAlert.GOES_ABOVE) {
                trendRadioGroup.check(R.id.rdBtn_goes_above);
            }
        }
        attachListeners();
    }

    private void attachListeners() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.onCancelRateAlertRequested();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateInputs();
                /*if (!isInputsValid && rateAlert.isValid() && rateAlert.isManaged()) {
                    rateAlert.delete();
                } else {
                    //save the data
                    Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(rateAlert);
                        }
                    }
                    );
                }*/
                if (isInputsValid && rateAlert.isValid()) {
                    //save the data
                    Realm realm = Realm.getDefaultInstance();
                    realm.executeTransactionAsync(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            realm.insertOrUpdate(rateAlert);
                        }
                    });
                    Toast.makeText(getActivity(), getString(R.string.rate_alert_success_save), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.rate_error_message), Toast.LENGTH_SHORT).show();
                }
            }
        });
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                rateAlert.setFromCurrency(Currency.valueOf(fromSpinner.getSelectedItem().toString()));
                setCurrentRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                rateAlert.setToCurrency(Currency.valueOf(toSpinner.getSelectedItem().toString()));
                setCurrentRate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.set_up_rate_alert));
    }

    private void setCurrentRate() {
        try {
            Float currentRate = currencyConverter.getExchangeRate(rateAlert.getFromCurrency(), rateAlert.getToCurrency());
            currentRateTextView.setText(Float.toString(currentRate));
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void showDeleteConfirmationDialog() {
        //check if there is a rate rate alert
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.confirmation_delete_title))
                .setMessage(getString(R.string.confirmation_delete_text))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (rateAlert.isManaged() && rateAlert.isValid()) {
                            rateAlert.delete();
                        }
                        callback.onCancelRateAlertRequested();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void validateInputs() {
        try {
            float desiredRate = Float.parseFloat(desiredRateEditText.getText().toString());
            if (desiredRate <= 0) {
                Toast.makeText(getActivity(), getString(R.string.rate_desired_error_message), Toast.LENGTH_LONG).show();
                isInputsValid = false;
                return;
            }
            rateAlert.setFromCurrency(Currency.valueOf(fromSpinner.getSelectedItem().toString()));
            rateAlert.setToCurrency(Currency.valueOf(toSpinner.getSelectedItem().toString()));
            rateAlert.setDesiredValue(desiredRate);
            int checkedButtonId = trendRadioGroup.getCheckedRadioButtonId();
            if (checkedButtonId == R.id.rdBtn_falls_bellow) {
                rateAlert.setTrend(RateAlert.FALLS_BELLOW);
            } else if (checkedButtonId == R.id.rdBtn_goes_above) {
                rateAlert.setTrend(RateAlert.GOES_ABOVE);
            }
            isInputsValid = true;
        } catch (Exception ex) {
            isInputsValid = false;
        }
        if (!isInputsValid) {
            Toast.makeText(getActivity(), getString(R.string.rate_error_message), Toast.LENGTH_LONG).show();
            return;
        }
        callback.onCancelRateAlertRequested();
    }

}
