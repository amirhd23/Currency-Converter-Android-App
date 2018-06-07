package com.comp3617.currencyconverter;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.comp3617.currencyconverter.model.ExchangeRate;
import com.comp3617.currencyconverter.network.DataManager;
import com.comp3617.currencyconverter.network.DownloadService;

import java.util.List;

import io.realm.RealmResults;

/**
 * Created by Owner on 10/31/2017.
 */

public class CurrencyListFragment extends Fragment {
    private static final String TAG = "CurrencyListFragment";
    private RecyclerView mRecyclerView;
    private CurrencyAdapter mAdapter;

    private Callbacks mCallbacks;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DataManager.CONNECTION_ERROR)) {
                String message = intent.getStringExtra(DataManager.CONNECTION_ERROR_MESSAGE_KEY);
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                return;
            } else {
                updateUI();
            }
        }
    };

    /**
     * Required interface for hosting activities
     */
    public interface Callbacks {
        void onExchangeRateSelected(ExchangeRate exchangeRate);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter filters = new IntentFilter();
        filters.addAction(DownloadService.DOWNLOAD_COMPLETE);
        filters.addAction(DataManager.DATA_REFRESHED);
        filters.addAction(DataManager.CONNECTION_ERROR);
        lbm.registerReceiver(receiver, filters);
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
        lbm.unregisterReceiver(receiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.fragment_currency_list, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        //preventing search view becoming full screen in landscape mode
        int options = searchView.getImeOptions();
        searchView.setImeOptions(options | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_download_data:
                DataManager.getInstance().loadFeed(getActivity());
                break;
            case R.id.menu_delete_recorded_data:
                askConfirmationForDelete();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void askConfirmationForDelete() {
        new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.confirmation_delete_title))
                .setMessage(getResources().getString(R.string.confirmation_delete_text))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        DataManager.getInstance().deleteOldData(getActivity());
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }


    public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyViewHolder> {
        private List<ExchangeRate> mExchangeRates;

        public CurrencyAdapter(List<ExchangeRate> rates) {
            mExchangeRates = rates;
        }

        public void setExchangeRates(List<ExchangeRate> rates) {
            mExchangeRates = rates;
        }

        @Override
        public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.currency_list_item, parent, false);
            return new CurrencyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CurrencyViewHolder holder, int position) {
            ExchangeRate item = mExchangeRates.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return mExchangeRates.size();
        }

        public void filter(String text) {
            if (text == null || text.isEmpty()) {
                mExchangeRates = DataManager.getInstance().getLatestExchangeRates(getActivity());
            } else {
                text = text.toLowerCase();
                mExchangeRates = DataManager.getInstance().filterByText(text);
            }

            notifyDataSetChanged();
        }
    }

    public class CurrencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ExchangeRate mExchangeRate;
        private boolean flippedValues = false;
        private ImageView baseCurrencyImage;
        private TextView baseCurrencyTitle;
        private ImageButton flipBaseTargetCurrency;
        private ImageView targetCurrencyImage;
        private TextView targetCurrencyTitle;
        private TextView conversionValue;
        private TextView alternateConversionValue;
        private TextView updateDate;

        public CurrencyViewHolder(View itemView) {
            super(itemView);
            baseCurrencyImage = (ImageView) itemView.findViewById(R.id.base_currency_image);
            baseCurrencyTitle = (TextView) itemView.findViewById(R.id.base_currency_title);
            flipBaseTargetCurrency = (ImageButton) itemView.findViewById(R.id.flip_base_target_currency);
            flipBaseTargetCurrency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ValueAnimator va = ObjectAnimator.ofFloat(flipBaseTargetCurrency, "rotation", 0f, 180f);
                    va.setDuration(300);
                    va.start();
                    if (mExchangeRate != null && mExchangeRate.getValue() != 0) {
                        if (!flippedValues) {
                            conversionValue.setVisibility(View.GONE);
                            alternateConversionValue.setVisibility(View.VISIBLE);
                        } else {
                            conversionValue.setVisibility(View.VISIBLE);
                            alternateConversionValue.setVisibility(View.GONE);
                        }
                        flippedValues = !flippedValues;
                    }
                }
            });
            targetCurrencyImage = (ImageView) itemView.findViewById(R.id.target_currency_image);
            targetCurrencyTitle = (TextView) itemView.findViewById(R.id.target_currency_title);
            conversionValue = (TextView) itemView.findViewById(R.id.conversion_value);
            updateDate = (TextView) itemView.findViewById(R.id.update_date_text);
            alternateConversionValue = (TextView) itemView.findViewById(R.id.alternate_conversion_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onExchangeRateSelected(mExchangeRate);
        }

        public void bind(ExchangeRate exchangeRate) {
            mExchangeRate = exchangeRate;
            String baseCurrency = mExchangeRate.getBaseCurrency().toString().toLowerCase();
            String targetCurrency = mExchangeRate.getTargetCurrency().toString().toLowerCase();
            try {
                int baseCurrencyResId = getResources().getIdentifier(baseCurrency , "drawable", getActivity().getPackageName());
                int targetCurrencyResId = getResources().getIdentifier(targetCurrency , "drawable", getActivity().getPackageName());
                baseCurrencyImage.setImageResource(baseCurrencyResId);
                targetCurrencyImage.setImageResource(targetCurrencyResId);
            } catch (Exception ex) {
                Log.e(TAG, "Error finding currency image.");
            }
            baseCurrencyTitle.setText(baseCurrency);
            targetCurrencyTitle.setText(targetCurrency);
            conversionValue.setText(String.format("%.3f", mExchangeRate.getValue()));
            conversionValue.setVisibility(View.VISIBLE);
            alternateConversionValue.setText(String.format("%.3f", 1/mExchangeRate.getValue()));
            alternateConversionValue.setVisibility(View.GONE);
            updateDate.setText(String.format(getString(R.string.update_date), mExchangeRate.getLastUpdate()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.app_name));
        updateUI();
    }

    public void updateUI() {
        RealmResults<ExchangeRate> exRates = DataManager.getInstance().getLatestExchangeRates(getActivity());
        if (mAdapter == null) {
            mAdapter = new CurrencyAdapter(exRates);
        } else {
            mAdapter.setExchangeRates(exRates);
            mAdapter.notifyDataSetChanged();
        }
        mRecyclerView.setAdapter(mAdapter);
    }


}
