package com.relylabs.neartag;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amplitude.api.Amplitude;
import com.relylabs.neartag.models.User;
import com.relylabs.neartag.registration.LoginFragment;
import com.relylabs.neartag.registration.PhoneVerificationFragment;
import com.relylabs.neartag.registration.UserNameAskFragment;
import com.relylabs.neartag.registration.UserPreferenceAskFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user =  User.getLoggedInUser();
        if (user != null) {
            Amplitude.getInstance().initialize(
                    getApplicationContext(),
                    "fcb10b1e728780e9d9bae42ac53d16a4",
                    Integer.toString(user.UserID)
            ).enableForegroundTracking(getApplication()
            );
        } else {
            Amplitude.getInstance().initialize(
                    getApplicationContext(),
                    "fcb10b1e728780e9d9bae42ac53d16a4").enableForegroundTracking(getApplication()
            );
        }
        setContentView(R.layout.activity_main);
        setUpFragment(findFragment());
    }

    private void setUpFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commitAllowingStateLoss();
    }

    private Fragment findFragment() {
        User user = User.getLoggedInUser();
        if (user == null) {
            return new LoginFragment();
        }

        if (user.IsOTPVerified == false) {
            return new PhoneVerificationFragment();
        }

        return new NewsFeedFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment uploadType = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (uploadType != null) {
            uploadType.onActivityResult(requestCode, resultCode, data);
        }
    }
}
