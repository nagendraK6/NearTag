package com.neartag.in.stories;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.neartag.in.R;
import com.squareup.picasso.Picasso;
import android.view.MotionEvent;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_stories_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        storiesProgressView = (StoriesProgressView) view.findViewById(R.id.stories);
        storiesProgressView.setStoriesCount(PROGRESS_COUNT); // <- set stories
        storiesProgressView.setStoryDuration(3000L);
        storiesProgressView.setStoriesListener(this); // <- set listener
        storiesProgressView.startStories(); // <- start progress


        all_images = getArguments().getStringArrayList("urls");
        texts = getArguments().getStringArrayList("texts");
        image = (ImageView) view.findViewById(R.id.image);
        txt = view.findViewById(R.id.story_text);
        Picasso.with(getContext()).load(all_images.get(counter)).into(image);
        txt.setText(texts.get(counter));
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
        Picasso.with(getContext()).load(all_images.get(++counter)).into(image);
        txt.setText(texts.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        Picasso.with(getContext()).load(all_images.get(--counter)).into(image);
        txt.setText(texts.get(counter));
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
