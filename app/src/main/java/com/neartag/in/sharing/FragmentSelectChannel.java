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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.neartag.in.App;
import com.neartag.in.DeviceUtils;
import com.neartag.in.PreCachingLayoutManager;
import com.neartag.in.R;
import com.neartag.in.models.Contact;


import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * Created by nagendra on 8/30/18.
 *
 */
public class FragmentSelectChannel extends Fragment {

    SelectedContactsListAdapter adapter;
    RecyclerView contacts_list;
    ArrayList<Contact> all_contacts;
    TextView start_sharing;
    ProgressBar bus_load;
    EditText channel_name;
    public static FragmentShareView newInstance(){
        return new FragmentShareView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_channel, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        contacts_list = view.findViewById(R.id.selected_contacts_list);
        all_contacts = new ArrayList<>();
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        adapter = new SelectedContactsListAdapter(getContext(), all_contacts);
        contacts_list.setLayoutManager(layoutManager);
        contacts_list.setAdapter(adapter);
        findAndReadContacts();
        start_sharing = view.findViewById(R.id.start_sharing);
        channel_name = view.findViewById(R.id.channel_name);
        start_sharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isEmpty(channel_name.getText().toString())) {
                    Toast.makeText(getContext(), "Please choose a channel name", Toast.LENGTH_LONG).show();
                    return;
                }

                broadcastLocalUpdate();
            }
        });
        bus_load = view.findViewById(R.id.busy_load);
        view.findViewById(R.id.left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

    public void findAndReadContacts() {
        fetchContacts();
    }


    private void fetchContacts() {
        ArrayList<Contact> contacts = readContacts();
        all_contacts.addAll(contacts);
        adapter.notifyDataSetChanged();

    }

    public ArrayList<Contact> readContacts(){
        ArrayList<Contact> contacts = getArguments().getParcelableArrayList("contacts");
        return contacts;
    }

    private void broadcastLocalUpdate() {
        bus_load.setVisibility(View.VISIBLE);
        Bundle data_bundle = new Bundle();
        data_bundle.putString(getString(R.string.user_selected_image), "temp_image.jpg");
        data_bundle.putParcelableArrayList("selected_contacts", getArguments().getParcelableArrayList("contacts"));
        data_bundle.putString("channel_name", channel_name.getText().toString());

        Intent intent = new Intent("new_post");
        intent.putExtras(data_bundle);
        getActivity().sendBroadcast(intent);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }
}
