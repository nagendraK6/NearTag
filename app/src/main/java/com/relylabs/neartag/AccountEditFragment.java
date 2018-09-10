package com.relylabs.neartag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.activeandroid.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.neartag.composer.RecyclerGalaryFragment;
import com.relylabs.neartag.models.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
import org.apache.commons.lang3.StringUtils;
/**
 * Created by nagendra on 8/30/18.
 */
public class AccountEditFragment extends Fragment {

    CircleImageView profile;
    Boolean profile_updated = false;
    Boolean image_updated = false;
    String image_file_name;


    BroadcastReceiver broadCastNewMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("debug_data", "received data");


            image_file_name = intent
                    .getStringExtra(getString(R.string.user_selected_image));

            profile_updated = true;
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

        final User user = User.getLoggedInUser();
        if (!StringUtils.isEmpty(user.ProfilePicURL)) {
            Picasso.with(getContext()).load(user.ProfilePicURL).
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


        final ImageView saveChange = view.findViewById(R.id.saveChanges);
        final ProgressBar saveChangesBusy = view.findViewById(R.id.saveChangesBusy);
        final EditText user_name = view.findViewById(R.id.username);
        final  EditText display_name = view.findViewById(R.id.display_name);
        user_name.setText(user.Name);
        display_name.setText(user.Name);
        display_name.addTextChangedListener(new TextWatcher() {
                                                @Override
                                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                }

                                                @Override
                                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                }

                                                @Override
                                                public void afterTextChanged(Editable editable) {
                                                    final String new_display_name = editable.toString();
                                                    if (!new_display_name.equals(display_name)) {
                                                        profile_updated = true;
                                                    }
                                                }
                                            }
        );

                saveChange.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (profile_updated) {

                            saveChangesBusy.setVisibility(View.VISIBLE);
                            saveChange.setVisibility(View.INVISIBLE);
                            android.util.Log.d("debug_data", "upload started...");
                            AsyncHttpClient client = new AsyncHttpClient();
                            RequestParams params = new RequestParams();

                            if (image_updated){
                                try {
                                    File imgfile = new File(image_file_name);
                                    params.put("new_profile_image", imgfile);
                                } catch (FileNotFoundException fexception) {
                                    fexception.printStackTrace();
                                }
                            }

                            params.put("name", display_name.getText().toString());

                            JsonHttpResponseHandler jrep = new JsonHttpResponseHandler() {

                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    android.util.Log.d("debug_data", "uploaded the image on server...");

                                    // update user profile and broadcast the update
                                    try {
                                        JSONObject data = (JSONObject) response.getJSONObject("data");
                                        if(data.has("profile_image_url")) {
                                            user.ProfilePicURL = data.getString("profile_image_url");
                                        }

                                        user.Name = display_name.getText().toString();
                                        user.save();
                                        Intent intent = new Intent("user_profile_update");
                                        getActivity().sendBroadcast(intent);
                                        loadFragment();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


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
        App.getRefWatcher(getActivity()).watch(this);
        getActivity().unregisterReceiver(broadCastNewMessage);
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
