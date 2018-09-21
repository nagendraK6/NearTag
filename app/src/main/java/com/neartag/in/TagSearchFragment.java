package com.neartag.in;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neartag.in.Utils.Logger;
import com.neartag.in.Utils.SquareImageView;
import com.neartag.in.composer.AutoCompleteAdapter;
import com.neartag.in.composer.HashTagAutoCompleteTextView;
import com.neartag.in.composer.RecommendedTagsListAdapter;
import com.neartag.in.models.User;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.WeakHashMap;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/17/18.
 *
 */

public class TagSearchFragment extends Fragment implements RecommendedTagsListAdapter.ItemClickListener {

    BroadcastReceiver broadCastNewMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            View busy_fetch = fragment_view.findViewById(R.id.busy_fetch_tag);

            final String enable =  intent
                    .getStringExtra("enable");
            if (enable.equals("1")) {
                busy_fetch.setVisibility(View.VISIBLE);
            } else {
                busy_fetch.setVisibility(View.INVISIBLE);
            }

        }
    };

    RecyclerView recommended_tags_list;
    RecommendedTagsListAdapter recommendedTagsListAdapter;
    ArrayList<String> all_tags_list = new ArrayList<>();
    HashTagAutoCompleteTextView composer_post_text;
    View fragment_view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.tag_search_fragment, container, false);
        return fragment_view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            final String image_file_name =  getArguments()
                    .getString(getString(R.string.user_selected_image));

            final SquareImageView user_post_image = view.findViewById(R.id.user_post_image);
            if (image_file_name != null) {
                Picasso.with(getContext()).load(new File(image_file_name))
                        .into(user_post_image);
            }

            TextView post_create_btn = view.findViewById(R.id.tvNext);

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

        composer_post_text = view.findViewById(R.id.composer_post_text);
        composer_post_text.requestFocus();

        CircleImageView user_profile_image = view.findViewById(R.id.user_profile_image);
        User user = User.getLoggedInUser();
        if (!StringUtils.isEmpty(user.ProfilePicURL)) {
            Picasso.with(getContext()).load(user.ProfilePicURL).into(user_profile_image);
        }

        TextView user_name = view.findViewById(R.id.user_name);
        user_name.setText(user.Name);

        recommended_tags_list = view.findViewById(R.id.recommended_tags_list);

        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        recommended_tags_list.setLayoutManager(layoutManager);
        recommendedTagsListAdapter = new RecommendedTagsListAdapter(getContext(), all_tags_list);
        recommended_tags_list.setAdapter(recommendedTagsListAdapter);
        recommendedTagsListAdapter.setClickListener(this);
        fetchRecommendedList(1);

        AutoCompleteAdapter adapter = new AutoCompleteAdapter(getContext(), R.layout.custom_row);

        composer_post_text.setThreshold(1);
        composer_post_text.setAdapter(adapter);
        adapter.setCursorPositionListener(new AutoCompleteAdapter.CursorPositionListener() {
            @Override
            public int currentCursorPosition() {
                return composer_post_text.getSelectionStart();
            }
        });

        IntentFilter new_text = new IntentFilter("on_message");
        getActivity().registerReceiver(broadCastNewMessage, new_text);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        recommended_tags_list.setLayoutManager(null);
        recommended_tags_list.setAdapter(null);
        composer_post_text.setAdapter(null);
        recommendedTagsListAdapter = null;
        getActivity().unregisterReceiver(broadCastNewMessage);
    }

    private void loadFragment(Bundle bundle) {
        Intent intent=new Intent("new_post");
        intent.putExtras(bundle);
        getActivity().sendBroadcast(intent);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void fetchRecommendedList(Integer limit) {
        Logger.log(Logger.RECOMMENDED_LIST_FETCH_START);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("limit", limit.toString());
        User user = User.getLoggedInUser();
        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<String> all_tags = new ArrayList<>();
                try {
                    JSONArray all_tags_data = response.getJSONArray("data");
                    if (all_tags_data.length() > 0) {
                        for (int i =0; i < all_tags_data.length(); i++) {
                            JSONObject obj = all_tags_data.getJSONObject(i);
                            Integer tag_id = obj.getInt("id");
                            String tag_text = "#" + obj.getString("Name");

                            all_tags.add(tag_text);
                        }

                        all_tags_list.clear();
                        all_tags_list.addAll(all_tags);
                        recommendedTagsListAdapter.notifyDataSetChanged();
                        Logger.log(Logger.RECOMMENDED_LIST_FETCH_FAILED);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.RES, res);
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.RECOMMENDED_LIST_FETCH_FAILED, log_data);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                if (obj != null) {
                    log_data.put(Logger.JSON, obj.toString());
                }
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.RECOMMENDED_LIST_FETCH_FAILED, log_data);
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.get(App.getBaseURL() + "post/getRecommendedTags", params, response_json);
    }

    @Override
    public void onItemClick(View view, int position) {
        // tag at position click
        String tag_text = all_tags_list.get(position);
        String current_text = composer_post_text.getText().toString();
        if (StringUtils.isEmpty(current_text)) {
            composer_post_text.getText().append(tag_text + " ");
        } else {
            Character lastChar = current_text.charAt(current_text.length() -1);
            if (lastChar == ' ') {
                composer_post_text.getText().append(tag_text + " ");
            } else if (lastChar == '#') {
                composer_post_text.getText().append(tag_text.substring(1) + " ");
            } else {
                composer_post_text.getText().append(" " + tag_text + " ");
            }
        }
    }
}
