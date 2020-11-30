package com.example.newsgateway;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NewsArticleDownloader implements Runnable {

    private static final String TAG = "NewsArticleDownloader";
    private NewsService.ServiceReceiver serviceReceiver;
    private NewsService newsService;
    private String newsSource;
    private String DATA_URL;
    private static final String API_KEY = "bf9a91912be344539b68c21f2d934609";

    private static final ZoneId userTimeZone = ZoneId.of("CST");
    @SuppressLint("ConstantLocale")
    private static final DateTimeFormatter localizedFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault(Locale.Category.FORMAT));

    ArrayList<NewsArticle> articles = new ArrayList<>();

    public NewsArticleDownloader(NewsService.ServiceReceiver serviceReceiver, NewsService newsService, String newsSource) {
        this.serviceReceiver = serviceReceiver;
        this.newsService = newsService;
        this.newsSource = newsSource;
    }

    @Override
    public void run() {

        Log.d(TAG, "run: STARTING ARTICLE DOWNLOADING FOR SOURCE " + newsSource);

        DATA_URL = "https://newsapi.org/v2/top-headlines?sources=" +
                newsSource +
                "&language=en&apiKey=" +
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

        // obtain list of articles from parseJSON and process in NewsService
        articles = parseJSON(s);
        newsService.setArticles(articles);
    }

    private ArrayList<NewsArticle> parseJSON(String s) {

        ArrayList<NewsArticle> parsedArticles = new ArrayList<>();

        try {

            JSONObject jsonObject = new JSONObject(s);
            JSONArray articlesJSON = jsonObject.getJSONArray("articles");

            // iterate over each source the api returned
            for (int i = 0; i < articlesJSON.length(); i++) {
                JSONObject articleJSON = articlesJSON.getJSONObject(i);
                String author = articleJSON.getString("author");
                String title = articleJSON.getString("title");
                String description = articleJSON.getString("description");
                String url = articleJSON.getString("url");
                String imageUrl = articleJSON.getString("urlToImage");
                String date = articleJSON.getString("publishedAt");
//                if (date.contains("T"))
//                    date = date.replace("T", ", ");
//                if (date.contains("Z"))
//                    date = date.replace("Z", "");
                String newDate = Instant.parse(date).atZone(userTimeZone).format(localizedFormatter);
                NewsArticle na = new NewsArticle(author, title, description, url, imageUrl, newDate);
                parsedArticles.add(na);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parsedArticles;
    }
}
