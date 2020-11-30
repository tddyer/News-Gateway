package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // maps new source names to source objects
    private HashMap<String, NewsSource> currentCategorySourcesData = new HashMap<>();
//    private HashMap<String, ArrayList<String>> categorySourcesData = new HashMap<>();

    // contains all new sources that cover a given category
    private ArrayList<String> sourcesDisplayed = new ArrayList<>();

    private ArrayList<String> currentCategories;

    // menu + drawer vars
    private Menu optionsMenu;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private ArrayAdapter<String> arrayAdapter;
    private String currentSource;

    // fragment vars
    private List<Fragment> newsFragments;
    private CustomPageAdapter pageAdapter;
    private ViewPager viewPager;

    private NewsReceiver newsReceiver;

    static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start NewsService
        Intent intent = new Intent(MainActivity.this, NewsService.class);
        startService(intent);

        // create NewsReceiver
        newsReceiver = new NewsReceiver(this);


        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.drawer_list);
        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, sourcesDisplayed);
        drawerList.setAdapter(arrayAdapter);

        // setup drawer item onClick
        drawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectItem(position);
                    drawerLayout.closeDrawer(drawerList);
                }
        );

        // setup drawer toggle
        drawerToggle = new ActionBarDrawerToggle(
                this, // host activity
                drawerLayout, // drawerLayout obj
                R.string.access_open, // accessibility desc for opening
                R.string.access_close // accessibility desc for closing
        );

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        newsFragments = new ArrayList<>();
        pageAdapter = new CustomPageAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(pageAdapter);

        // load data
        if (currentCategorySourcesData.isEmpty()) {
            NewsSourceDownloader nsd = new NewsSourceDownloader(this, "");
            new Thread(nsd).start();
        }

    }

    @Override
    protected void onPostResume() {
        IntentFilter filter = new IntentFilter(ACTION_NEWS_STORY);
        newsReceiver = new NewsReceiver(this);
        registerReceiver(newsReceiver, filter);
        super.onPostResume();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(newsReceiver);
        super.onStop();
    }

    // sets sources after the filtered sources have been downloaded from NewsSourceDownloader
    public void setSources(ArrayList<NewsSource> sources, ArrayList<String> categories) {

        // flush out old source data
        currentCategorySourcesData.clear();
        sourcesDisplayed.clear();


        if (sources != null) {

            // fill sources list (that displays sources in side drawer) using new sources data
            for (NewsSource s : sources)
                sourcesDisplayed.add(s.getName());
            Collections.sort(sourcesDisplayed);

            // fill sources map with new sources
            for (NewsSource s : sources)
                currentCategorySourcesData.put(s.getName(), s);
        }


        if (categories != null && currentCategories == null) {
            currentCategories = new ArrayList<>();
            currentCategories.addAll(categories);
            Collections.sort(currentCategories);
            currentCategories.add(0, "all");

            // update options menu items with new categories
            optionsMenu.clear();
            for (String cat : currentCategories) {
                optionsMenu.add(cat);
            }
        }

        arrayAdapter.notifyDataSetChanged();

    }

    private void selectItem(int pos) {
        // change bg from image to null so text is readable
        viewPager.setBackground(null);
        currentSource = sourcesDisplayed.get(pos);
        setTitle(currentSource);

        // TODO: convert this runnable format to a broadcasted intent
//        SubRegionLoaderRunnable asrl = new SubRegionLoaderRunnable(this, currentSubRegion);
//        new Thread(asrl).start();

        drawerLayout.closeDrawer(drawerList);
    }



    // Menu onClick (also opens drawer using drawerToggle)


    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: drawer_toggle " + item);
            return true;
        }

        // download news source data
//        if (currentCategorySourcesData.isEmpty()) {
        NewsSourceDownloader nsd = new NewsSourceDownloader(this, item.getTitle().toString());
        new Thread(nsd).start();
//        }
//
//        sourcesDisplayed.clear();
//        NewsSource lst = currentCategorySourcesData.get(item.getTitle().toString());
//        if (lst != null) {
//            for (NewsSource src : lst)
//                sourcesDisplayed.add(src.getName());
//        }
//
//        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);

    }



    // setup options menu reference so values can be dynamically loaded
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.optionsMenu = menu;
        return true;
    }


    // drawer-toggle overrides


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }




    /* ---------------- CUSTOM CLASSES ---------------- */



    private class CustomPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;

        CustomPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return newsFragments.get(position);
        }

        @Override
        public int getCount() {
            return newsFragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }
    }

    public class NewsReceiver extends BroadcastReceiver {

        private static final String TAG = "NewsReceiver";
        private MainActivity mainActivity;

        public NewsReceiver(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action == null)
                return;

            if (action == ACTION_NEWS_STORY) {
                // get the source and run article downloader using the source
                ArrayList<NewsArticle> articles = (ArrayList<NewsArticle>) intent.getSerializableExtra("ARTICLES");
                redoFragments(articles);
            }
        }

        public void redoFragments(ArrayList<NewsArticle> articles) {
            setTitle(mainActivity.currentSource);

            for (int i = 0; i < pageAdapter.getCount(); i++)
                pageAdapter.notifyChangeInPosition(i);

            newsFragments.clear();

            for (int i = 0; i < articles.size(); i++) {
                newsFragments.add(
                        NewsFragment.newInstance(articles.get(i), i+1, articles.size()));
            }

            pageAdapter.notifyDataSetChanged();
            viewPager.setCurrentItem(0);
        }
    }
}