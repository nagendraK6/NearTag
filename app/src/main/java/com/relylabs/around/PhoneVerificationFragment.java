package com.relylabs.around;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import cz.msebera.android.httpclient.Header;

/**
 * Created by nagendra on 7/10/18.
 */

public class PhoneVerificationFragment extends Fragment {

    String otp1_text;
    String otp2_text = "";
    String otp3_text = "";
    String otp4_text = "";
    Boolean should_resend_otp = false;
    int timer = 0;
    private Handler handler = new Handler();
    EditText otp1, otp2, otp3, otp4;
    TextView phone_no_label;
    boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.phone_verification_fragment, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        phone_no_label = view.findViewById(R.id.toverify);
        running = true;
        final User user = User.getLoggedInUser();

        //phone_no_label.setText("Enter the 4-digit code we sent to\n" + user.getFormattedNo());
        otp1 = view.findViewById(R.id.otp1);
        otp2 = view.findViewById(R.id.otp2);
        otp3 = view.findViewById(R.id.otp3);
        otp4 = view.findViewById(R.id.otp4);


        otp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 1) {
                    otp1_text = editable.toString();
                    otp2.requestFocus();
                }
            }
        });

        otp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 1) {
                    otp2_text = editable.toString();
                    otp3.requestFocus();
                }
            }
        });

        otp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() == 1) {
                    otp3_text = editable.toString();
                    otp4.requestFocus();
                }
            }
        });

        otp4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                otp4_text = editable.toString();
                String otp = otp1_text + otp2_text + otp3_text + otp4_text;
                if (otp.length() == 4) {
                    ProgressBar busy = view.findViewById(R.id.busy_send_otp);
                    busy.setVisibility(View.VISIBLE);

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.addHeader("Accept", "application/json");
                    client.addHeader("Authorization", "Bearer " + user.AccessToken);
                    Log.d("debug_data", user.AccessToken);
                    RequestParams params = new RequestParams();
                    params.add("otp", otp);
                    Log.d("debug_data", "otp is " + otp);

                    SharedPreferences cached = getActivity().getSharedPreferences("app_shared_pref", Context.MODE_PRIVATE);
                    String fcm_token = cached.getString("fcm_token", null);
                    params.add("fcm_token", fcm_token);

                    JsonHttpResponseHandler jrep = new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                String error_message = (String) response.getString("error_message");
                                if (!error_message.equals("SUCCESS") && running) {
                                    Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();

                                    ProgressBar busy = view.findViewById(R.id.busy_send_otp);
                                    busy.setVisibility(View.INVISIBLE);
                                    otp1.setText("");
                                    otp2.setText("");
                                    otp3.setText("");
                                    otp4.setText("");
                                    otp1.requestFocus();
                                    //EditText otp = view.findViewById(R.id.otp);
                                    //otp.setText("");
                                    return;
                                }

                                String user_token = (String) response.getString("user_token");

                                User user = User.getLoggedInUser();
                                user.AccessToken = user_token;
                                user.IsOTPVerified = true;
                                user.save();



                                loadFragment(new NewsFeedFragment());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            Log.d("debug_data", "" + res);
                            HashMap logData = new HashMap<String, String>();
                            logData.put("status_code", statusCode);
                            logData.put("message", t.getMessage());
                            // PhirkiLogging.log(PhirkiLogging.USER_REGISTRATION_DATA_FAILED, logData);
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

                    client.post(App.getBaseURL() + "user_register/otp_send", params, jrep);
                }
            }

        });

        TextView resend = (TextView) view.findViewById(R.id.re_send);
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View fragment_view) {
                if (should_resend_otp) {
                    sendNewOTP();
                    startTimer(view);
                }
            }
        });
    }

    private void startTimer(final View fragment_view) {
        final TextView resend = (TextView) fragment_view.findViewById(R.id.re_send);
        resend.setBackgroundResource(R.drawable.disabled_text);
        should_resend_otp = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (timer < 30) {
                    timer += 1;

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            resend.setText("Resend SMS in " + (30 - timer) + " seconds");

                            // TODO Auto-generated method stub
                            if (timer == 30) {
                                should_resend_otp = true;
                                TextView resend = (TextView) fragment_view.findViewById(R.id.re_send);
                                resend.setBackgroundResource(R.drawable.incorrect_display);
                                resend.setText("Resend SMS");

                            }
                        }
                    });


                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(1000); //thread will take approx 3 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                timer = 0;
            }
        }).start();
    }

    private void sendNewOTP() {
        final User user = User.getLoggedInUser();

        otp1_text = "";
        otp2_text = "";
        otp3_text = "";
        otp4_text = "";

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        Log.d("debug_data", user.AccessToken);
        RequestParams params = new RequestParams();

        JsonHttpResponseHandler jrep = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String error_message = (String) response.getString("error_message");
                    if (!error_message.equals("SUCCESS")) {
                        Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Log.d("debug_data", "" + res);
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

        client.post(App.getBaseURL() + "user_register/otp_resend", params, jrep);
    }

    private void loadFragment(Fragment fragment_to_start) {
        if (running) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_holder, fragment_to_start);
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

}
