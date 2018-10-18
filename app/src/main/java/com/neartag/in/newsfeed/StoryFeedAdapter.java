package com.neartag.in.newsfeed;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neartag.in.App;
import com.neartag.in.DeviceUtils;
import com.neartag.in.NewsFeedAdapter;
import com.neartag.in.PreCachingLayoutManager;
import com.neartag.in.R;
import com.neartag.in.UserProfileFragment;
import com.neartag.in.Utils.Helper;
import com.neartag.in.Utils.SquareImageView;
import com.neartag.in.Utils.TimeAgo;
import com.neartag.in.VisibilityTracker;
import com.neartag.in.comments.ViewCommentsFragment;
import com.neartag.in.composer.RecommendedTagsListAdapter;
import com.neartag.in.models.NewsFeedElement;
import com.neartag.in.models.User;
import com.neartag.in.newsfeed.NewsTagsListAdapter;
import com.neartag.in.webview.WebviewFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/6/18.
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class StoryFeedAdapter extends RecyclerView.Adapter<StoryFeedAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<NewsFeedElement> news_feed_elements;
    // Store the context for easy access
    private Context mContext;

    private AppCompatActivity activity;
    View feed_view;
    User user;
    // Pass in the contact array into the constructor
    public StoryFeedAdapter(AppCompatActivity activity, Context context, List<NewsFeedElement> all_feed_elements) {
        news_feed_elements = all_feed_elements;
        mContext = context;
        this.activity = activity;
        this.user = User.getLoggedInUser();
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
        private TextView name;
        private ImageView bannerImage;
        private CircleImageView story_creator_profile;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            bannerImage = itemView.findViewById(R.id.center_image);
            name = itemView.findViewById(R.id.story_creator_name);
            story_creator_profile = itemView.findViewById(R.id.story_creator);

        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public StoryFeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        feed_view = inflater.inflate(R.layout.story_card_view, parent, false);
        //feed_view.setBackgroundColor(Color.RED);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(feed_view);


        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final StoryFeedAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        final NewsFeedElement current_element = news_feed_elements.get(position);
        // Set item views based on your views and data model
        // final TextView tag = viewHolder.tag;
        // tag.setText(current_element.getTag());
        // Set item views based on your views and data model
        viewHolder.name.setText(current_element.getUserName());
        Picasso.with(getContext()).load(this.user.ProfilePicURL).into(viewHolder.story_creator_profile);
        Integer height = current_element.getHeight();
        if (current_element.getWidth() != 0 && current_element.getHeight() != 0) {
            if (current_element.getHeight() / current_element.getWidth() > 1.1) {
                //resuze
                Double new_ht = current_element.getWidth() * 1.0;
                height = new_ht.intValue();
            }
        }

        if (current_element.getHeight() > current_element.getWidth()) {
            Picasso.with(getContext())
                    .load(current_element.bannerImageURLHigh)
                    .resize(current_element.getWidth(), current_element.getWidth())
                    .centerCrop()
                    .into(viewHolder.bannerImage);
        } else {
            Picasso.with(getContext())
                    .load(current_element.bannerImageURLHigh)
                    .into(viewHolder.bannerImage);
        }

        viewHolder.bannerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sListener != null) {
                    sListener.onClickStoryView(current_element);
                }
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return news_feed_elements.size();
    }


    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }

    public interface StoryViewListener {
        void onClickStoryView( NewsFeedElement current_element);
    }

    public void setStoryViewListener(StoryFeedAdapter.StoryViewListener listener) {
        this.sListener = listener;
    }

    StoryViewListener sListener;
}
