package com.relylabs.around;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.around.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 7/10/18.
 */

public class UserNameAskFragment extends Fragment {


    EditText user_name;
    Boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.user_name_ask_fragment, container, false);
        final User user = User.getLoggedInUser();
        user_name = view.findViewById(R.id.edit_user_name);
        user_name.setText(user.Name);
        final TextView send_button = view.findViewById(R.id.send_user_name);
        running = true;
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                if (user_name.getText().toString() == "") {
                    Toast.makeText(getContext(), "Name is required", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("debug_data", user_name.getText().toString());
                ProgressBar busy = view.findViewById(R.id.busy_send_user_name);
                busy.setVisibility(View.VISIBLE);
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                client.addHeader("Accept", "application/json");
                client.addHeader("Authorization", "Bearer " + user.AccessToken);
                params.add("user_name", user_name.getText().toString());


                JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String error_message = (String) response.getString("error_message");
                            if (!error_message.equals("SUCCESS")) {
                                Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();

                                ProgressBar busy = view.findViewById(R.id.busy_send_user_name);
                                busy.setVisibility(View.INVISIBLE);
                                return;
                            }



                            user.Name = user_name.getText().toString();
                            user.save();

                            loadFragment(new NewsFeedFragment());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        Log.d("debug data", " " + statusCode);
                        HashMap logData = new HashMap<String, String>();
                        logData.put("status_code", statusCode);
                        logData.put("message", t.getMessage());
                        //PhirkiLogging.log(PhirkiLogging.USER_REGISTRATION_DATA_FAILED, logData);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
                        if (statusCode == 401) {
                            user.AccessToken = "";
                            user.save();
                            loadFragment(new LoginFragment());
                        }
                    }
                };

                client.post( App.getBaseURL() + "user_register/user_name_send", params, jrep);
            }
        });

        return view;
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
        user_name.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity()!= null) {
                    user_name.requestFocus();
                    user_name.setSelection(user_name.getText().length());
                    InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imgr.showSoftInput(user_name, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
        //App.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onPause() {
        running = false;
        super.onPause();
    }
}
