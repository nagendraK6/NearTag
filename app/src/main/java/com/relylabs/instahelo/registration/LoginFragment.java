package com.relylabs.instahelo.registration;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.text.TextWatcher;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.instahelo.App;
import com.relylabs.instahelo.R;
import com.relylabs.instahelo.Utils.Logger;
import com.relylabs.instahelo.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.WeakHashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 7/3/18.
 */

public class LoginFragment extends Fragment {

    EditText phone_no;
    EditText country_code;
    ProgressBar busy;
    TextView phone_desc;
    Boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.login_fragment, container, false);
        phone_no = view.findViewById(R.id.edit_txt_phone);
        phone_desc = view.findViewById(R.id.phone_no_desc);
        country_code = view.findViewById(R.id.country_code);
        busy = view.findViewById(R.id.busy_send);
        FadingCircle cr = new FadingCircle();
        cr.setColor(R.color.neartagtextcolor);
        busy.setIndeterminateDrawable(cr);
        running = true;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;
        phone_no.post(new Runnable() {
            @Override
            public void run() {
                phone_no.requestFocus();
                if (getActivity()!= null) {
                    InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imgr.showSoftInput(phone_no, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPhoneNo();
    }

    @Override
    public void onPause() {
        running = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

    private  void setupPhoneNo() {
        phone_no.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String phone_number = editable.toString();
                if (phone_number.length() == 10) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(getContext());
                    }

                    Logger.log(Logger.PHONE_ADD_REQUEST_START);
                    builder.setMessage(getString(R.string.verify_no_msg) +  " \n\n" + country_code.getText() + "-" + phone_number + "\n\n" + getString(R.string.edit_no_msg))
                            .setPositiveButton(getString(R.string.ok) , new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    // continue with delete
                                    AsyncHttpClient client = new AsyncHttpClient();
                                    RequestParams params = new RequestParams();
                                    params.add("phone_no", phone_number);
                                    params.add("country_code", country_code.getText().toString());
                                    JsonHttpResponseHandler jrep= new JsonHttpResponseHandler() {
                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                            try {
                                                String error_message = response.getString("error_message");
                                                if (!error_message.equals("SUCCESS")) {
                                                    Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();
                                                    phone_no.setText("");
                                                    return;
                                                }

                                                Toast.makeText(getContext(), getString(R.string.otp_send_text), Toast.LENGTH_LONG).show();
                                                busy.setVisibility(View.INVISIBLE);

                                                Integer user_id =  response.getInt("user_id");
                                                String user_name = response.getString("user_name");
                                                String user_location = response.getString("user_location");
                                                String user_token = response.getString("user_token");
                                                String description = response.getString("description");

                                                User user = User.getLoggedInUser();

                                                if(user == null) {
                                                    //
                                                    user = new User();
                                                    user.CountryCode = country_code.getText().toString();
                                                    user.PhoneNo = phone_number;
                                                    user.UserID = user_id;
                                                    user.Name = user_name;
                                                    user.Location = user_location;
                                                    user.IsOTPVerified = false;
                                                    user.AccessToken = user_token;
                                                    user.Description = description;
                                                    user.ProfilePicURL = response.getString("profile_image_url");
                                                } else {
                                                    user.UserID = user_id;
                                                    user.AccessToken = user_token;
                                                }

                                                Logger.log(Logger.PHONE_ADD_REQUEST_SUCCESS);
                                                user.save();
                                                loadFragment(new PhoneVerificationFragment());
                                                // move to code verification
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
                                            Logger.log(Logger.PHONE_ADD_REQUEST_FAILED, log_data);
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                                            WeakHashMap<String, String> log_data = new WeakHashMap<>();
                                            log_data.put(Logger.STATUS, Integer.toString(statusCode));
                                            if (obj != null) {
                                                log_data.put(Logger.JSON, obj.toString());
                                            }

                                            log_data.put(Logger.THROWABLE, t.toString());
                                            Logger.log(Logger.PHONE_ADD_REQUEST_FAILED, log_data);
                                        }
                                    };

                                    busy.setVisibility(View.VISIBLE);
                                    client.post( App.getBaseURL() + "registration/phone_add", params, jrep);

                                }
                            })
                            .setNegativeButton(getString(R.string.edit), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    phone_no.setText("");

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            }
        });
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commit();
    }
}
