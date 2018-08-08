package com.relylabs.around;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Created by nagendra on 8/6/18.
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class NewsFeedAdapter extends
        RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<NewsFeedElement> news_feed_elements;
    // Store the context for easy access
    private Context mContext;
    View feed_view;
    // Pass in the contact array into the constructor
    public NewsFeedAdapter(Context context, List<NewsFeedElement> all_feed_elements) {
        news_feed_elements = all_feed_elements;
        mContext = context;
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

        public ProgressBar busy;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tag = (TextView) itemView.findViewById(R.id.tag);
            banngerImage = (ImageView) itemView.findViewById(R.id.banner_image);
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
    public void onBindViewHolder(final NewsFeedAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final NewsFeedElement current_element = news_feed_elements.get(position);
        // Set item views based on your views and data model
        final TextView tag = viewHolder.tag;
        tag.setText(current_element.getTag());
        // Set item views based on your views and data model


        final ImageView banner_image = viewHolder.banngerImage;
        Picasso.with(getContext()).load(current_element.getBanngerImageURL()).into(
                banner_image,
                new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        tag.setVisibility(View.VISIBLE);
                        viewHolder.itemView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        //do smth when there is picture loading error
                    }
                }
        );
    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return news_feed_elements.size();
    }
}