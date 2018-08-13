package com.relylabs.around;

/**
 * Created by nagendra on 8/11/18.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by nagendra on 8/11/18.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/6/18.
 */

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ComposerImageAdapter extends
        RecyclerView.Adapter<ComposerImageAdapter.ViewHolder> {

    // Store a member variable for the contacts
    private List<ComposerImageElement> composer_elements;
    private Context mContext;
    View feed_view;

    CallBackFromComposer composer_listener;

    public ComposerImageAdapter(
            Context context,
            List<ComposerImageElement> raw_composer_elements,
            CallBackFromComposer m
            ) {
        composer_elements = raw_composer_elements;
        mContext = context;
        composer_listener = m;
    }

    private Context getContext() {
        return mContext;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView banngerImage;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            banngerImage =  itemView.findViewById(R.id.bg_image);
        }
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ComposerImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        feed_view = inflater.inflate(R.layout.composer_template_image_elements, parent, false);
        return new ViewHolder(feed_view);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(final ComposerImageAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final ComposerImageElement current_element = composer_elements.get(position);
        // Set item views based on your views and data model


        final ImageView banner_image = viewHolder.banngerImage;
        Picasso.with(getContext()).load(current_element.getThumbnailURL()).into(
                banner_image);
        banner_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url_for_bg = current_element.getCanvasURL();
                composer_listener.onElementClick(url_for_bg);
            }
        });
    }


    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        Log.d("debug_data", "count is " + composer_elements.size());
        return composer_elements.size();
    }
}
