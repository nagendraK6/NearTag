package com.relylabs.around;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.around.composer.RecyclerGalaryFragment;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nagendra on 8/30/18.
 */
public class AccountEditFragment extends Fragment {

    CircleImageView profile;
    Boolean image_updated = false;
    String image_file_name;


    BroadcastReceiver broadCastNewMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("debug_data", "received data");


            image_file_name = intent
                    .getStringExtra(getString(R.string.user_selected_image));

            image_updated = true;

            File f = new File(image_file_name);

            Picasso.with(getContext()).load(Uri.fromFile(f)).
                    resize(120, 120).
                    into(
                            profile,
                            new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError() {
                                    //do smth when there is picture loading error
                                }
                            }
                    );
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editprofile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment();
            }
        });

        TextView changePhoto = view.findViewById(R.id.changeProfilePhoto);
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment frag = new RecyclerGalaryFragment();
                Bundle bnd = new Bundle();
                bnd.putString("ref", "profile");
                frag.setArguments(bnd);
                setUpFragment(frag);
            }
        });

        profile = view.findViewById(R.id.profile_photo);

            ImageView saveChange = view.findViewById(R.id.saveChanges);
            saveChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (image_updated) {

                        android.util.Log.d("debug_data", "upload started...");
                        User user = User.getLoggedInUser();
                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestParams params = new RequestParams();
                        try {
                            File imgfile = new File(image_file_name);
                            params.put("new_profile_image", imgfile);
                        } catch(FileNotFoundException fexception) {
                            fexception.printStackTrace();
                        }

                        JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                android.util.Log.d("debug_data", "uploaded the image on server...");

                                // update user profile and broadcast the update
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                HashMap logData = new HashMap<String, String>();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                                HashMap logData = new HashMap<String, String>();
                            }
                        };

                        client.addHeader("Accept", "application/json");
                        client.addHeader("Authorization", "Bearer " + user.AccessToken);
                        client.post(App.getBaseURL() + "profile/update", params, jrep);

                    }
                }
            });

        IntentFilter profile_update = new IntentFilter("profile_update");
        getActivity().registerReceiver(broadCastNewMessage, profile_update);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //App.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void loadFragment() {
        getActivity().onBackPressed();
    }

    private void setUpFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }
}
