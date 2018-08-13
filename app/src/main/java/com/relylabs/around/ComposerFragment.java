package com.relylabs.around;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 8/12/18.
 */

public class ComposerFragment  extends Fragment {

    ArrayList<ComposerImageElement> composer_elements;
    ComposerImageAdapter adapter;
    RecyclerView composer_image_list;
    ComposerImageAdapter composer_listener;
    ImageView raw_img;
    EditText post_editor;
    TextView select_image;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.composer_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        raw_img = (ImageView) view.findViewById(R.id.theme_image);

        Bitmap bitmap =  (Bitmap) getArguments().getParcelable("bitmap");
        raw_img.setImageBitmap(bitmap);

        post_editor = (EditText) view.findViewById(R.id.user_post_text);
        composer_image_list = (RecyclerView) view.findViewById(R.id.composer_images_view);
        composer_elements = new ArrayList<ComposerImageElement>();
        select_image = view.findViewById(R.id.image_select);
        adapter = new ComposerImageAdapter(getActivity(), composer_elements, new CallBackFromComposer() {
            @Override
            public void onElementClick(String s) {
                Log.d("debug_data", "element clicked");
                Picasso.with(getContext()).load(s).into(raw_img);
                post_editor.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                post_editor.setGravity(Gravity.CENTER);
            }
        });

        // Attach the adapter to the recyclerview to populate items
        composer_image_list.setAdapter(adapter);
        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new GalaryImageSelectFragment());
            }
        });
        getStandardViewList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

    private void getStandardViewList() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        // response

        JsonHttpResponseHandler response_json = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                ArrayList<ComposerImageElement> feed_elements = new ArrayList<>();
                try {
                    JSONArray all_contests = (JSONArray) response.getJSONArray("data");
                    for (int i =0; i < all_contests.length(); i++) {
                        JSONObject obj = all_contests.getJSONObject(i);

                        String thumbnail_url = (String) obj.getString("thumbnail_url");
                        String canvas_url = (String) obj.getString("canvas_url");


                        ComposerImageElement current_element = new ComposerImageElement(
                                thumbnail_url,
                                canvas_url
                        );

                        feed_elements.add(current_element);
                    }

                    composer_elements.addAll(feed_elements);
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
                com.activeandroid.util.Log.d("debug_data", " " + statusCode);
                if (statusCode == 401) {
                }
            }
        };
        // request
        client.addHeader("Accept", "application/json");
        //client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.get(App.getBaseURL() + "newsfeed/composer", params, response_json);
    }

    private void loadFragment(Fragment fragment_to_start) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_holder, fragment_to_start);
            ft.commitAllowingStateLoss();
    }
}

