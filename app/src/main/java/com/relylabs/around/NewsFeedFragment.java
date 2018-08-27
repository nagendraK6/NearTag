package com.relylabs.around;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.activeandroid.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.around.composer.ComposerFragment;
import com.relylabs.around.composer.RecyclerGalaryFragment;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 8/3/18.
 */

public class NewsFeedFragment extends Fragment {
    ArrayList<NewsFeedElement> all_feeds;
    NewsFeedAdapter adapter;
    RecyclerView news_feed_list;
    ProgressBar busy_show_feed_fetch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_feed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View fragment_view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment_view, savedInstanceState);

        ImageView user_profile_shortlink = fragment_view.findViewById(R.id.user_profile_shortlink);
        user_profile_shortlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new UserProfileFragment());
            }
        });

        news_feed_list = (RecyclerView) fragment_view.findViewById(R.id.news_feed_list);
        busy_show_feed_fetch = (ProgressBar) fragment_view.findViewById(R.id.busy_show_feed_fetch);
        // Initialize cont acts
        all_feeds = new ArrayList<NewsFeedElement>();
        // Create adapter passing in the sample user data
        adapter = new NewsFeedAdapter(getActivity(), getActivity(), all_feeds);

        // Attach the adapter to the recyclerview to populate items
        news_feed_list.setAdapter(adapter);
        // Set layout manager to position the items
        // line inbetween the data rows

        FloatingActionButton fab =  fragment_view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new RecyclerGalaryFragment());
            }
        });


        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        news_feed_list.setLayoutManager(layoutManager);

        if (getArguments() != null) {
            final String image_file_name =  getArguments()
                    .getString(getString(R.string.user_selected_image));

            final String user_message =  getArguments()
                    .getString("user_message");

            NewsFeedElement new_post = new NewsFeedElement(
                    0,
                    "#ok",
                    "",
                    "",
                    false,
                    false,
                    user_message,
                    image_file_name
            );

            all_feeds.add(new_post);
            createAPost(new_post);
          //  adapter.notifyDataSetChanged();
        }




        getStandardViewList();
    }

    private void getStandardViewList() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        busy_show_feed_fetch.setVisibility(View.VISIBLE);
        User user = User.getLoggedInUser();
        // response

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<NewsFeedElement> feed_elements = new ArrayList<>();
                try {
                    JSONArray all_contests = (JSONArray) response.getJSONArray("data");
                    for (int i =0; i < all_contests.length(); i++) {
                        JSONObject obj = all_contests.getJSONObject(i);
                        Integer post_id = (Integer) obj.getInt("post_id");

                        String tag_text = (String) obj.getString("tag_text");
                        String image_url = (String) obj.getString("image_url");
                        String profile_image_url = (String) obj.getString("profile_image_url");
                        String message_text = (String) obj.getString("message_text");
                        Boolean has_liked = (Boolean) obj.getBoolean("has_liked");

                        NewsFeedElement current_element = new NewsFeedElement(
                                post_id,
                                tag_text,
                                image_url,
                                profile_image_url,
                                has_liked,
                                true,
                                message_text,
                                ""
                        );

                        feed_elements.add(current_element);
                    }

                    all_feeds.addAll(feed_elements);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                Log.d("debug_data", " " + statusCode);
                if (statusCode == 401) {
                }
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.get(App.getBaseURL() + "newsfeed", params, response_json);
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        news_feed_list.setAdapter(null);
        news_feed_list.setLayoutManager(null);
        App.getRefWatcher(getActivity()).watch(this);
    }


    private void createAPost(
            NewsFeedElement current_element
    ) {
        android.util.Log.d("debug_data", "upload started...");
        User user = User.getLoggedInUser();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            File imgfile = new File(current_element.getGalleryImageFile());
            params.put("file", imgfile);
            params.put("tag", current_element.getTag());
            params.put("post_text", current_element.getUserPostText());
        } catch(FileNotFoundException fexception) {
            fexception.printStackTrace();
        }
        JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                android.util.Log.d("debug_data", "uploaded the image on server...");
                all_feeds.get(0).setHasPublished(true);
                adapter.notifyItemChanged(0);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                HashMap logData = new HashMap<String, String>();
                logData.put("status_code", statusCode);
                logData.put("message", t.getMessage());
                android.util.Log.d("upload_image", "the errorstring: " +logData);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                android.util.Log.d("upload_image", "the errorstring: ");
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "post/create", params, jrep);
    }
}
