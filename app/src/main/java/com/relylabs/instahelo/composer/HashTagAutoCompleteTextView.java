package com.relylabs.instahelo.composer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.util.AttributeSet;

import com.relylabs.instahelo.R;

/**
 * Created by nagendra on 9/17/18.
 */

public class HashTagAutoCompleteTextView extends AppCompatAutoCompleteTextView {

    private static final int MESSAGE_TEXT_CHANGED = 100;
    private static final int DEFAULT_AUTOCOMPLETE_DELAY = 100;

    private int mAutoCompleteDelay = DEFAULT_AUTOCOMPLETE_DELAY;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            HashTagAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };


    public void setAutoCompleteDelay(int autoCompleteDelay) {
        mAutoCompleteDelay = autoCompleteDelay;
    }


    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_TEXT_CHANGED, text), mAutoCompleteDelay);
    }

    public HashTagAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public HashTagAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.autoCompleteTextViewStyle);
    }

    public HashTagAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void replaceText(CharSequence text) {

        clearComposingText();

        AutoCompleteAdapter adapter = (AutoCompleteAdapter) getAdapter();
        AutoCompleteAdapter.HashTagFilter filter = (AutoCompleteAdapter.HashTagFilter) adapter.getFilter();

        Editable span = getText();
        span.replace(filter.start, filter.end, text);
    }
}
