package com.neartag.in;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neartag.in.R;
import com.neartag.in.models.User;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/26/18.
 */

public class UserProfileFragment extends Fragment {


    CircleImageView profile_img;
    TextView username;
    TextView display_name, description;
    User user;
    TextView unfollow, postsCount, followersCount, followingCount;
    TextView follow;
    View fragmentView;

    BroadcastReceiver broadCastNewMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            User user = User.getLoggedInUser();

            Picasso.with(getContext()).load(user.ProfilePicURL).

                    into(
                            profile_img,
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

            if (!StringUtils.isEmpty(user.Name)) {
                username.setText(user.Name);
                display_name.setText(user.Name);
                description.setText(user.Description);
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        IntentFilter new_post = new IntentFilter("user_profile_update");
        getActivity().registerReceiver(broadCastNewMessage, new_post);

        ImageView backArrow = fragmentView.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment();
            }
        });

        username = fragmentView.findViewById(R.id.username);
        display_name = fragmentView.findViewById(R.id.display_name);
        profile_img = fragmentView.findViewById(R.id.profile_photo);
        unfollow =  fragmentView.findViewById(R.id.unfollow);
        follow = fragmentView.findViewById(R.id.follow);
        description = fragmentView.findViewById(R.id.description);

        postsCount = fragmentView.findViewById(R.id.tvPosts);
        followersCount = fragmentView.findViewById(R.id.tvFollowers);
        followingCount = fragmentView.findViewById(R.id.tvFollowing);

        user = User.getLoggedInUser();
        if (getArguments() != null) {
            final Integer post_creator_user_id = getArguments().getInt("user_id");
            if(post_creator_user_id != user.UserID) {
                String image_url = getArguments().getString("profile_image_url");
                String user_name = getArguments().getString("user_name");

                follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mutateFollowUnfollow(post_creator_user_id, true);
                    }
                });


                unfollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mutateFollowUnfollow(post_creator_user_id, false);
                    }
                });


                setupOthers(fragmentView, post_creator_user_id, image_url, user_name);
                return fragmentView;
            }
        }


        setupCurrent(fragmentView, user.UserID);
        return fragmentView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        getActivity().unregisterReceiver(broadCastNewMessage);
    }


    private void setupOthers(View view, Integer user_id, String profile_image_url, String user_name) {

        if (!StringUtils.isEmpty(profile_image_url)) {
            Picasso.with(getContext()).load(profile_image_url).
                    resize(120, 120).
                    into(
                            profile_img,
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

        username.setText(user_name);
        display_name.setText(user_name);

        fetchUserProfile(view, user_id, false);
    }

    private void fetchUserProfile(final View view, Integer user_id, final Boolean is_current_user) {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            //busy_show_feed_fetch.setVisibility(View.VISIBLE);
            params.put("user_id", user_id);
            User user = User.getLoggedInUser();
            // response

            JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        JSONObject data = response.getJSONObject("data");
                        Boolean is_followed = data.getBoolean("is_followed");
                        Integer posts_count = data.getInt("posts_count");
                        Integer followers_count = data.getInt("followers_count");
                        Integer following_count = data.getInt("following_count");

                        if (!is_current_user) {
                            if (is_followed) {
                                unfollow.setVisibility(View.VISIBLE);
                            } else {

                                follow.setVisibility(View.VISIBLE);
                            }

                        }

                        postsCount.setText(posts_count.toString());
                        followersCount.setText(followers_count.toString());
                        followingCount.setText(following_count.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                    com.activeandroid.util.Log.d("debug_data", " " + statusCode);
                    if (statusCode == 401) {
                    }
                }
            };
            // request
            client.addHeader("Accept", "application/json");
            client.addHeader("Authorization", "Bearer " + user.AccessToken);
            client.get(App.getBaseURL() + "newsfeed/userprofile", params, response_json);
    }

    private  void setupCurrent(View view, Integer user_id) {
        if (!StringUtils.isEmpty(user.Name)) {
            username.setText(user.Name);
            TextView display_name = view.findViewById(R.id.display_name);
            display_name.setText(user.Name);
        }

        if (!StringUtils.isEmpty(user.ProfilePicURL)) {
            Picasso.with(getContext()).load(user.ProfilePicURL).
                    resize(120, 120).
                    into(
                            profile_img,
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


        description.setText(user.Description);

        TextView editProfile = view.findViewById(R.id.textEditProfile);
        editProfile.setVisibility(View.VISIBLE);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpFragment(new AccountEditFragment());
            }
        });
        fetchUserProfile(view, user_id, true);
    }

    private void loadFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void setUpFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void mutateFollowUnfollow(Integer user_id, final Boolean is_follow) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        //busy_show_feed_fetch.setVisibility(View.VISIBLE);
        params.put("user_id", user_id);

        // is follow = true meanng user clicked on follow
        // is follow  = false meaning user clicked on unfollow
        params.put("status", is_follow? "FOLLOW": "UNFOLLOW");

        if (is_follow) {
            unfollow.setVisibility(View.VISIBLE);
            follow.setVisibility(View.GONE);
        } else {
            follow.setVisibility(View.VISIBLE);
            unfollow.setVisibility(View.GONE);
        }

        User user = User.getLoggedInUser();

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject data = response.getJSONObject("data");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                com.activeandroid.util.Log.d("debug_data", " " + statusCode);
                if (statusCode == 401) {
                }
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "useractivity/follow_unfollow", params, response_json);
    }
}
