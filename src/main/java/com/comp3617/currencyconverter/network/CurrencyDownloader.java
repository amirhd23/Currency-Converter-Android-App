package com.comp3617.currencyconverter.network;

import com.comp3617.currencyconverter.model.ExchangeRate;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Owner on 10/26/2017.
 */

public class CurrencyDownloader {
    public ExchangeRate[] downloadData(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        XmlParser xmlParser = new XmlParser();
        ExchangeRate[] items = null;

        try {
            stream = downloadUrl(urlString);
            items = xmlParser.parse(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return items;
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);/* milliseconds */
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }
}
