package com.neartag.in.composer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.neartag.in.App;
import com.neartag.in.R;
import com.neartag.in.TagSearchFragment;
import com.neartag.in.Utils.Helper;
import com.neartag.in.sharing.ImageSharingView;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by nagendra on 8/21/18.
 */

public class RecyclerGalaryFragment extends Fragment  implements MyRecyclerViewAdapter.ItemClickListener {
    public static final int REQUEST_FOR_TAKE_PHOTO = 9;

    RecyclerView recyclerView;
    MyRecyclerViewAdapter adapter;
    String mSelectedImage;
    ArrayList<String> all_images;
    ImageView preview_image;
    View fragment_view;
    String ref = "";
    TextView tvNext;
    private Spinner directorySpinner;
    ArrayList<String> directoryNames = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.recycler_galary_fragment, container, false);
        return fragment_view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView closeButton = view.findViewById(R.id.ivCloseShare);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        directorySpinner = view.findViewById(R.id.spinnerDirectory);

        processImageSelection();

        if (getArguments() != null) {
            ref =  getArguments()
                    .getString("ref");
        }

        tvNext = fragment_view.findViewById(R.id.tvNext);
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
    }

    private void setAlbumNames() {
        directoryNames = Helper.getDirectoryNames(getContext());
        Integer wIndex = directoryNames.indexOf("WhatsApp Images");
        if (wIndex >= 0 && wIndex < directoryNames.size()) {
            Collections.swap(directoryNames, 0, wIndex);
        }


        ArrayAdapter<String> dr_adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, directoryNames);
        dr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(dr_adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                galleryIntent(directoryNames.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Picasso.with(getContext()).load(new File(all_images.get(position)))
                .fit()
                .placeholder(R.color.white)
                .noFade()
                .centerCrop()
                .into(preview_image);

        mSelectedImage = all_images.get(position);
    }


    public void processImageSelection() {
        if (getActivity() != null) {
            findPermissionsAndSelectImage();
        }
    }

    public void findPermissionsAndSelectImage() {
        boolean result = checkPermission(getActivity());
        if (result) {
            setAlbumNames();
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
            setAlbumNames();

        } else {
            Toast.makeText(getActivity(), "Permission is needed to capture image for the profile", Toast.LENGTH_LONG).show();
        }
    }

    private void galleryIntent(String directoName) {
        all_images = Helper.getAllShownImagesPath(directoName, getContext());

        // set up the RecyclerView
        recyclerView = fragment_view.findViewById(R.id.all_images);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new MyRecyclerViewAdapter(getContext(), all_images);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        tvNext = fragment_view.findViewById(R.id.tvNext);
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle data_bundle = new Bundle();
                data_bundle.putString(getString(R.string.user_selected_image), mSelectedImage);

                if (ref.equals("composer")) {
                    Fragment frg = new TagSearchFragment();
                    frg.setArguments(data_bundle);
                    loadFragment(frg);
                } else {
                    // close current fragment and pass data to previous fragment
                    Intent intent=new Intent("new_image");
                    intent.putExtras(data_bundle);
                    Fragment frg = new ImageSharingView();
                    frg.setArguments(data_bundle);
                    loadFragment(frg);
                }
            }
        });

        mSelectedImage = all_images.get(0);
        preview_image = fragment_view.findViewById(R.id.preview_image);
        Picasso.with(getContext()).load(new File(mSelectedImage))
                .fit()
                .placeholder(R.color.white)
                .centerCrop()
                .into(preview_image, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        //do smth when picture is loaded successfully
                        tvNext.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onError() {
                        //do smth when there is picture loading error
                        tvNext.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
