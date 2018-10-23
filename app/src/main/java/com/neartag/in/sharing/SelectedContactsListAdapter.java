package com.neartag.in.sharing;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neartag.in.R;
import com.neartag.in.composer.MyRecyclerViewAdapter;
import com.neartag.in.models.Contact;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class SelectedContactsListAdapter extends RecyclerView.Adapter<SelectedContactsListAdapter.ViewHolder> {

    private ArrayList<Contact> mData;
    private LayoutInflater mInflater;
    private ContactsListAdapter.ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    SelectedContactsListAdapter(Context context, ArrayList<Contact> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    public SelectedContactsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.snippet_contacts_selected_view, parent, false);
        return new SelectedContactsListAdapter.ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(final SelectedContactsListAdapter.ViewHolder holder, final int position) {
        final Contact c = mData.get(position);
        holder.name.setText(c.getName());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        ImageView contact_selected;
        Boolean is_selected;
        View v;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            contact_selected = itemView.findViewById(R.id.contact_selected);
            v = itemView;
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Contact getItem(int id) {
        return mData.get(id);
    }
}