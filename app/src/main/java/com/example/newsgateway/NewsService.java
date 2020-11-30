package com.example.newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class NewsService extends Service {

    private static final String TAG = "NewsService";
    private boolean running = true;
    private int count = 1;
    private final ArrayList<NewsArticle> newsArticles = new ArrayList<>();

    private ServiceReceiver serviceReceiver;

    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceReceiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter(ACTION_MSG_TO_SERVICE);
        registerReceiver(serviceReceiver, filter);

        new Thread(() -> {

            while (running) {
                while (newsArticles.isEmpty()) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

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
