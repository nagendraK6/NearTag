package com.relylabs.around;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/6/18.
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class NewsFeedAdapter extends
        RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

    private  VisibilityTracker mVisibilityTracker;
    private final WeakHashMap<View, Integer> mViewPositionMap = new WeakHashMap<>();

    // Store a member variable for the contacts
    private List<NewsFeedElement> news_feed_elements;
    // Store the context for easy access
    private Context mContext;
    View feed_view;
    // Pass in the contact array into the constructor
    public NewsFeedAdapter(Activity activity, Context context, List<NewsFeedElement> all_feed_elements) {
        mVisibilityTracker = new VisibilityTracker(activity);
        mVisibilityTracker.setVisibilityTrackerListener(new VisibilityTracker.VisibilityTrackerListener() {
            @Override
            public void onVisibilityChanged(List<View> visibleViews, List<View> invisibleViews) {
                handleVisibleViews(visibleViews);
            }
        });

        news_feed_elements = all_feed_elements;
        mContext = context;
    }

    private void handleVisibleViews(List<View> visibleViews) {
        Log.d(NewsFeedAdapter.class.getSimpleName(), "Currently visible views \n");
        for (View v : visibleViews) {
            Integer viewPosition = mViewPositionMap.get(v);
            Log.d("debug_data", "VP " + viewPosition.toString());
        }
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tag;
        public ImageView banngerImage;
        public CircleImageView profilePicURL;
        public ProgressBar uploadingFile;
        public TextView upload_in_progress_text;
        public TextView userPostText;

        public ProgressBar busy;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tag = (TextView) itemView.findViewById(R.id.tag);
            banngerImage = (ImageView) itemView.findViewById(R.id.banner_image);
            profilePicURL = (CircleImageView) itemView.findViewById(R.id.user_profile_image);
            uploadingFile = itemView.findViewById(R.id.progress);
            upload_in_progress_text = itemView.findViewById(R.id.upload_in_progress_text);
            userPostText = itemView.findViewById(R.id.user_post_text);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public NewsFeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        feed_view = inflater.inflate(R.layout.news_item, parent, false);
        //feed_view.setBackgroundColor(Color.RED);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(feed_view);


        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final NewsFeedAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        final NewsFeedElement current_element = news_feed_elements.get(position);
        // Set item views based on your views and data model
        final TextView tag = viewHolder.tag;
        tag.setText(current_element.getTag());
        // Set item views based on your views and data model

        ImageView profile = viewHolder.profilePicURL;
        if (!current_element.getProfileImageURL().equals("")) {
            Picasso.with(getContext()).load(current_element.getProfileImageURL()).
                    resize(50, 50).
                    into(
                    profile,
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

        final ImageView banner_image = viewHolder.banngerImage;
        viewHolder.userPostText.setText(current_element.getUserPostText());

        if (current_element.getBanngerImageURL() != "") {
            Picasso.with(getContext()).load(current_element.getBanngerImageURL())
                    .into(
                            banner_image,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    viewHolder.itemView.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );
        }

        if (current_element.getGalleryImageFile() != "") {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            File piccasso_file = new File(current_element.getGalleryImageFile());
            Picasso.with(getContext()).load(piccasso_file).
                    into(
                            banner_image,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d("debug_data", "loading from local file");
                                }


                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );
            if(current_element.getHasPublished()) {
                viewHolder.banngerImage.setAlpha((float) 1);
                viewHolder.profilePicURL.setAlpha((float) 1);
                viewHolder.tag.setAlpha((float) 1);
                viewHolder.uploadingFile.setVisibility(View.GONE);
                viewHolder.upload_in_progress_text.setVisibility(View.GONE);
            }
        }

        if (current_element.getHasPublished()) {
            viewHolder.upload_in_progress_text.setVisibility(View.GONE);
        } else {
            viewHolder.itemView.setVisibility(View.VISIBLE);
            viewHolder.upload_in_progress_text.setVisibility(View.VISIBLE);
            viewHolder.banngerImage.setAlpha((float) 0.5);
            viewHolder.profilePicURL.setAlpha((float) 0.5);
            viewHolder.tag.setAlpha((float) 0.5);
            viewHolder.uploadingFile.setVisibility(View.VISIBLE);
        }

        mViewPositionMap.put(viewHolder.itemView, position);
        mVisibilityTracker.addView(viewHolder.itemView, 50);
    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return news_feed_elements.size();
    }
}