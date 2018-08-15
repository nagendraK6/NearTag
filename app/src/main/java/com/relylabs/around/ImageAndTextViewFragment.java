package com.relylabs.around;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by nagendra on 8/12/18.
 */

public class ImageAndTextViewFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.image_and_text_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String local_image_url = getArguments().getString("user_selected_image");

        final ImageView img = view.findViewById(R.id.user_photo);
        Picasso.with(getContext()).load(
                new File(local_image_url)
        ).into(img);


        TextView ok_click = view.findViewById(R.id.image_edit_complete);
        ok_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComposerFragment frg = new ComposerFragment();
                Bundle x = new Bundle();
                img.setDrawingCacheEnabled(true);
                img.buildDrawingCache(true);

                Bitmap bitmap = img.getDrawingCache();

                bitmap = bitmap.copy(bitmap.getConfig(), false);


                String filename = "bitmap.png";
                FileOutputStream stream = null;
                try {
                    stream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bitmap.recycle();
                }

                //Cleanup

                img.setDrawingCacheEnabled(false);
                x.putString("image_file_name", filename);
                frg.setArguments(x);
                loadFragment(frg);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }

    private void loadFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commitAllowingStateLoss();
    }
}
