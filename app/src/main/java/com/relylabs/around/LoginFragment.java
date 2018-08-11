package com.relylabs.around;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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
        running = true;
        final View view = inflater.inflate(R.layout.login_fragment, container, false);
        phone_no = view.findViewById(R.id.edit_txt_phone);
        phone_desc = view.findViewById(R.id.phone_no_desc);
        country_code = view.findViewById(R.id.country_code);
        busy = view.findViewById(R.id.busy_send);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        running = true;

        Typeface fontHindi = Typeface.createFromAsset(getContext().getAssets(), "mangal.ttf");
        phone_desc.setTypeface(fontHindi);
        phone_desc.setText(R.string.phn_msg);
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
                    builder.setMessage("We will be verifying the phone number \n\n" + country_code.getText() + "-" + phone_number + "\n\n" + "Is this ok or would you like to enter new number?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                                                String error_message = (String) response.getString("error_message");
                                                if (!error_message.equals("SUCCESS")) {
                                                    Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();
                                                    phone_no.setText("");
                                                    return;
                                                }

                                                Toast.makeText(getContext(), " Message sent ", Toast.LENGTH_LONG).show();
                                                busy.setVisibility(View.INVISIBLE);

                                                String user_id = (String) response.getString("user_id");
                                                String user_name = (String) response.getString("user_name");
                                                String user_location = (String) response.getString("user_location");
                                                String user_token = (String) response.getString("user_token");
                                                User user = User.getLoggedInUser();

                                                if(user == null) {
                                                    //
                                                    user = new User();
                                                    user.Name = user_name;
                                                    user.Location = user_location;
                                                    user.IsOTPVerified = false;
                                                    user.AccessToken = user_token;
                                                } else {
                                                    user.AccessToken = user_token;
                                                }

                                                user.save();
                                                loadFragment(new PhoneVerificationFragment());
                                                // move to code verification
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                                            Log.d("debug data_error", " " + statusCode);
                                        }
                                    };

                                    busy.setVisibility(View.VISIBLE);
                                    client.post( App.getBaseURL() + "user_register/phone_add", params, jrep);

                                }
                            })
                            .setNegativeButton("EDIT", new DialogInterface.OnClickListener() {
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
