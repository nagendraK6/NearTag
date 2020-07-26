package com.relylabs.instahelo.sharing;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.relylabs.instahelo.R;

import java.io.File;
import java.io.FileOutputStream;

public class ImageSharingView extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image_sharing, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String image_uri = getArguments().getString(getString(R.string.user_selected_image));
        final ImageView img = view.findViewById(R.id.stillshot_imageview_2);
        Glide.with(getContext()).load(new File(image_uri)).into(img);
        ImageView share = view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.invalidate();
                img.setDrawingCacheEnabled(true);
                img.measure(View.MeasureSpec.makeMeasureSpec(
                        img.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(img.getHeight(), View.MeasureSpec.EXACTLY));

                img.buildDrawingCache(true);
                Bitmap b = Bitmap.createBitmap(img.getDrawingCache());
                img.setDrawingCacheEnabled(false); // clear drawing cache
                Bitmap bitmap2 = b.copy(b.getConfig(), false);
                String filename = "temp_image.png";



                String root = Environment.getExternalStorageDirectory().toString();
                root = "/storage/emulated/0/Android/data/com.relylabs.instahelo/files/";
               // File myDir = new File(root);
               // myDir.mkdirs();
                File file = new File("/storage/emulated/0/Android/data/com.relylabs.instahelo/files/temp_image.jpg");
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap2.compress(Bitmap.CompressFormat.PNG, 90, out);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                loadFragment(FragmentShareView.newInstance());
            }
        });
     }

    private void loadFragment(Fragment frg) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_holder, frg);
        ft.addToBackStack(null);
        ft.commit();
    }
}
