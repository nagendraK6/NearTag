package com.relylabs.around;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.relylabs.around.db.DaoMaster;

import org.greenrobot.greendao.database.Database;

/**
 * Created by nagendra on 7/10/18.
 */

public class UserNameAskFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_name_ask_fragment, container, false);
    }
}
