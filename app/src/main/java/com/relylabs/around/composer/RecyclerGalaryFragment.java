package com.relylabs.around.composer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.relylabs.around.App;
import com.relylabs.around.R;
import com.relylabs.around.TagSearchFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nagendra on 8/21/18.
 */

public class RecyclerGalaryFragment extends Fragment  implements MyRecyclerViewAdapter.ItemClickListener {

    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;
    String mSelectedImage;
    ArrayList<String> all_images;
    ImageView preview_image;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_galary_fragment, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        all_images =  getAllShownImagesPath();

        // set up the RecyclerView
        recyclerView = (RecyclerView) view.findViewById(R.id.all_images);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new MyRecyclerViewAdapter(getContext(), all_images);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        TextView tvNext = view.findViewById(R.id.tvNext);
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data_bundle = new Bundle();
                data_bundle.putString(getString(R.string.user_selected_image), mSelectedImage);
                Fragment frg = new TagSearchFragment();
                frg.setArguments(data_bundle);
                loadFragment(frg);
            }
        });

        mSelectedImage = all_images.get(0);
        preview_image = view.findViewById(R.id.preview_image);
        Picasso.with(getContext()).load(new File(mSelectedImage))
                .fit()
                .centerCrop()
                .into(preview_image);

    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        recyclerView.setAdapter(null);

    }

    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        Context context = getContext();
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();

        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        cursor = context.getContentResolver().query(uri, projection, null,
                null, orderBy);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
            column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        }
        return listOfAllImages;
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d("debug_data", "position is " + position);

        Picasso.with(getContext()).load(new File(all_images.get(position)))
                .fit()
                .centerCrop()
                .into(preview_image);

        mSelectedImage = all_images.get(position);
    }
}
