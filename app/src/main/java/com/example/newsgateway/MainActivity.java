package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

    private ArrayList<String> currentCategories = new ArrayList<>();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: START SERVICE (NewsService) HERE

        // TODO: CREATE NewsReceiver object HERE


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
//        if (categorySourcesData.isEmpty()) {
//            NewsSourceDownloader nsd = new NewsSourceDownloader(this, "");
//            new Thread(nsd).start();
//        }

    }


    // sets sources after downloaded (categories is already filtered down based off of the
    // selection that triggered the source downloader)
    private void setSources(ArrayList<NewsSource> sources, ArrayList<String> categories) {

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


        if (currentCategories == null && categories != null) {
            currentCategories = new ArrayList<>();
            currentCategories = new ArrayList<>();
            currentCategories.addAll(categories);
            Collections.sort(currentCategories);
        }
        currentCategories.add("all");

//        for (String cat : currentCategories) {
//            optionsMenu.add(cat);
//        }

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
//        if (categorySourcesData.isEmpty()) {
//            NewsSourceDownloader nsd = new NewsSourceDownloader(this, item.getTitle().toString());
//            new Thread(nsd).start();
//        }

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
        optionsMenu = menu;
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
}