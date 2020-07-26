package com.relylabs.instahelo.registration;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.relylabs.instahelo.R;

import java.util.ArrayList;


import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by nagendra on 9/12/18.
 */

public class PreferenceListAdapter extends RecyclerView.Adapter<PreferenceListAdapter.ViewHolder> {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    PreferenceListAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    public PreferenceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.preference_item, parent, false);
        return new PreferenceListAdapter.ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(PreferenceListAdapter.ViewHolder holder, int position) {
        holder.preference_option.setText(mData.get(position));
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder  {
        TextView preference_option;
        Boolean selected = false;

        ViewHolder(final View itemView) {
            super(itemView);
            preference_option = itemView.findViewById(R.id.preference_option);
            preference_option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!selected) {
                        preference_option.setBackground(context.getDrawable(R.drawable.circular_tag_selected));
                        preference_option.setTextColor(Color.WHITE);
                        selected = true;
                    } else {
                        preference_option.setBackground(context.getDrawable(R.drawable.circular_tag));
                        preference_option.setTextColor(context.getResources().getColor(R.color.neartagtextcolor));
                        selected = false;
                    }

                    if (mClickListener != null) mClickListener.onItemClick(getAdapterPosition(), selected);
                }
            });
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(PreferenceListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(int position, boolean shouldAdd);
    }
}
