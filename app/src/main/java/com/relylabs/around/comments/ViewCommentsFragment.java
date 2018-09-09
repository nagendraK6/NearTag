package com.relylabs.around.comments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.around.App;
import com.relylabs.around.DeviceUtils;
import com.relylabs.around.PreCachingLayoutManager;
import com.relylabs.around.R;
import com.relylabs.around.models.User;
import com.relylabs.around.models.Comment;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 8/28/18.
 */

    public class ViewCommentsFragment extends Fragment {

    ArrayList<Comment> all_comments_list;
    CommentListAdapter adapter;
    RecyclerView comment_list_view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment();
            }
        });

        // request focus
        final EditText comment = view.findViewById(R.id.comment);
        comment.requestFocus();
        final Integer post_id = getArguments().getInt("post_id");
        ImageView ivPostComment = view.findViewById(R.id.ivPostComment);
        ivPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // logic to post comment

                // add post to the list
                if (!StringUtils.isEmpty(comment.getText().toString())) {
                    addCommentsToList(comment.getText().toString(), post_id);
                    saveCommentOnServer(comment.getText().toString(), post_id);
                }
            }
        });


        comment_list_view = (RecyclerView) view.findViewById(R.id.listView);
        // Initialize cont acts
        all_comments_list = new ArrayList<Comment>();
        // Create adapter passing in the sample user data
        adapter = new CommentListAdapter((AppCompatActivity) getActivity(), getActivity(), all_comments_list);

        // Attach the adapter to the recyclerview to populate items
        comment_list_view.setAdapter(adapter);

        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        comment_list_view.setLayoutManager(layoutManager);

        readComments(post_id);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        comment_list_view.setAdapter(null);
        comment_list_view.setLayoutManager(null);
        App.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void loadFragment() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    private void addCommentsToList(String comment_text, Integer post_id) {
        User user = User.getLoggedInUser();
        Comment new_comment = new Comment(
                user.UserID,
                user.Name,
                user.ProfilePicURL,
                comment_text,
                ""
        );

        all_comments_list.add(new_comment);
        adapter.notifyDataSetChanged();
    }

    private void saveCommentOnServer(String comment_text, Integer post_id) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        User user = User.getLoggedInUser();
        params.put("post_id", post_id);
        params.put("post_comment_text", comment_text);

        JsonHttpResponseHandler jrep = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // update user profile and broadcast the update
                try {
                    JSONObject data = (JSONObject) response.getJSONObject("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "post/addcomment", params, jrep);
    }

    private  void readComments(Integer post_id) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        User user = User.getLoggedInUser();
        params.put("post_id", post_id);

        JsonHttpResponseHandler jrep = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // update user profile and broadcast the update
                ArrayList<Comment> comments_list = new ArrayList<>();
                try {
                    JSONArray all_comments = response.getJSONArray("data");
                    for (int i = 0; i < all_comments.length(); i++) {
                        JSONObject obj = all_comments.getJSONObject(i);
                        Integer user_id = obj.getInt("user_id");
                        String user_name = obj.getString("user_name");
                        String user_profile_image_url = obj.getString("user_profile_image_url");
                        String comment_text = obj.getString("comment_text");

                        Comment new_comment = new Comment(
                                user_id,
                                user_name,
                                user_profile_image_url,
                                comment_text,
                                ""
                        );
                        comments_list.add(new_comment);
                    }

                    all_comments_list.addAll(comments_list);
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
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.get(App.getBaseURL() + "post/getcomments", params, jrep);
    }
}
