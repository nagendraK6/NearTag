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
import com.squareup.picasso.Picasso;
import android.view.MotionEvent;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import jp.shts.android.storiesprogressview.StoriesProgressView;

/**
 * Created by nagendra on 9/26/18.
 */

public class StoryViewFragment extends Fragment implements StoriesProgressView.StoriesListener  {

    private StoriesProgressView storiesProgressView;
    private static final int PROGRESS_COUNT = 5;
    private ImageView image;
    private TextView txt;

    private int counter = 0;

    private final long[] durations = new long[]{
            500L, 1000L, 1500L, 4000L, 5000L, 1000,
    };

    long pressTime = 0L;
    long limit = 500L;
    ArrayList<String> all_images = new ArrayList<>();
    ArrayList<String> texts = new ArrayList<>();
    ArrayList<Integer> widths = new ArrayList<>();
    ArrayList<Integer> heights = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_stories_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        storiesProgressView = (StoriesProgressView) view.findViewById(R.id.stories);



        all_images = getArguments().getStringArrayList("urls");
        texts = getArguments().getStringArrayList("texts");

        widths = getArguments().getIntegerArrayList("widths");
        heights = getArguments().getIntegerArrayList("heights");

        // all_images = new ArrayList<>();
       // all_images.add("https://firebasestorage.googleapis.com/v0/b/firebase-satya.appspot.com/o/images%2Fi00001.jpg?alt=media&token=460667e4-e084-4dc5-b873-eefa028cec32");
        storiesProgressView.setStoriesCount(all_images.size()); // <- set stories
        storiesProgressView.setStoryDuration(5000L);
        storiesProgressView.setStoriesListener(this); // <- set listener
        storiesProgressView.startStories(); // <- start progress
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop();
        for (int i  =0; i < all_images.size(); i++) {
   //         Picasso.with(getActivity()).load(all_images.get(i)).fetch();
            Glide.with(this)
                    .load(all_images.get(i))
                    .apply(options)
            .preload();
        }

        image = (ImageView) view.findViewById(R.id.image);
        txt = view.findViewById(R.id.story_text);
        Glide.with(getActivity()).load(all_images.get(counter)).into(image);
                //Picasso.with(getContext()).load(all_images.get(counter)).into(image);
        if (!StringUtils.isEmpty(texts.get(counter))) {
            txt.setText(texts.get(counter));
        }  else {
            txt.setVisibility(View.GONE);
        }
  //      image.setImageResource(resources[counter]);

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
        Glide.with(getActivity()).load(all_images.get(++counter)).into(image);

        //Picasso.with(getContext()).load(all_images.get(++counter)).into(image);
        if (!StringUtils.isEmpty(texts.get(counter))) {
            txt.setText(texts.get(counter));
        }  else {
            txt.setVisibility(View.GONE);
        }
        Log.d("story_debug", "ht " + heights.get(counter).toString() + " widths " + widths.get(counter).toString());
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Glide.with(getActivity()).load(all_images.get(--counter)).into(image);
        //Picasso.with(getContext()).load(all_images.get(--counter)).into(image);
        if (!StringUtils.isEmpty(texts.get(counter))) {
            txt.setText(texts.get(counter));
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
}
