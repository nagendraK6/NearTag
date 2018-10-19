package com.neartag.in.newsfeed;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neartag.in.App;
import com.neartag.in.DeviceUtils;
import com.neartag.in.PreCachingLayoutManager;
import com.neartag.in.R;
import com.neartag.in.UserProfileFragment;
import com.neartag.in.Utils.Logger;
import com.neartag.in.camera.Camera2Fragment;
import com.neartag.in.composer.RecyclerGalaryFragment;
import com.neartag.in.models.NewsFeedElement;
import com.neartag.in.models.StoryBucket;
import com.neartag.in.models.StoryElement;
import com.neartag.in.models.User;
import com.neartag.in.sharing.FragmentShareView;
import com.neartag.in.stories.StoryViewFragment;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.WeakHashMap;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/3/18.
 */

public class StoryFeedFragment extends Fragment  implements  StoryFeedAdapter.StoryViewListener{
    ArrayList<StoryBucket> feed_buckets;
    StoryFeedAdapter adapter;
    RecyclerView news_feed_list;
    ProgressBar busy_show_feed_fetch;

    ImageView image_in_progress;
    View view_img_upload;

    SkeletonScreen skeletonScreen;
    View tag_selection_view;

    BroadcastReceiver broadCastNewMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            appendNewStory();
        }
    };

    public static StoryFeedFragment newInstance(){
        return new StoryFeedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.news_feed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View fragment_view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragment_view, savedInstanceState);

        view_img_upload = fragment_view.findViewById(R.id.upload_preview);
        image_in_progress = fragment_view.findViewById(R.id.upload_image);
        CircleImageView user_profile_shortlink = fragment_view.findViewById(R.id.user_profile_shortlink);
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

        } else {
            TextView empty_profile = fragment_view.findViewById(R.id.empty_profile_image);
            empty_profile.setVisibility(View.VISIBLE);
            empty_profile.setText(user.Name.substring(0, 1));
        }
        news_feed_list = fragment_view.findViewById(R.id.news_feed_list);

        busy_show_feed_fetch = fragment_view.findViewById(R.id.busy_show_feed_fetch);
        FadingCircle cr = new FadingCircle();
        cr.setColor(getResources().getColor(R.color.neartagtextcolor));
        busy_show_feed_fetch.setIndeterminateDrawable(cr);
        // Initialize cont acts
        feed_buckets = new ArrayList<StoryBucket>();
        // Create adapter passing in the sample user data
        adapter = new StoryFeedAdapter((AppCompatActivity) getActivity(), getActivity(), feed_buckets);
        tag_selection_view = fragment_view.findViewById(R.id.tag_selection_view);
        ImageView tag_cancel = tag_selection_view.findViewById(R.id.tag_cancel);
        adapter.setStoryViewListener(this);
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
                startCamera2();
                //loadFragment(Camera2Fragment.newInstance());
            }
        });


        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        news_feed_list.setLayoutManager(layoutManager);
        getStandardViewList(1);
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
        RequestParams params = new RequestParams();
        params.add("limit", limit.toString());
        // busy_show_feed_fetch.setVisibility(View.VISIBLE);
        getFeedList(params);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void getFeedList(RequestParams params) {
        Logger.log(Logger.NEWS_FEED_FETCH_START);
        AsyncHttpClient client = new AsyncHttpClient();
        User user = User.getLoggedInUser();
        // response

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<StoryBucket> t_feed_buckets = new ArrayList<>();
                try {
                    JSONArray all_buckets_data = (JSONArray) response.getJSONArray("data");
                    if (all_buckets_data.length() > 0) {
                        for (int i = 0; i < all_buckets_data.length(); i++) {
                            JSONArray current_all_stories = all_buckets_data.getJSONArray(i);
                            StoryBucket current_element = null;
                            ArrayList<StoryElement> story_elements = new ArrayList<>();
                            for (int j = 0; j < current_all_stories.length(); j++) {
                                JSONObject obj = current_all_stories.getJSONObject(j);
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
                                Integer width = obj.getInt("banner_image_width");
                                Integer height = obj.getInt("banner_image_height");
                                Boolean is_system_user = obj.getBoolean("is_system_user");
                                String learn_more_url = obj.getString("learn_more_url");
                                JSONArray tags = obj.getJSONArray("tags");
                                Boolean is_user_story = obj.getBoolean("is_user_story");

                                ArrayList<String> tags_list = new ArrayList<>();



                                RequestOptions options = new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .centerCrop();

                                if (j == 0) {
                                    ArrayList<StoryElement> current_bucket_elements  = new ArrayList<>();
                                    current_element = new StoryBucket(
                                            current_bucket_elements,
                                            user_name,
                                            profile_image_url,
                                            banner_image_url_high,
                                            width,
                                            height
                                    );
                                    current_element.setUserStory(is_user_story);
                                    t_feed_buckets.add(current_element);
                                        Glide.with(getContext())
                                                .load(banner_image_url_high)
                                                .apply(options)
                                                .preload();
                                        }

                                StoryElement new_current_element = new StoryElement(
                                        post_id,
                                        banner_image_url_low,
                                        banner_image_url_high,
                                        width,
                                        height,
                                        message_text,
                                        learn_more_url
                                );

                                current_element.addStoryElement(new_current_element);
                            }
                        }

                        feed_buckets.clear();
                        feed_buckets.addAll(t_feed_buckets);
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
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.RES, res);
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.NEWS_FEED_FETCH_FAILED, log_data);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                if (obj != null) {
                    log_data.put(Logger.JSON, obj.toString());
                }
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.NEWS_FEED_FETCH_FAILED, log_data);
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.get(App.getBaseURL() + "newsfeed", params, response_json);
    }

    @Override
    public void onClickStoryView(StoryBucket current_element) {
        // start the story fragment;
        StoryViewFragment sf =new StoryViewFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("stories", current_element.getStoryElements());
        sf.setArguments(args);
        loadFragment(sf);
    }

    private void startCamera2(){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_holder, Camera2Fragment.newInstance(), getString(R.string.fragment_camera2));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void createStory(Long story_id) {
        Logger.log(Logger.STORY_CREATE_START);
        android.util.Log.d("debug_data", "upload started...");
        User user = User.getLoggedInUser();
        final AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            String path = "/storage/emulated/0/Android/data/com.neartag.in/files/";
            File imgfile = new File(path + "/temp_image.jpg");
            params.put("file", imgfile);
            params.put("story_id", story_id);

        } catch(FileNotFoundException fexception) {
            fexception.printStackTrace();
        }
        JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Integer width = 0, height  = 0;
                try {
                    JSONObject data = response.getJSONObject("data");
                    String banner_image_url_low = data.getString("banner_image_url_low");
                    String banner_image_url_high = data.getString("banner_image_url_high");
                    Integer banner_image_width = data.getInt("banner_image_width");
                    Integer banner_image_height = data.getInt("banner_image_height");
                    Long story_id_from_serv = data.getLong("story_id");

                    // find the story and update the url and
                    Integer user_bucket_index = null;
                    for(int i  = 0; i < feed_buckets.size(); i++) {
                        if (feed_buckets.get(i).getUserStory()) {
                            user_bucket_index = i;
                            break;
                        }
                    }

                    if (user_bucket_index != null) {
                        feed_buckets.get(user_bucket_index).setBucketCenterImageUrl(banner_image_url_high);
                        feed_buckets.get(user_bucket_index).setCenterImageWidth(banner_image_width);
                        feed_buckets.get(user_bucket_index).setCenterImageHeight(banner_image_height);
                        feed_buckets.get(user_bucket_index).setLocalFile(false);


                        // find the index of story and update the content

                        Integer story_index = null;
                        for(int i = 0 ; i < feed_buckets.get(user_bucket_index).getStoryElements().size(); i++) {
                            if (feed_buckets.get(user_bucket_index).getStoryElements().get(i).getStoryElementId() == story_id_from_serv) {
                                feed_buckets.get(user_bucket_index).getStoryElements().get(i).setBannerImageURLHigh(banner_image_url_high);
                                feed_buckets.get(user_bucket_index).getStoryElements().get(i).setBannerImageURLLow(banner_image_url_low);
                                feed_buckets.get(user_bucket_index).getStoryElements().get(i).setHeight(banner_image_height);
                                feed_buckets.get(user_bucket_index).getStoryElements().get(i).setWidth(banner_image_width);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Logger.log(Logger.STORY_CREATE_SUCCESS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.RES, res);
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.STORY_CREATE_FAILED, log_data);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                if (obj != null) {
                    log_data.put(Logger.JSON, obj.toString());
                }
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.STORY_CREATE_FAILED, log_data);
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "story/create", params, jrep);
    }

    private void appendNewStory() {
        Date date= new Date();
        long time = date.getTime();

        // find the feed_buckets where IsUserStory = true;
        Integer user_bucket_index = null;
        for(int i  = 0; i < feed_buckets.size(); i++) {
            if (feed_buckets.get(i).getUserStory()) {
                user_bucket_index = i;
                break;
            }
        }


        String path = "/storage/emulated/0/Android/data/com.neartag.in/files/";
        File imgfile = new File(path + "/temp_image.jpg");
        String uri = imgfile.getAbsolutePath();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;


        User user = User.getLoggedInUser();
        if (user_bucket_index == null) {
            // create a new one
            ArrayList<StoryElement> all_elements = new ArrayList<>();
            StoryBucket user_bucket = new StoryBucket(
                    all_elements,
                    user.Name,
                    user.ProfilePicURL,
                    uri,
                    imageWidth,
                    imageHeight
            );
            user_bucket.setUserStory(true);
            user_bucket.setLocalFile(true);
            user_bucket.setStoryBucketId(time);
            feed_buckets.add(0, user_bucket);
            user_bucket_index = 0;
        } else {
            feed_buckets.get(user_bucket_index).setUserStory(true);
            feed_buckets.get(user_bucket_index).setLocalFile(true);
            feed_buckets.get(user_bucket_index).setBucketCenterImageUrl(uri);
            feed_buckets.get(user_bucket_index).setCenterImageWidth(imageWidth);
            feed_buckets.get(user_bucket_index).setCenterImageHeight(imageHeight);
            feed_buckets.get(user_bucket_index).setStoryBucketId(time);
        }

        StoryElement se = new StoryElement(
                0,
                uri,
                uri,
                imageWidth,
                imageHeight, "",
                ""
        );
        se.setLocalFile(true);
        se.setStoryElementId(time);
        feed_buckets.get(user_bucket_index).addStoryElementFront(se);

        adapter.notifyDataSetChanged();
        createStory(time);
    }
}
