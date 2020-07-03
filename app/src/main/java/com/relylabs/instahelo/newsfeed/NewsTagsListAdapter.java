package com.relylabs.instahelo.newsfeed;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.relylabs.instahelo.R;

import java.util.ArrayList;

public class NewsTagsListAdapter extends RecyclerView.Adapter<NewsTagsListAdapter.ViewHolder> {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private NewsTagsListAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public NewsTagsListAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the cell layout from xml when needed
    @Override
    public NewsTagsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.news_tag_list, parent, false);
        return new NewsTagsListAdapter.ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(@NonNull NewsTagsListAdapter.ViewHolder holder, int position) {
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
            tagDisplayView = itemView.findViewById(R.id.recommended_tag_value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onTagClick(mData.get(getAdapterPosition()));
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(NewsTagsListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onTagClick(String tag);
    }
}


