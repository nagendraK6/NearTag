package com.relylabs.around.composer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.relylabs.around.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nagendra on 8/22/18.
 */

public class TagsListAdapter extends RecyclerView.Adapter<TagsListAdapter.ViewHolder> {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private TagsListAdapter.ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public TagsListAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    public TagsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.tag_chip_display, parent, false);
        return new TagsListAdapter.ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(TagsListAdapter.ViewHolder holder, int position) {
        Log.d ("debug_data", "position  " + position);
        holder.tagDisplayView.setText(mData.get(position));
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tagDisplayView;

        ViewHolder(View itemView) {
            super(itemView);
            tagDisplayView = itemView.findViewById(R.id.tag_one);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(TagsListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
