package com.relylabs.instahelo.videobroadcast;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.relylabs.instahelo.App;
import com.relylabs.instahelo.R;
import com.relylabs.instahelo.Utils.Logger;
import com.relylabs.instahelo.registration.LoginFragment;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import de.tavendo.autobahn.WebSocket;
import io.antmedia.webrtcandroidframework.IWebRTCClient;
import io.antmedia.webrtcandroidframework.IWebRTCListener;
import io.antmedia.webrtcandroidframework.WebRTCClient;
import io.antmedia.webrtcandroidframework.apprtc.CallActivity;
import io.antmedia.webrtcandroidframework.apprtc.CallFragment;

import static io.antmedia.webrtcandroidframework.apprtc.CallActivity.EXTRA_CAPTURETOTEXTURE_ENABLED;

public class VideoBroadcastFragment extends Fragment  implements IWebRTCListener  {

    public static final int REQUEST_FOR_TAKE_PHOTO = 9;

    public static final String SERVER_URL = "wss://video.insatori.com/WebRTCAppEE/websocket";
    private CallFragment callFragment;

    private WebRTCClient webRTCClient;
    private String webRTCMode;
    private Button startStreamingButton;
    private String operationName = "";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_broadcast, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkPermission(getActivity());

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        //getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());

        //setContentView(R.layout.activity_main);


        webRTCClient = new WebRTCClient( this,getActivity());

        //webRTCClient.setOpenFrontCamera(false);


        //String streamId = "stream" + (int)(Math.random() * 999);
        String streamId = "streama";
        String tokenId = "tokenId";

        SurfaceViewRenderer cameraViewRenderer = view.findViewById(R.id.camera_view_renderer);

        SurfaceViewRenderer pipViewRenderer = view.findViewById(R.id.pip_view_renderer);

        startStreamingButton = (Button)view.findViewById(R.id.start_streaming_button);

        webRTCClient.setVideoRenderers(pipViewRenderer, cameraViewRenderer);

        // Check for mandatory permissions.


        getActivity().getIntent().putExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, true);

        //TODO make it more developer friendly
        webRTCMode = IWebRTCClient.MODE_PUBLISH;

        if (webRTCMode.equals(IWebRTCClient.MODE_PUBLISH)) {
            startStreamingButton.setText("Start Publishing");
            operationName = "Publishing";
        }
        else  if (webRTCMode.equals(IWebRTCClient.MODE_PLAY)) {
            startStreamingButton.setText("Start Playing");
            operationName = "Playing";
        }
        else if (webRTCMode.equals(IWebRTCClient.MODE_JOIN)) {
            startStreamingButton.setText("Start P2P");
            operationName = "P2P";
        }
        // this.getIntent().putExtra(CallActivity.EXTRA_VIDEO_FPS, 24);
        webRTCClient.init(SERVER_URL, streamId, webRTCMode, tokenId, getActivity().getIntent());
        startStreamingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStreaming(view);
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
        ft.commit();
    }

    private void videowork() {
        Toast.makeText(getActivity(), "Ready for broadcast", Toast.LENGTH_LONG).show();

    }

    public boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, REQUEST_FOR_TAKE_PHOTO);
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, REQUEST_FOR_TAKE_PHOTO);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
        super.onRequestPermissionsResult(RC, per, PResult);
        if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
            videowork();

        } else {
            Toast.makeText(getActivity(), "Permission is needed to capture image for the profile", Toast.LENGTH_LONG).show();
        }
    }

    public void startStreaming(View v) {

        if (!webRTCClient.isStreaming()) {
            ((Button)v).setText("Stop " + operationName);
            webRTCClient.startStream();
        }
        else {
            ((Button)v).setText("Start " + operationName);
            webRTCClient.stopStream();
        }
    }


    @Override
    public void onPlayStarted() {
        Log.w(getClass().getSimpleName(), "onPlayStarted");
        Toast.makeText(getActivity(), "Play started", Toast.LENGTH_LONG).show();
        webRTCClient.switchVideoScaling(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }

    @Override
    public void onPublishStarted() {
        Log.w(getClass().getSimpleName(), "onPublishStarted");
        Toast.makeText(getActivity(), "Publish started", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onPublishFinished() {
        Log.w(getClass().getSimpleName(), "onPublishFinished");
        Toast.makeText(getActivity(), "Publish finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPlayFinished() {
        Log.w(getClass().getSimpleName(), "onPlayFinished");
        Toast.makeText(getActivity(), "Play finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public void noStreamExistsToPlay() {
        Log.w(getClass().getSimpleName(), "noStreamExistsToPlay");
        Toast.makeText(getActivity(), "No stream exist to play", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onError(String description) {
        Toast.makeText(getActivity(), "Error: "  +description , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        super.onStop();
        webRTCClient.stopStream();

    }

    @Override
    public void onSignalChannelClosed(WebSocket.WebSocketConnectionObserver.WebSocketCloseNotification code) {
        Toast.makeText(getActivity(), "Signal channel closed with code " + code, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDisconnected() {

        Log.w(getClass().getSimpleName(), "disconnected");
        Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_LONG).show();

        finish();
    }

    @Override
    public void onConnected() {
        //it is called when connected to ice
    }


    public void onOffVideo(View view) {
        if (webRTCClient.isVideoOn()) {
            webRTCClient.disableVideo();
        }
        else {
            webRTCClient.enableVideo();
        }
    }

    public void onOffAudio(View view) {
        if (webRTCClient.isAudioOn()) {
            webRTCClient.disableAudio();
        }
        else {
            webRTCClient.enableAudio();
        }
    }

    @Override
    public void onTrackList(String[] tracks) {

    }

    private  void finish() {
        getActivity().getFragmentManager().popBackStack();
    }
}

