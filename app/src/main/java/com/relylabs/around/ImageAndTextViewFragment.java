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

        AlertDialog.Builder dialog = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        } else {
            dialog = new AlertDialog.Builder(getContext());
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

        View promptView = layoutInflater.inflate(R.layout.add_image_text_dialog, null);
        dialog.setView(promptView);

        final AlertDialog alertDialog_ref = dialog.create();

        ImageView text_add_click = view.findViewById(R.id.text_add_option);
        text_add_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog_ref.show();
            }
        });

        StickerView st_view = view.findViewById(R.id.sticker_view);


        final TextSticker sticker = new TextSticker(getContext());
        sticker.setText("Hello world");
        sticker.setTextColor(Color.BLACK);
        sticker.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        sticker.resizeText();


        BitmapStickerIcon heartIcon =
                new BitmapStickerIcon(getContext().getDrawable(R.drawable.ic_fav_white_24dp),
                        BitmapStickerIcon.LEFT_BOTTOM);
       heartIcon.setIconEvent(new HelloIconEvent());


        st_view.addSticker(sticker);
        st_view.configDefaultIcons();
  //      st_view.setIcons(Arrays.asList( heartIcon));



        /*TextView ok_click = view.findViewById(R.id.image_edit_complete);
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
        });*/

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
