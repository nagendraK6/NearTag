package com.relylabs.around;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by nagendra on 8/17/18.
 */

public class TagSearchFragment extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tag_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            final String image_file_name =  getArguments()
                    .getString(getString(R.string.user_selected_image));

            final ImageView user_post_image = view.findViewById(R.id.user_post_image);
            Picasso.with(getContext()).load(new File(image_file_name))
                    .into(user_post_image);

            TextView post_create_btn = view.findViewById(R.id.create_post);
            final EditText composer_post_text = view.findViewById(R.id.composer_post_text);
            composer_post_text.requestFocus();

            post_create_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle data_bundle = new Bundle();
                    data_bundle.putString(getString(R.string.user_selected_image), image_file_name);
                    data_bundle.putString("user_message", composer_post_text.getText().toString());
                    Fragment news_feed_fragment = new NewsFeedFragment();
                    news_feed_fragment.setArguments(data_bundle);
                    loadFragment(news_feed_fragment);
                }
            });
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commitAllowingStateLoss();
    }
}
