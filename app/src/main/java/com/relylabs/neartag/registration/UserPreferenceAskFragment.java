package com.relylabs.neartag.registration;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.neartag.App;
import com.relylabs.neartag.DeviceUtils;
import com.relylabs.neartag.NewsFeedAdapter;
import com.relylabs.neartag.NewsFeedFragment;
import com.relylabs.neartag.PreCachingLayoutManager;
import com.relylabs.neartag.R;
import com.relylabs.neartag.Utils.Logger;
import com.relylabs.neartag.composer.MyRecyclerViewAdapter;
import com.relylabs.neartag.models.NewsFeedElement;
import com.relylabs.neartag.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.WeakHashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 9/12/18.
 */

public class UserPreferenceAskFragment extends Fragment implements PreferenceListAdapter.ItemClickListener {
    ProgressBar busy;
    Boolean running = false;
    ArrayList<String> current_preference;
    ArrayList<String> all_preferences;
    RecyclerView recyclerView;
    PreferenceListAdapter adapter;
    TextView next_button;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_preferences_ask_fragment, container, false);
        running = true;
        busy = view.findViewById(R.id.busy_send_preference);
        FadingCircle cr = new FadingCircle();
        cr.setColor(R.color.black);
        busy.setIndeterminateDrawable(cr);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.preference_list);


        all_preferences = new ArrayList<String>();
        current_preference = new ArrayList<String>();
        all_preferences.add(getString(R.string.option_1));
        all_preferences.add(getString(R.string.option_2));
        all_preferences.add(getString(R.string.option_3));
        all_preferences.add(getString(R.string.option_4));
        all_preferences.add(getString(R.string.option_5));
        all_preferences.add(getString(R.string.option_6));
        all_preferences.add(getString(R.string.option_7));
        all_preferences.add(getString(R.string.option_1));
        all_preferences.add(getString(R.string.option_2));
        all_preferences.add(getString(R.string.option_3));
        all_preferences.add(getString(R.string.option_4));
        all_preferences.add(getString(R.string.option_5));
        all_preferences.add(getString(R.string.option_6));
        all_preferences.add(getString(R.string.option_7));

        // Create adapter passing in the sample user data
        adapter = new PreferenceListAdapter((AppCompatActivity) getActivity(),  all_preferences);
        adapter.setClickListener(this);

        // Attach the adapter to the recyclerview to populate items
        recyclerView.setAdapter(adapter);

        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        recyclerView.setLayoutManager(layoutManager);

        next_button = view.findViewById(R.id.send_user_preference);



        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busy.setVisibility(View.VISIBLE);
                next_button.setVisibility(View.INVISIBLE);
                sendPreferences();
            }
        });
    }

    private void loadFragment(Fragment fragment_to_start) {
        if (running) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_holder, fragment_to_start);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
    }

    @Override
    public void onPause() {
        running = false;
        super.onPause();
    }

    @Override
    public void onItemClick(int position, boolean shouldAdd) {
        String preference = all_preferences.get(position);
        if (shouldAdd) {
            if (!current_preference.contains(preference)) {
                current_preference.add(preference);
            }
        } else {
            if (current_preference.contains(preference)) {
                current_preference.remove(preference);
            }
        }
    }

    public void sendPreferences() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        User user = User.getLoggedInUser();
        Logger.log(Logger.USER_PREFERENCE_SEND_REQUEST_START);

        JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String error_message = (String) response.getString("error_message");
                    if (!error_message.equals("SUCCESS")) {
                        Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();
                        busy.setVisibility(View.INVISIBLE);
                        return;
                    }

                    Logger.log(Logger.USER_PREFERENCE_SEND_REQUEST_SUCCESS);
                    loadFragment(new NewsFeedFragment());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.RES, res);
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.USER_PREFERENCE_SEND_REQUEST_FAILED, log_data);
                busy.setVisibility(View.INVISIBLE);
                next_button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.JSON, obj.toString());
                log_data.put(Logger.THROWABLE, t.toString());
                Logger.log(Logger.USER_PREFERENCE_SEND_REQUEST_FAILED, log_data);
                busy.setVisibility(View.INVISIBLE);
                next_button.setVisibility(View.VISIBLE);
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post( App.getBaseURL() + "user_register/preference_send", params, jrep);
    }
}