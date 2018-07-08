package com.relylabs.around;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.relylabs.around.db.User;
import com.relylabs.around.db.UserDao;

/**
 * Created by nagendra on 7/3/18.
 */

public class LoginFragment extends Fragment {

    EditText phone_no;
    Boolean running = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        running = true;
        final View view = inflater.inflate(R.layout.login_fragment, container, false);
        phone_no = view.findViewById(R.id.edit_txt_phone);
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
    }
}
