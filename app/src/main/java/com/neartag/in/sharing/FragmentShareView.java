package com.neartag.in.sharing;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neartag.in.App;
import com.neartag.in.DeviceUtils;
import com.neartag.in.PreCachingLayoutManager;
import com.neartag.in.R;
import com.neartag.in.Utils.Logger;
import com.neartag.in.camera.Camera2Fragment;
import com.neartag.in.models.Contact;
import com.neartag.in.models.NewsFeedElement;
import com.neartag.in.models.User;
import com.neartag.in.newsfeed.StoryFeedAdapter;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.WeakHashMap;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;
/**
 * Created by nagendra on 8/30/18.
 *
 */
public class FragmentShareView extends Fragment implements  ContactsListAdapter.ItemClickListener {

    ContactsListAdapter adapter;
    RecyclerView contacts_list;
    public static int REQUEST_FOR_READ_CONTACTS = 9;
    ArrayList<Contact> all_contacts;
    WeakHashMap<Integer, Contact> selected_list = new WeakHashMap<>();
    TextView start_sharing;


    public static FragmentShareView newInstance(){
        return new FragmentShareView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_contacts, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contacts_list = view.findViewById(R.id.contacts_list);
        all_contacts = new ArrayList<>();
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        adapter = new ContactsListAdapter(getContext(), all_contacts);
        adapter.setClickListener(this);
        contacts_list.setLayoutManager(layoutManager);
        contacts_list.setAdapter(adapter);
        findAndReadContacts();
        start_sharing = view.findViewById(R.id.start_sharing);
        start_sharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStory();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

    public void findAndReadContacts() {
        boolean result = checkPermission(getActivity());
        if (result) {
            fetchContacts();
        }
    }


    public boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.READ_CONTACTS)) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_FOR_READ_CONTACTS);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_FOR_READ_CONTACTS);
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
            fetchContacts();

        } else {
            Toast.makeText(getActivity(), "Permission is needed to share the story", Toast.LENGTH_LONG).show();
        }
    }


    private void loadFragment() {
        getActivity().onBackPressed();
    }

    private void fetchContacts() {
        ArrayList<Contact> contacts = readContacts();
        all_contacts.addAll(contacts);
        adapter.notifyDataSetChanged();

    }

    private void setUpFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, fragment_to_start);
        ft.addToBackStack(null);
        ft.commit();
    }

    public ArrayList<Contact> readContacts(){
        ArrayList<Contact> contacts = new ArrayList<>();
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);

                    String phone = "";
                    while (pCur.moveToNext()) {
                        phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }

                    pCur.close();
                    contacts.add(new Contact(name, phone));
                }
            }
        }

        return contacts;
    }

    @Override
    public void onItemClick(View view, int position) {
        if (selected_list.containsKey(position)) {
            selected_list.remove(position);
        } else {
            selected_list.put(position, all_contacts.get(position));
        }
    }

    private void createStory() {
        Logger.log(Logger.STORY_CREATE_START);
        android.util.Log.d("debug_data", "upload started...");
        User user = User.getLoggedInUser();
        final AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        try {
            String path = "/storage/emulated/0/Android/data/com.neartag.in/files/";
            File imgfile = new File(path + "/temp_image.jpg");
            params.put("file", imgfile);
        } catch(FileNotFoundException fexception) {
            fexception.printStackTrace();
        }
        JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Integer width = 0, height  = 0;
                try {
                    JSONObject data = response.getJSONObject("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Logger.log(Logger.STORY_CREATE_SUCCESS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.RES, res);
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.STORY_CREATE_FAILED, log_data);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                if (obj != null) {
                    log_data.put(Logger.JSON, obj.toString());
                }
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.STORY_CREATE_FAILED, log_data);
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "story/create", params, jrep);
    }
}
