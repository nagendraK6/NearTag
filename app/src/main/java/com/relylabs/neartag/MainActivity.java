package com.relylabs.neartag;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.relylabs.neartag.models.User;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}
