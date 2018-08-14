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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

/**
 * Created by nagendra on 8/12/18.
 */

public class GalaryImageSelectFragment extends Fragment {
    public static final int REQUEST_FOR_TAKE_PHOTO = 9;
    View fragment_view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.galary_view_fragment, container, false);
        return fragment_view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        processImageSelection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

    public void processImageSelection() {
        if(getActivity() != null) {
            findPermissionsAndSelectImage();
        }
    }

    public  void findPermissionsAndSelectImage() {
        boolean result= checkPermission(getActivity());
        if (result) {
            galleryIntent();
        }
    }


    public boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED  ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},  REQUEST_FOR_TAKE_PHOTO);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},  REQUEST_FOR_TAKE_PHOTO);
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
            Toast.makeText(getActivity(),"Permission is needed to capture image for the profile", Toast.LENGTH_LONG).show();
        }
    }

    private void galleryIntent() {
        GridView gallery = (GridView) fragment_view.findViewById(R.id.all_images);
        gallery.setAdapter(new ImageAdapter(getContext(), new CallBackFromComposer() {
            @Override
            public void onElementClick(String s) {
                Fragment image_edit_fragment = new ImageAndTextViewFragment();
                Bundle args = new Bundle();
                args.putString("user_selected_image", s);
                image_edit_fragment.setArguments(args);
                loadFragment(image_edit_fragment);
            }
        }));
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commit();
    }
}
