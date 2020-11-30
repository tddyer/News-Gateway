package com.example.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NewsService extends Service {

    private static final String TAG = "NewsService";
    private boolean running = true;
    private int count = 1;
    private final ArrayList<NewsArticle> newsArticles = new ArrayList<>();

    private ServiceReceiver serviceReceiver;

    private NewsService newsService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: STARTING NEWSSERVICE");

        newsService = this;
        
        serviceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_MSG_TO_SERVICE);
        registerReceiver(serviceReceiver, filter);

        new Thread(() -> {

            while (running) {
                Log.d(TAG, "onStartCommand: NEWSSERVICE THREAD RUNNING - WAITING FOR ARTICLES");
                while (newsArticles.isEmpty()) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d(TAG, "onStartCommand: NEWSSERVICE THREAD RUNNING - ARTICLES RECEIVED");
                Intent intent1 = new Intent();
                intent1.setAction(MainActivity.ACTION_NEWS_STORY);
                intent1.putExtra("ARTICLES", newsArticles);
                sendBroadcast(intent1);

                newsArticles.clear();
            }
        }).start();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(serviceReceiver);
        running = false;
        super.onDestroy();
    }

    public void setArticles(ArrayList<NewsArticle> articles) {
        // flush out old articles
        newsArticles.clear();

        // re-populate with new articles
        newsArticles.addAll(articles);
    }



    /* ------------ Service Receiver Class ------------ */

    class ServiceReceiver extends BroadcastReceiver {

        private static final String TAG = "ServiceReceiver";

        public ServiceReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive: SERVICE RECEIVED");

            String action = intent.getAction();

            if (action == null)
                return;

            if (action == MainActivity.ACTION_MSG_TO_SERVICE) {
                // get the source and run article downloader using the source
                String source = intent.getStringExtra("SOURCE");
                Log.d(TAG, "onReceive: LAUNCHING ARTICLE DOWNLOADER WITH SOURCE " + source);
                NewsArticleDownloader nad = new NewsArticleDownloader(this, newsService, source);
                new Thread(nad).start();
            }
        }
    }

}
