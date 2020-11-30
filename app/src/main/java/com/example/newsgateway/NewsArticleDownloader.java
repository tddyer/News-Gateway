package com.example.newsgateway;

public class NewsArticleDownloader implements Runnable {

    private static final String TAG = "NewsSourceDownloader";
    private MainActivity mainActivity;
    private NewsService.ServiceReceiver serviceReceiver;
    private String newsSource;
    private String DATA_URL;
    private static final String API_KEY = "bf9a91912be344539b68c21f2d934609";

    public NewsArticleDownloader(NewsService.ServiceReceiver serviceReceiver, String newsSource) {
        this.serviceReceiver = serviceReceiver;
        this.newsSource = newsSource;
    }

    @Override
    public void run() {

    }

    private void parseJSON(String s) {

    }
}
