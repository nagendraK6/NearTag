package com.relylabs.neartag.registration;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.relylabs.neartag.App;
import com.relylabs.neartag.R;
import com.relylabs.neartag.Utils.Logger;
import com.relylabs.neartag.models.User;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.WeakHashMap;

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
    ProgressBar busy;
    String otp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.phone_verification_fragment, container, false);
        busy = view.findViewById(R.id.busy_send_otp);
        FadingCircle cr = new FadingCircle();
        cr.setColor(R.color.neartagtextcolor);
        busy.setIndeterminateDrawable(cr);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        phone_no_label = view.findViewById(R.id.toverify);
        running = true;

        if (shouldRegister(getContext())) {
            SmsReceiver.bindListener(new SmsListener() {
                @Override
                public void messageReceived(String messageText) {
                    otp = messageText;
                    otpSendToServer(otp, false);
                }
            });
        }

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
                Logger.log(Logger.OTP_TYPING);
                otp4_text = editable.toString();
                String otp = otp1_text + otp2_text + otp3_text + otp4_text;
                if (otp.length() == 4) {
                    otpSendToServer(otp, false);
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
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject obj) {
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
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

    public boolean shouldRegister(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    private void otpSendToServer(String otp, final Boolean auto) {
        if (auto) {
            Logger.log(Logger.OTP_VERIFY_REQUEST_START);
        } else {
            Logger.log(Logger.AUTO_OTP_VERIFY_REQUEST_START);
        }
        busy.setVisibility(View.VISIBLE);
        FadingCircle cr = new FadingCircle();
        cr.setColor(R.color.neartagtextcolor);
        busy.setIndeterminateDrawable(cr);
        final User user = User.getLoggedInUser();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("otp", otp);

        SharedPreferences cached = getActivity().getSharedPreferences("app_shared_pref", Context.MODE_PRIVATE);
        String fcm_token = cached.getString("fcm_token", null);
        params.add("fcm_token", fcm_token);

        JsonHttpResponseHandler jrep = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String error_message = (String) response.getString("error_message");
                    if (!error_message.equals("SUCCESS") && running) {
                        busy.setVisibility(View.INVISIBLE);
                        if (!auto) {
                            Toast.makeText(getContext(), error_message, Toast.LENGTH_LONG).show();
                            otp1.setText("");
                            otp2.setText("");
                            otp3.setText("");
                            otp4.setText("");
                            otp1.requestFocus();
                        }
                        return;
                    }

                    String user_token = (String) response.getString("user_token");
                    user.AccessToken = user_token;
                    user.IsOTPVerified = true;
                    user.save();
                    if (auto) {
                        Logger.log(Logger.OTP_VERIFY_REQUEST_SUCCESS);
                    } else {
                        Logger.log(Logger.AUTO_OTP_VERIFY_REQUEST_SUCCESS);
                    }
                    loadFragment(new UserNameAskFragment());
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
                if (auto) {
                    Logger.log(Logger.OTP_VERIFY_REQUEST_FAILED, log_data);
                } else {
                    Logger.log(Logger.AUTO_OTP_VERIFY_REQUEST_FAILED, log_data);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable t, JSONObject obj) {
                WeakHashMap<String, String> log_data = new WeakHashMap<>();
                log_data.put(Logger.STATUS, Integer.toString(statusCode));
                log_data.put(Logger.JSON, obj.toString());
                log_data.put(Logger.THROWABLE, t.toString());
                if (auto) {
                    Logger.log(Logger.OTP_VERIFY_REQUEST_FAILED, log_data);
                } else {
                    Logger.log(Logger.AUTO_OTP_VERIFY_REQUEST_FAILED, log_data);
                }
            }
        };

        client.addHeader("Accept", "application/json");
        client.addHeader("Authorization", "Bearer " + user.AccessToken);
        client.post(App.getBaseURL() + "user_register/otp_send", params, jrep);
    }
}
