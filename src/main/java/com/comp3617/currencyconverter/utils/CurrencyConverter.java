package com.comp3617.currencyconverter.utils;


import com.comp3617.currencyconverter.model.Currency;
import com.comp3617.currencyconverter.model.ExchangeRate;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Owner on 10/31/2017.
 */

public class CurrencyConverter {

    RealmResults<ExchangeRate> exchangeRates;

    public CurrencyConverter() {

    }

    /**
     * return exchange rate from Currency A to Currency B (USD/A / USD/B = B/A)
     * @param A
     * @param B
     * @return value such that 1A = value * B
     */
    public float getExchangeRate(Currency A, Currency B) {
        if (A.equals(B)) {
            return 1;
        }
        Realm realm = Realm.getDefaultInstance();
        this.exchangeRates = realm.where(ExchangeRate.class)
                .equalTo("isLatestData", true)
                .findAll();

        float usdToA = 0;
        float usdToB = 0;
        boolean aFound = false;
        boolean bFound = false;
        Currency baseCurrency;
        for (ExchangeRate eRate : exchangeRates) {
            baseCurrency = eRate.getBaseCurrency();
            if (baseCurrency.equals(A)) {
                usdToA = eRate.getValue();
                aFound = true;
            } else if (baseCurrency.equals(B)) {
                usdToB = eRate.getValue();
                bFound = true;
            }
            if (aFound && bFound) {
                break;
            }
        }
        if (!aFound) {
            //a is the USD
            usdToA = 1;
        }
        if (!bFound) {
            //b is the usd
            usdToB = 1;
        }
        return  usdToB / usdToA;
    }
}
