package com.neartag.in.stories;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.neartag.in.R;
import com.neartag.in.models.StoryElement;
import com.squareup.picasso.Picasso;
import android.view.MotionEvent;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;

import jp.shts.android.storiesprogressview.StoriesProgressView;

/**
 * Created by nagendra on 9/26/18.
 */

public class StoryViewFragment extends Fragment implements StoriesProgressView.StoriesListener  {

    private StoriesProgressView storiesProgressView;
    private ImageView image;
    private TextView txt;

    private int counter = 0;
    ArrayList<StoryElement> all_stories;
    long pressTime = 0L;
    long limit = 500L;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_stories_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        storiesProgressView = view.findViewById(R.id.stories);

        all_stories =  getArguments().getParcelableArrayList("stories");

        storiesProgressView.setStoriesCount(all_stories.size()); // <- set stories
        storiesProgressView.setStoryDuration(5000L);
        storiesProgressView.setStoriesListener(this); // <- set listener
        storiesProgressView.startStories(); // <- start progress

        image = view.findViewById(R.id.image);
        txt = view.findViewById(R.id.story_text);
        preLoading();
        loadImage(counter);

        if (!StringUtils.isEmpty(all_stories.get(counter).getText())) {
            txt.setText(all_stories.get(counter).getText());
        }  else {
            txt.setVisibility(View.GONE);
        }

        // bind reverse view
        View reverse = view.findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = view.findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onNext() {
        counter++;
        loadImage(counter);
        if (!StringUtils.isEmpty(all_stories.get(counter).getText())) {
            txt.setText(all_stories.get(counter).getText());
        }  else {
            txt.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        --counter;
        loadImage(counter);
        if (!StringUtils.isEmpty(all_stories.get(counter).getText())) {
            txt.setText(all_stories.get(counter).getText());
        }  else {
            txt.setVisibility(View.GONE);
        }
    }

    @Override
    public void onComplete() {
        loadFragment();
    }

    @Override
    public void onDestroy() {
        // Very important !
        storiesProgressView.destroy();
        super.onDestroy();
    }

    private void loadFragment() {
        getActivity().onBackPressed();
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    private void loadImage(int counter) {
        if (all_stories.get(counter).getLocalFile()) {
            RequestOptions options_2 = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .centerCrop();

            Glide.with(getContext()).load(
                    new File(all_stories.get(counter).getBannerImageURLHigh())).apply(options_2).into(image);
        } else {
            Glide.with(getContext()).load(
                    all_stories.get(counter).getBannerImageURLHigh()

            ).into(image);
        }
    }

    private void preLoading() {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .centerCrop();


        for (int i  = 0; i < all_stories.size(); i++) {
            if (!all_stories.get(i).getLocalFile()) {
                Glide.with(this).load(all_stories.get(i).getBannerImageURLHigh()).apply(options).preload();
            }
        }
    }
}
