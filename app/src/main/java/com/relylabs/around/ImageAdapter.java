package com.relylabs.around;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nagendra on 8/12/18.
 */

public class ImageAdapter extends BaseAdapter {

    /** The context. */
    private Context context;

    ArrayList<String> images;
    ArrayList<String> album_names;

    private LayoutInflater mInflater;
    private int layoutResource;
    private final RequestManager glide;

    public ImageAdapter(RequestManager glide, Context localContext, int layoutResource) {
        context = localContext;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        images = getAllShownImagesPath();
        this.layoutResource = layoutResource;
        this.glide = glide;
    }

    private static class ViewHolder{
        ImageView image;
    }

    public int getCount() {
        return images.size();
    }

    public ArrayList<String> getItems() {
        return images;
    }

    public ArrayList<String> getAlbums() {
        return album_names;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView,
                        ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.gridImageView);
            convertView.setTag(holder);


        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(context).load(new File(images.get(position))).resize(270, 270).into(holder.image);

        /*
        glide.load(new File(images.get(position)))
                .override(270, 270)
                .into(holder.image);
*/
        //RequestCreator requestCreator = new RequestCreator();
        //requestCreator.memoryPolicy(MemoryPolicy.NO_CACHE);

        return convertView;
    }

    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        album_names = new ArrayList<String>();

        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            String album_name = cursor.getString(column_index_folder_name);
            if(!album_names.contains(album_name)) {
                album_names.add(album_name);
            }
            Log.d("debug_data", cursor.getString(column_index_folder_name));

        }
        return listOfAllImages;
    }
}
