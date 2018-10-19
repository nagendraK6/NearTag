package com.neartag.in.registration;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.neartag.in.App;
import com.neartag.in.R;
import com.neartag.in.Utils.Logger;
import com.neartag.in.models.User;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.WeakHashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 7/10/18.
 */

public class UserNameAskFragment extends Fragment {


    EditText user_name;
    ProgressBar busy;
    Boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.user_name_ask_fragment, container, false);
        final User user = User.getLoggedInUser();
        user_name = view.findViewById(R.id.edit_user_name);
        user_name.setText(user.Name);
        final TextView send_button = view.findViewById(R.id.send_user_name);

        busy = view.findViewById(R.id.busy_send_user_name);
        FadingCircle cr = new FadingCircle();
        cr.setColor(R.color.black);
        busy.setIndeterminateDrawable(cr);

        running = true;
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                if (StringUtils.isEmpty(user_name.getText().toString())) {
                    Toast.makeText(getContext(), getString(R.string.empty_name_msg), Toast.LENGTH_LONG).show();
                    return;
                }

                busy.setVisibility(View.VISIBLE);
                send_button.setVisibility(View.INVISIBLE);
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.add("user_name", user_name.getText().toString());
                Logger.log(Logger.USER_NAME_SEND_REQUEST_START);


                JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            String error_message = response.getString("error_message");
                            if (!error_message.equals("SUCCESS")) {
                                Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();

                                ProgressBar busy = view.findViewById(R.id.busy_send_user_name);
                                busy.setVisibility(View.INVISIBLE);
                                send_button.setVisibility(View.VISIBLE);
                                return;
                            }



                            user.Name = user_name.getText().toString();
                            user.save();
                            Logger.log(Logger.USER_NAME_SEND_REQUEST_SUCCESS);
                            loadFragment(new UserLocationAskFragment());
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
                        Logger.log(Logger.USER_NAME_SEND_REQUEST_FAILED, log_data);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                        WeakHashMap<String, String> log_data = new WeakHashMap<>();
                        log_data.put(Logger.STATUS, Integer.toString(statusCode));
                        log_data.put(Logger.JSON, obj.toString());
                        log_data.put(Logger.THROWABLE, t.toString());
                        Logger.log(Logger.USER_NAME_SEND_REQUEST_FAILED, log_data);
                    }
                };

                client.addHeader("Accept", "application/json");
                client.addHeader("Authorization", "Bearer " + user.AccessToken);
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
        App.getRefWatcher(getActivity()).watch(this);
    }

    @Override
    public void onPause() {
        running = false;
        super.onPause();
    }
}
