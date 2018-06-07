package com.comp3617.currencyconverter.network;

import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.Xml;

import com.comp3617.currencyconverter.model.Currency;
import com.comp3617.currencyconverter.model.ExchangeRate;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Owner on 10/26/2017.
 */

public class XmlParser {
    private static final String ns = null;
    private static final String TAG = "xml parser";
    private static final String EMPTY_STRING = "";
    private static final String ITEM_TAG = "item";
    private static final String DATE_TAG = "pubDate";
    private static final String TITLE_TAG = "title";
    private static final String DESCRIPTION_TAG = "description";
    private static final String CHANNEL_TAG = "channel";

    private static final String DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss z";

    public ExchangeRate[] parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    private ExchangeRate[] readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        int size = Currency.values().length - 1;//because we dont want USD to USD
        int index = 0;
        ExchangeRate[] entries = new ExchangeRate[size];
        int eventType = parser.getEventType();
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            String tagName = parser.getName();
            if (tagName == null) {
                eventType = parser.next();
                continue;
            }
            switch (eventType) {
                case XmlResourceParser.START_TAG:
                    if (tagName.equals(CHANNEL_TAG)) {
                        int eventType2 = parser.next();
                        while (eventType2 != XmlResourceParser.END_DOCUMENT) {
                            String tagName2 = parser.getName();
                            if (tagName2 == null) {
                                eventType2 = parser.next();
                                continue;
                            }
                            switch (eventType2) {
                                case XmlResourceParser.START_TAG:
                                    if (tagName2.equals(ITEM_TAG)) {
                                        ExchangeRate item = readEntry(parser);
                                        if (item != null) {
                                            entries[index++] = item;
                                        }
                                    }
                                    break;
                                case XmlResourceParser.TEXT:
                                    break;
                                case XmlPullParser.END_TAG:
                                    break;
                            }
                            eventType2 = parser.next();
                        }
                    }
                    break;
                case XmlResourceParser.TEXT:
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = parser.next();
        }
        return entries;
    }

    private ExchangeRate readEntry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, ITEM_TAG);
        ExchangeRate exchangeRate = new ExchangeRate();
        String description = null;
        Currency baseCurrency = null;
        Currency targetCurrency = null;
        Matcher matcher;
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        float value = 1;
        Date lastUpdate = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name == null) {
                continue;
            }
            if (name.equals(DESCRIPTION_TAG)) {
                description = readField(parser, DESCRIPTION_TAG);
                value = getExchangeRateValue(description);
            } else if (name.equals(DATE_TAG)) {
                try {
                    lastUpdate = dateFormat.parse(readField(parser, DATE_TAG));
                } catch (ParseException e) {
                    Log.e(TAG, e.getMessage());
                }
            } else if (name.equals(TITLE_TAG)) {
                String baseTargetCurrency = readField(parser, TITLE_TAG);
                //Extracting currency codes such as (IRR) and (AUD)
                matcher = pattern.matcher(baseTargetCurrency);
                boolean firstOccurrence = true;
                try {
                    while(matcher.find()) {
                        if (firstOccurrence) {
                            targetCurrency = Currency.valueOf(matcher.group(1));
                            firstOccurrence = false;
                        } else {
                            baseCurrency = Currency.valueOf(matcher.group(1));
                        }
                    }
                } catch (Exception ex) {
                    //Log.e(TAG, "non-existing currency codes from :" + baseTargetCurrency);
                }

                if (baseCurrency == null || targetCurrency == null) {
                    return null;
                }
            } else {
                skip(parser);
            }
        }
        if (baseCurrency.equals(targetCurrency)) {
            return null;
        }
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setDescription(description);
        exchangeRate.setValue(value);
        exchangeRate.setLastUpdate(lastUpdate);
        exchangeRate.setLatestData(true);
        return exchangeRate;
    }

    private String readField(XmlPullParser parser, String fieldName) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, fieldName);
        String value = readText(parser);
        return value;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = EMPTY_STRING;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    /**
     * @param input should be in format "1 Iran Rial = 4.0E-5 Australian Dollar"
     * @return 4.0E-5 in above example
     */
    private float getExchangeRateValue(String input) {
        float value = 1;
        String[] words = input.split("\\s+");
        int index = Arrays.asList(words).indexOf("=");
        try {
            value = Float.parseFloat(words[index + 1]);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return value;
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
