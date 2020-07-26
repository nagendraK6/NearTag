package com.relylabs.instahelo.newsfeed;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.relylabs.instahelo.R;
import com.relylabs.instahelo.models.StoryBucket;
import com.relylabs.instahelo.models.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/6/18.
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class StoryFeedAdapter extends RecyclerView.Adapter<StoryFeedAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<StoryBucket> story_buckets;
    // Store the context for easy access
    private Context mContext;

    private AppCompatActivity activity;
    View feed_view;
    User user;
    // Pass in the contact array into the constructor
    public StoryFeedAdapter(AppCompatActivity activity, Context context, List<StoryBucket> all_feed_elements) {
        story_buckets = all_feed_elements;
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

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(feed_view);


        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final StoryFeedAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        final StoryBucket current_element = story_buckets.get(position);
        viewHolder.name.setText(current_element.getCreatorName());
        Glide.with(getContext()).load(this.user.ProfilePicURL).into(viewHolder.story_creator_profile);

        if (current_element.getCenterImageHeight() > current_element.getCenterImageWidth()) {
            if (current_element.getLocalFile()) {
                viewHolder.bannerImage.setAlpha(0.4f);
                Picasso.with(getContext())
                        .load(new File(current_element.getBucketCenterImageUrl()))
                        .resize(current_element.getCenterImageWidth(), current_element.getCenterImageWidth())
                        .centerCrop()

                        .into(viewHolder.bannerImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                viewHolder.itemView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            } else {
                viewHolder.bannerImage.setAlpha(1.0f);
                Picasso.with(getContext())
                        .load(current_element.getBucketCenterImageUrl())
                        .resize(current_element.getCenterImageWidth(), current_element.getCenterImageWidth())
                        .centerCrop()
                        .into(viewHolder.bannerImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                viewHolder.itemView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
        } else {
            Picasso.with(getContext())
                    .load(current_element.getBucketCenterImageUrl())
                    .into(viewHolder.bannerImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            viewHolder.itemView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError() {

                        }
                    });
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
        return story_buckets.size();
    }


    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }

    public interface StoryViewListener {
        void onClickStoryView(StoryBucket current_element);
    }

    public void setStoryViewListener(StoryFeedAdapter.StoryViewListener listener) {
        this.sListener = listener;
    }

    StoryViewListener sListener;
}
