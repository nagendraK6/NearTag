package com.relylabs.around;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nagendra on 8/12/18.
 */

public class ImageAdapter extends BaseAdapter {

    /** The context. */
    private Context context;

    ArrayList<String> images;
    CallBackFromComposer callback_on_image_click;
    /**
     * Instantiates a new image adapter.
     *
     * @param localContext
     *            the local context
     */
    public ImageAdapter(Context localContext, CallBackFromComposer m) {
        context = localContext;
        images = getAllShownImagesPath();
        callback_on_image_click = m;
    }

    public ImageAdapter(Context localContext) {
        context = localContext;
        images = getAllShownImagesPath();
        callback_on_image_click = null;
    }

    public int getCount() {
        return images.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        ImageView picturesView;
        Log.d("debug_data", "Pcitures getview called");
        if (convertView == null) {
            picturesView = new ImageView(context);
            picturesView.setScaleType(ImageView.ScaleType.FIT_XY);
            picturesView
                    .setLayoutParams(new GridView.LayoutParams(270, 270));

        } else {
            picturesView = (ImageView) convertView;
        }

        Log.d("debug_data", "Pcitures view called");
        Picasso.with(context).load(new File(images.get(position))).into(picturesView);
        picturesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callback_on_image_click != null) {
                    callback_on_image_click.onElementClick(images.get(position));
                }
            }
        });

        return picturesView;
    }

    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = context.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }
}
