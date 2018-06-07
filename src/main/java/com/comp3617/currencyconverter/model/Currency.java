package com.comp3617.currencyconverter.model;

/**
 * Created by Owner on 10/26/2017.
 */

public enum Currency {
    AUD("Australian Dollar"),
    ALL("Albanian Lek"),
    ARS("Argentine Peso"),
    CAD("Canadian Dollar"),
    DZD("Algerian Dinar"),
    GBP("British Pound"),
    IRR("Iran Rial"),
    USD("United States Dollar");


    private final String title;

    Currency(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }




}
