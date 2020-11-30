package com.example.newsgateway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class NewsFragment extends Fragment {

    public NewsFragment() {}

    static NewsFragment newInstance(NewsArticle article, int index, int max) {
        NewsFragment f = new NewsFragment();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable("ARTICLE_DATA", article);
        bundle.putSerializable("INDEX", index);
        bundle.putSerializable("TOTAL", max);
        f.setArguments(bundle);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View fragment_view = inflater.inflate(R.layout.news_fragment, container, false);

        Bundle args = getArguments();

        if (args != null) {
            final NewsArticle currArticle = (NewsArticle) args.getSerializable("ARTICLE_DATA");
            if (currArticle == null)
                return null;

            int index = args.getInt("INDEX");
            int total = args.getInt("TOTAL");

            TextView headline = fragment_view.findViewById(R.id.articleHeadline);
            String title = currArticle.getTitle();
            if (title != null)
                headline.setText(title);
            else
                headline.setText("");
            headline.setOnClickListener(v -> openArticle(currArticle.getUrl()));

            TextView date = fragment_view.findViewById(R.id.articleDate);
            String published = currArticle.getPublishDate();
            if (!published.equals("null"))
                date.setText(published);
            else
                date.setText("");

            TextView authors = fragment_view.findViewById(R.id.articleAuthors);
            String auth = currArticle.getAuthor();
            if (!auth.equals("null"))
                authors.setText(auth);
            else
                authors.setText("");

            ImageView image = fragment_view.findViewById(R.id.articleImage);
            String imageUrl = currArticle.getImageUrl();
            if (!imageUrl.equals("null"))
                loadImage(imageUrl, image, currArticle);

            TextView description = fragment_view.findViewById(R.id.articleText);
            String desc = currArticle.getDescription();
            if (!desc.equals("null"))
                description.setText(desc);
            else
                description.setText("");
            description.setOnClickListener(v -> openArticle(currArticle.getUrl()));

            TextView pageNum = fragment_view.findViewById(R.id.pageNumber);
            pageNum.setText(String.format(Locale.US, "%d of %d", index, total));

            return fragment_view;
        } else {
            return null;
        }
    }


    // Picasso image download
    public void loadImage(final String url, ImageView imageView, NewsArticle currArticle) {
        Picasso.get().load(url)
                .into(imageView,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                imageView.setOnClickListener(v -> openArticle(currArticle.getUrl()));
                            }

                            @Override
                            public void onError(Exception e) {
                                // do nothing
                            }
                        });
    }

    public void openArticle(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
