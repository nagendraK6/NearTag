package com.relylabs.instahelo.sharing;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.relylabs.instahelo.App;
import com.relylabs.instahelo.DeviceUtils;
import com.relylabs.instahelo.PreCachingLayoutManager;
import com.relylabs.instahelo.R;
import com.relylabs.instahelo.models.Contact;


import java.util.ArrayList;
import java.util.WeakHashMap;

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
    ProgressBar bus_load;

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
                if (selected_list.size() == 0) {
                    Toast.makeText(getContext(), "Please must select a contact", Toast.LENGTH_LONG).show();
                    return;
                }

                loadFragment();
            }
        });
        bus_load = view.findViewById(R.id.busy_load);
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

    private void fetchContacts() {
        ArrayList<Contact> contacts = readContacts();
        all_contacts.addAll(contacts);
        adapter.notifyDataSetChanged();

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
    public void onItemClick(int position) {
        if (selected_list.containsKey(position)) {
            selected_list.remove(position);
        } else {
            selected_list.put(position, all_contacts.get(position));
        }
    }

    private void loadFragment() {
        Fragment frg = new FragmentSelectChannel();
        Bundle data = new Bundle();
        ArrayList<Contact> selected_contacts = new ArrayList<>();
        for(Contact c: selected_list.values() ) {
            selected_contacts.add(c);
        }

        data.putParcelableArrayList("contacts", selected_contacts);
        frg.setArguments(data);
        loadFragment(frg);
    }

    private void loadFragment(Fragment frg) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, frg);
        ft.addToBackStack(null);
        ft.commit();
    }
}
