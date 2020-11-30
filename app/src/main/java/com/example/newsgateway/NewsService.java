package com.example.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NewsService extends Service {

    private static final String TAG = "NewsService";
    private boolean running = true;
    private int count = 1;
    private final ArrayList<NewsArticle> newsArticles = new ArrayList<>();

    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        // 1: unregister service receiver

        // 2: set service's thread's running flag to false

        super.onDestroy();
    }

    public void setArticles(ArrayList<NewsArticle> articles) {
        // flush out old articles
        newsArticles.clear();

        // re-populate with new articles
        newsArticles.addAll(articles);
    }



    /* ------------ Service Receiver Class ------------ */

    public class ServiceReceiver extends BroadcastReceiver {

        private static final String TAG = "ServiceReceiver";
        private NewsService newsService;

        public ServiceReceiver(NewsService newsService) {
            this.newsService = newsService;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action == null)
                return;

            if (action == NewsService.ACTION_MSG_TO_SERVICE) {
                // get the source and run article downloader using the source
                String source = intent.getStringExtra("SOURCE");
                NewsArticleDownloader nad = new NewsArticleDownloader(this, source);
                new Thread(nad).start();
            }
        }
    }

}
