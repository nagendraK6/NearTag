package com.neartag.in;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.neartag.in.R;
import com.neartag.in.composer.RecommendedTagsListAdapter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nagendra on 8/17/18.
 *
 */

public class TagSearchFragment extends Fragment {
    RecyclerView recommended_tags_list;
    RecommendedTagsListAdapter recommendedTagsListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tag_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            final String image_file_name =  getArguments()
                    .getString(getString(R.string.user_selected_image));

            final ImageView user_post_image = view.findViewById(R.id.user_post_image);
            if (image_file_name != null) {
                Picasso.with(getContext()).load(new File(image_file_name))
                        .into(user_post_image);
            }

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

        recommended_tags_list = view.findViewById(R.id.recommended_tags_list);
        ArrayList<String> all_tags_data = new ArrayList<>();


        all_tags_data.add("#गुड मॉर्निंग");
        all_tags_data.add("#आज का दिन");
        all_tags_data.add("#सचिन तेंदुलकर");
        all_tags_data.add("#पॉलिटिक्स");
        all_tags_data.add("#भारत");
        all_tags_data.add("#महंगाई");
        all_tags_data.add("#मोदी");
        all_tags_data.add("#राहुल गाँधी");
        all_tags_data.add("#लालू यादव");
        all_tags_data.add("#आरक्षण");


        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        recommended_tags_list.setLayoutManager(layoutManager);
        recommendedTagsListAdapter = new RecommendedTagsListAdapter(getContext(), all_tags_data);
        recommended_tags_list.setAdapter(recommendedTagsListAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        recommended_tags_list.setLayoutManager(null);
        recommended_tags_list.setAdapter(null);
        recommendedTagsListAdapter = null;
    }

    private void loadFragment(Bundle bundle) {
        Intent intent=new Intent("new_post");
        intent.putExtras(bundle);
        getActivity().sendBroadcast(intent);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
