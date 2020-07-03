package com.relylabs.instahelo.webview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.github.ybq.android.spinkit.style.FadingCircle;
import com.relylabs.instahelo.App;
import com.relylabs.instahelo.R;

/**
 * Created by nagendra on 9/28/18.
 * webview component
 */

public class WebviewFragment extends Fragment {

    private WebView mWebView;
    private ProgressBar busy;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.webview_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String url = getArguments().getString("url");
        busy = view.findViewById(R.id.busy_load);
        FadingCircle cr = new FadingCircle();
        cr.setColor(R.color.neartagtextcolor);
        busy.setIndeterminateDrawable(cr);
        busy.setVisibility(View.VISIBLE);

        mWebView = view.findViewById(R.id.webview);
        mWebView.loadUrl(url);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                busy.setVisibility(View.INVISIBLE);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            }
        });

        ImageView closeButton = view.findViewById(R.id.ivCloseShare);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getRefWatcher(getActivity()).watch(this);
    }
}
