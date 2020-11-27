package com.example.newsgateway;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class NewsSourceDownloader implements Runnable {

    private static final String TAG = "NewsSourceDownloader";
    private MainActivity mainActivity;
    private String category;
    private String DATA_URL;
    private static final String API_KEY = "bf9a91912be344539b68c21f2d934609";

    // data structures for storing desired api data
    private ArrayList<NewsSource> sources = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>();

    NewsSourceDownloader(MainActivity mainActivity, String category) {
        this.mainActivity = mainActivity;
        this.category = category;
    }

    @Override
    public void run() {

        // api takes empty string as input for all categories
        if (category == "all")
            category = "";

        // setting url for api call
        DATA_URL = "https://newsapi.org/v2/sources?language=en&country=us&category=" +
                category +
                "&apiKey=" +
                API_KEY;

        Uri dataUri = Uri.parse(DATA_URL);
        String usageURL = dataUri.toString();
        StringBuilder sb = new StringBuilder();

        try {
            // url connection
            URL url = new URL(usageURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent", ""); // required by api
            conn.connect();

            // checking connection
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP Response NOT OK - " + conn.getResponseCode());
                dataHandler(null);
                return;
            }

            // read raw input
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

        } catch (Exception e) {
            Log.d(TAG, "run: Exception - ", e);
            dataHandler(null);
            return;
        }

        // pass the parsed input to dataHandler for further processing
        dataHandler(sb.toString());

    }

    private void dataHandler(String s) {
        if (s == null) {
            Log.d(TAG, "dataHandler: Failure in downloading data.");
            return;
        }

        // obtain list of sources from parseJSON
        sources = parseJSON(s);

        // generate list of unique news categories from the parsed sources
        for (NewsSource sc : sources) {
            // only add category if it hasn't been added yet
            if (!(categories.contains(sc.getCategory()))) {
                categories.add(sc.getCategory());
            }
        }

        mainActivity.runOnUiThread(() -> mainActivity.setSources(sources, categories));
    }

    private ArrayList<NewsSource> parseJSON(String s) {

        ArrayList<NewsSource> parsedSources = new ArrayList<>();

        try {

            JSONObject jsonObject = new JSONObject(s);
            JSONArray sourcesJSON = jsonObject.getJSONArray("sources");

            // iterate over each source the api returned
            for (int i = 0; i < sourcesJSON.length(); i++) {
                JSONObject sourceJSON = sourcesJSON.getJSONObject(i);
                String id = sourceJSON.getString("id");
                String name = sourceJSON.getString("name");
                String category = sourceJSON.getString("category");
                NewsSource ns = new NewsSource(id, name, category);
                parsedSources.add(ns);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parsedSources;
    }
}
