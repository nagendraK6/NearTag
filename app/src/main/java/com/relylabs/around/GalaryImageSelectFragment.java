package com.relylabs.around;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by nagendra on 8/12/18.
 */

public class GalaryImageSelectFragment extends Fragment {
    public static final int REQUEST_FOR_TAKE_PHOTO = 9;
    View fragment_view;
    private String mSelectedImage;
    GridView gallery = null;
    TextView tvNext = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.galary_view_fragment, container, false);
        return fragment_view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        directorySpinner = view.findViewById(R.id.spinnerDirectory);
        processImageSelection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        if (gallery != null) {
            gallery.setOnItemClickListener(null);
            gallery.setAdapter(null);
        }

        if (tvNext != null) {
            tvNext.setOnClickListener(null);
        }

        Glide.get(getContext()).clearMemory();
        System.gc();

        Log.d("debug_data", "Fragment destroyed");
    }

    public void processImageSelection() {
        if (getActivity() != null) {
            findPermissionsAndSelectImage();
        }
    }

    public void findPermissionsAndSelectImage() {
        boolean result = checkPermission(getActivity());
        if (result) {
            galleryIntent();
        }
    }


    public boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FOR_TAKE_PHOTO);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FOR_TAKE_PHOTO);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
        super.onRequestPermissionsResult(RC, per, PResult);
        if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
            galleryIntent();

        } else {
            Toast.makeText(getActivity(), "Permission is needed to capture image for the profile", Toast.LENGTH_LONG).show();
        }
    }

    private void galleryIntent() {
         gallery = fragment_view.findViewById(R.id.all_images);

        ImageAdapter galaryAdapter = new ImageAdapter(Glide.with(this), getContext(), R.layout.layout_grid_imageview);
        final ArrayList<String> all_images = galaryAdapter.getItems();

        gallery.setAdapter(galaryAdapter);
        init(galaryAdapter.getAlbums());

        final ImageView view_preview = fragment_view.findViewById(R.id.preview_image);

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Glide.with(GalaryImageSelectFragment.this).load(new File(all_images.get(i)))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(view_preview);

                mSelectedImage = all_images.get(i);

            }
        });

        Glide.with(this).load(new File(all_images.get(0)))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
              .into(view_preview);

        mSelectedImage = all_images.get(0);

        TextView tvNext = fragment_view.findViewById(R.id.tvNext);
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data_bundle = new Bundle();
                data_bundle.putString(getString(R.string.user_selected_image), mSelectedImage);
                Fragment frg = new TagSearchFragment();
                frg.setArguments(data_bundle);
                //getActivity().finish();
                loadFragment(frg);
            }
        });

    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commit();
    }

    private void init(ArrayList<String> directoryNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, directoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
    }

    private Spinner directorySpinner;
}