package com.relylabs.around;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import okhttp3.internal.cache.DiskLruCache;

/**
 * Created by nagendra on 8/9/18.
 */

public class ImageEditFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_photo, container, false);
        ImageView img = view.findViewById(R.id.user_photo);
        Picasso.with(getContext()).load("https://www.rely.ai/Image/1503784090.jpg").into(img);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button btn = view.findViewById(R.id.create_img);
        final ImageView created_one = view.findViewById(R.id.created_one);

        final ImageAndTextView v = view.findViewById(R.id.image_section);
        Log.d("debug_data", "I am called ");
        if (v == null) {
            Log.d("debug_data", "I am null");
        } else {
            Log.d("debug_data", "I am NOT null");
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                v.setDrawingCacheEnabled(true);

// this is the important code :)
// Without it the view will have a dimension of 0,0 and the bitmap will be null
                v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

                v.buildDrawingCache(true);

                Bitmap bitmap = v.getDrawingCache();
                if (bitmap != null) {
                    created_one.setImageBitmap(bitmap.copy(bitmap.getConfig(), false));
                }
                v.setDrawingCacheEnabled(false);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }
}
