package com.relylabs.around;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

/**
 * Created by nagendra on 8/9/18.
 */

public class ImageAndTextView extends RelativeLayout {



    LayoutInflater mInflater;
    public ImageAndTextView(Context context) {
        super(context);
        mInflater = LayoutInflater.from(context);
        init();

    }
    public ImageAndTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public ImageAndTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = LayoutInflater.from(context);
        init();
    }
    public void init()
    {
         mInflater.inflate(R.layout.image_and_text_layout, this, true);
    }

}
