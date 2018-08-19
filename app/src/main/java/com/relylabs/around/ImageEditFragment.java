package com.relylabs.around;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import okhttp3.internal.cache.DiskLruCache;

/**
 * Created by nagendra on 8/9/18.
 */

public class ImageEditFragment extends Fragment {


    ArrayList<ComposerImageElement> composer_elements;
    ComposerImageAdapter adapter;
    RecyclerView composer_image_list;
    ComposerImageAdapter composer_listener;
    ImageView raw_img;

    View fragment_view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragment_edit_photo, container, false);
        raw_img = fragment_view.findViewById(R.id.user_photo);
        return  fragment_view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        //File f = new File("storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20180812-WA0000.jpg");

        //Picasso.with(getContext()).load(f).into(raw_img);

        Picasso.with(getContext()).load("https://www.rely.ai/Image/1503784090.jpg").into(raw_img);
        Button btn = view.findViewById(R.id.create_img);
        final ImageView created_one = view.findViewById(R.id.created_one);

        final ImageAndTextView v = view.findViewById(R.id.image_section);
        Log.d("debug_data", "I am called ");
        if (v == null) {
            Log.d("debug_data", "I am null");
        } else {
            Log.d("debug_data", "I am NOT null");
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                v.setDrawingCacheEnabled(true);

// this is the important code :)
// Without it the view will have a dimension of 0,0 and the bitmap will be null
                v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

                v.buildDrawingCache(true);

                Bitmap bitmap = v.getDrawingCache();
                if (bitmap != null) {
                    created_one.setImageBitmap(bitmap.copy(bitmap.getConfig(), false));
                }
                v.setDrawingCacheEnabled(false);
            }
        });


        composer_image_list = (RecyclerView) view.findViewById(R.id.composer_images_view);
        // Initialize cont acts
        composer_elements = new ArrayList<ComposerImageElement>();
        // Create adapter passing in the sample user data
        /*adapter = new ComposerImageAdapter(getActivity(), composer_elements, new CallBackFromComposer() {
            @Override
            public void onElementClick(String s) {
                Log.d("debug_data", "element clicked");
                Picasso.with(getContext()).load(s).into(raw_img);
            }
        });*/

        // Attach the adapter to the recyclerview to populate items
        composer_image_list.setAdapter(adapter);
        // Set layout manager to position the items

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
}

