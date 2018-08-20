package com.relylabs.around;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.xiaopo.flying.sticker.BitmapStickerIcon;
import com.xiaopo.flying.sticker.StickerView;
import com.xiaopo.flying.sticker.TextSticker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by nagendra on 8/12/18.
 */

public class ImageAndTextViewFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.text_on_image_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String local_image_url = getArguments().getString("user_selected_image");

        final ImageView img = view.findViewById(R.id.user_photo);
        Picasso.with(getContext()).load(
                new File(local_image_url)
        ).into(img);


        final StickerView st_view = view.findViewById(R.id.sticker_view);


        final TextSticker sticker = new TextSticker(getContext());
        sticker.setText("Hello world");
        sticker.setTextColor(Color.BLACK);
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        sticker.resizeText();


        BitmapStickerIcon heartIcon =
                new BitmapStickerIcon(getContext().getDrawable(R.drawable.ic_fav_white_24dp),
                        BitmapStickerIcon.LEFT_BOTTOM);


        st_view.addSticker(sticker);
        st_view.configDefaultIcons();


  //      st_view.setIcons(Arrays.asList( heartIcon));



        ImageView ok_click = view.findViewById(R.id.ok_tick_mark);
        ok_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagSearchFragment frg = new TagSearchFragment();
                Bundle x = new Bundle();


                st_view.setLocked(true);
                st_view.setDrawingCacheEnabled(true);
                st_view.buildDrawingCache(true);

                Bitmap bitmap = st_view.getDrawingCache();

                Bitmap bitmap2 = bitmap.copy(bitmap.getConfig(), false);


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
                    bitmap2.recycle();
                }

                //Cleanup

                st_view.setDrawingCacheEnabled(false);
                st_view.setLocked(false);
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
