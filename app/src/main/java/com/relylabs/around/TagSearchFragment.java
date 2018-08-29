package com.relylabs.around;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.gravity.IChildGravityResolver;
import com.beloo.widget.chipslayoutmanager.layouter.breaker.IRowBreaker;
import com.relylabs.around.composer.TagsListAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nagendra on 8/17/18.
 */

public class TagSearchFragment extends Fragment {
    RecyclerView tags_list;
    ChipsLayoutManager chipsLayoutManager;
    TagsListAdapter adapter;

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
                    loadFragment(data_bundle);
                }
            });
        }

        tags_list = view.findViewById(R.id.all_tags);
        ArrayList<String> all_tags_data = new ArrayList<>();
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");
        all_tags_data.add("देश की आजादी");

        chipsLayoutManager = ChipsLayoutManager.newBuilder(getContext())
                //set vertical gravity for all items in a row. Default = Gravity.CENTER_VERTICAL
                .setChildGravity(Gravity.TOP)
                //whether RecyclerView can scroll. TRUE by default
                .setScrollingEnabled(true)
                //set maximum views count in a particular row
                .setMaxViewsInRow(5)
                //set gravity resolver where you can determine gravity for item in position.
                //This method have priority over previous one
                .setGravityResolver(new IChildGravityResolver() {
                    @Override
                    public int getItemGravity(int position) {
                        return Gravity.CENTER;
                    }
                })
                //you are able to break row due to your conditions. Row breaker should return true for that views
                .setRowBreaker(new IRowBreaker() {
                    @Override
                    public boolean isItemBreakRow(@IntRange(from = 0) int position) {
                        return false;
                    }
                })
                //a layoutOrientation of layout manager, could be VERTICAL OR HORIZONTAL. HORIZONTAL by default
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                // row strategy for views in completed row, could be STRATEGY_DEFAULT, STRATEGY_FILL_VIEW,
                //STRATEGY_FILL_SPACE or STRATEGY_CENTER
                .setRowStrategy(ChipsLayoutManager.STRATEGY_FILL_VIEW)
                // whether strategy is applied to last row. FALSE by default
                .withLastRow(false)
                .build();

        tags_list.setLayoutManager(chipsLayoutManager);
        adapter = new TagsListAdapter(getContext(), all_tags_data);
        tags_list.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        adapter = null;
        chipsLayoutManager = null;
        tags_list.setLayoutManager(null);
        tags_list.setAdapter(null);
    }

    private void loadFragment(Bundle bundle) {
        Intent intent=new Intent("new_post");
        intent.putExtras(bundle);
        getActivity().sendBroadcast(intent);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
