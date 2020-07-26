package com.relylabs.instahelo.sharing;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.relylabs.instahelo.App;
import com.relylabs.instahelo.DeviceUtils;
import com.relylabs.instahelo.PreCachingLayoutManager;
import com.relylabs.instahelo.R;
import com.relylabs.instahelo.models.Contact;


import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;

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
