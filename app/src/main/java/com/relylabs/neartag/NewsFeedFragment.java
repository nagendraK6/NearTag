package com.relylabs.neartag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.util.Log;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.neartag.Utils.Logger;
import com.relylabs.neartag.composer.RecyclerGalaryFragment;
import com.relylabs.neartag.models.NewsFeedElement;
import com.relylabs.neartag.models.User;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 8/3/18.
 */

public class NewsFeedFragment extends Fragment {
    ArrayList<NewsFeedElement> all_feeds;
    NewsFeedAdapter adapter;
    RecyclerView news_feed_list;
    ProgressBar busy_show_feed_fetch;

    ImageView image_in_progress;
    View view_img_upload;
    SkeletonScreen skeletonScreen;

    BroadcastReceiver broadCastNewMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

                final String image_file_name =  intent
                        .getStringExtra(getString(R.string.user_selected_image));

                final String user_message =  intent
                        .getStringExtra("user_message");

                view_img_upload.setVisibility(View.VISIBLE);
                File f = new File(image_file_name);

            Picasso.with(getContext()).load(Uri.fromFile(f)).
                    resize(40, 40).
                    into(
                            image_in_progress,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );



                User user  = User.getLoggedInUser();
                NewsFeedElement new_post = new NewsFeedElement(
                        0,
                        "#ok",
                        "",
                        "",
                        "",
                        false,
                        false,
                        user_message,
                        image_file_name,
                        user.UserID,
                        user.Name,
                        user.Location,
                        new Long(0),
                        "",
                        "",
                        ""
                );

            createAPost(new_post);
            }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_feed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View fragment_view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment_view, savedInstanceState);

        view_img_upload = fragment_view.findViewById(R.id.upload_preview);
        image_in_progress = fragment_view.findViewById(R.id.upload_image);
        ImageView user_profile_shortlink = fragment_view.findViewById(R.id.user_profile_shortlink);
        user_profile_shortlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new UserProfileFragment());
            }
        });


        User user = User.getLoggedInUser();
        if (!StringUtils.isEmpty(user.ProfilePicURL)) {
            Picasso.with(getContext()).load(user.ProfilePicURL).
                    resize(40, 40).
                    into(
                            user_profile_shortlink,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );

        }
        news_feed_list = fragment_view.findViewById(R.id.news_feed_list);

;

        busy_show_feed_fetch = fragment_view.findViewById(R.id.busy_show_feed_fetch);
        FadingCircle cr = new FadingCircle();
        cr.setColor(getResources().getColor(R.color.neartagtextcolor));
        busy_show_feed_fetch.setIndeterminateDrawable(cr);
        // Initialize cont acts
        all_feeds = new ArrayList<NewsFeedElement>();
        // Create adapter passing in the sample user data
        adapter = new NewsFeedAdapter((AppCompatActivity) getActivity(), getActivity(), all_feeds);

        // Attach the adapter to the recyclerview to populate items
        news_feed_list.setAdapter(adapter);
        // Set layout manager to position the items
        // line inbetween the data rows

        FloatingActionButton fab =  fragment_view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new RecyclerGalaryFragment();
                Bundle bnd = new Bundle();
                bnd.putString("ref", "composer");
                frag.setArguments(bnd);
                loadFragment(frag);
            }
        });


        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        news_feed_list.setLayoutManager(layoutManager);
        getStandardViewList(2);
        getStandardViewList(50);


        skeletonScreen  = Skeleton.bind(news_feed_list)
                .adapter(adapter)
                .load(R.layout.item_skeletion_news)
                .frozen(false)
                .show();

        IntentFilter new_post = new IntentFilter("new_post");
        getActivity().registerReceiver(broadCastNewMessage, new_post);
    }

    private void getStandardViewList(Integer limit) {
        Logger.log(Logger.NEWS_FEED_FETCH_START);
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("limit", limit.toString());
        busy_show_feed_fetch.setVisibility(View.VISIBLE);
        User user = User.getLoggedInUser();
        // response

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<NewsFeedElement> feed_elements = new ArrayList<>();
                try {
                    JSONArray all_news_items = (JSONArray) response.getJSONArray("data");
                    if (all_news_items.length() > 0) {
                        new Delete().from(NewsFeedElement.class).execute();
                        for (int i =0; i < all_news_items.length(); i++) {
                            JSONObject obj = all_news_items.getJSONObject(i);
                            Integer post_id = obj.getInt("post_id");

                            String tag_text = obj.getString("tag_text");
                            String banner_image_url_low = obj.getString("banner_image_url_low");
                            String banner_image_url_high = obj.getString("banner_image_url_high");

                            String profile_image_url = obj.getString("profile_image_url");
                            Integer user_id = obj.getInt("user_id");
                            String message_text = obj.getString("message_text");
                            Boolean has_liked = obj.getBoolean("has_liked");
                            String user_name = obj.getString("user_name");
                            String user_location = obj.getString("user_location");
                            Long timeStamp = obj.getLong("timestamp");

                            String likes_count = obj.getString("likes_count");
                            String shared_count = obj.getString("shared_count");
                            String comments_count = obj.getString("comments_count");

                            NewsFeedElement current_element = new NewsFeedElement(
                                    post_id,
                                    tag_text,
                                    banner_image_url_low,
                                    banner_image_url_high,
                                    profile_image_url,
                                    has_liked,
                                    true,
                                    message_text,
                                    "",
                                    user_id,
                                    user_name,
                                    user_location,
                                    timeStamp,
                                    likes_count,
                                    shared_count,
                                    comments_count
                            );

                            feed_elements.add(current_element);
                        }

                        all_feeds.clear();
                        all_feeds.addAll(feed_elements);
                        skeletonScreen.hide();
                        adapter.notifyDataSetChanged();
                        Logger.log(Logger.NEWS_FEED_FETCH_SUCCESS);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                if(all_feeds.size() == 0) {
                    // read from local storage
                    List<NewsFeedElement> feed_elements = new Select()
                            .all()
                            .from(NewsFeedElement.class)
                            .execute();
                    all_feeds.addAll(feed_elements);
                    skeletonScreen.hide();
                    adapter.notifyDataSetChanged();
                }
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.RES, res);
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.NEWS_FEED_FETCH_FAILED, log_data);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                if(all_feeds.size() == 0) {
                    // read from local storage
                    List<NewsFeedElement> feed_elements = new Select()
                            .all()
                            .from(NewsFeedElement.class)
                            .execute();
                    all_feeds.addAll(feed_elements);
                    skeletonScreen.hide();
                    adapter.notifyDataSetChanged();
                }
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.JSON, obj.toString());
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.NEWS_FEED_FETCH_FAILED, log_data);
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.get(App.getBaseURL() + "newsfeed", params, response_json);
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        news_feed_list.setAdapter(null);
        news_feed_list.setLayoutManager(null);
        App.getRefWatcher(getActivity()).watch(this);
        getActivity().unregisterReceiver(broadCastNewMessage);
    }


    private void createAPost(final NewsFeedElement current_element) {
        Logger.log(Logger.POST_CREATE_START);
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
                current_element.setHasPublished(true);
                all_feeds.add(0, current_element);
                adapter.notifyDataSetChanged();
                view_img_upload.setVisibility(View.GONE);
                Logger.log(Logger.POST_CREATE_SUCCESS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.RES, res);
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.POST_CREATE_FAILED, log_data);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.JSON, obj.toString());
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.POST_CREATE_FAILED, log_data);
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "post/create", params, jrep);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.OnSharingCallback(requestCode);
    }
}
