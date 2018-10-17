package com.neartag.in;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amplitude.api.Amplitude;
import com.neartag.in.R;
import com.neartag.in.camera.Camera2Fragment;
import com.neartag.in.camera.IMainActivity;
import com.neartag.in.camera.ViewStickersFragment;
import com.neartag.in.models.User;
import com.neartag.in.newsfeed.StoryFeedFragment;
import com.neartag.in.registration.GetStartedFragment;
import com.neartag.in.registration.PhoneVerificationFragment;

public class MainActivity extends AppCompatActivity implements IMainActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1234;
    public static String CAMERA_POSITION_FRONT;
    public static String CAMERA_POSITION_BACK;
    public static String MAX_ASPECT_RATIO;

    //widgets

    //vars
    private boolean mPermissions;
    public String mCameraOrientation = "none"; // Front-facing or back-facing

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user =  User.getLoggedInUser();
        if (user != null) {
            Amplitude.getInstance().initialize(
                    getApplicationContext(),
                    "fcb10b1e728780e9d9bae42ac53d16a4",
                    Integer.toString(user.UserID)
            ).enableForegroundTracking(getApplication()
            );
        } else {
            Amplitude.getInstance().initialize(
                    getApplicationContext(),
                    "fcb10b1e728780e9d9bae42ac53d16a4").enableForegroundTracking(getApplication()
            );
        }
        setContentView(R.layout.activity_main);
        setUpFragment(findFragment());
    }

    private void setUpFragment(Fragment fragment_to_start) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_holder, fragment_to_start);
        ft.commitAllowingStateLoss();
    }

    private Fragment findFragment() {
        User user = User.getLoggedInUser();
        if (user == null) {
            return new GetStartedFragment();
        }

        if (user.IsOTPVerified == false) {
            return new PhoneVerificationFragment();
        }

        return new StoryFeedFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment uploadType = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (uploadType != null) {
            uploadType.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void setCameraFrontFacing() {
        Log.d(TAG, "setCameraFrontFacing: setting camera to front facing.");
        mCameraOrientation = CAMERA_POSITION_FRONT;
    }

    @Override
    public void setCameraBackFacing() {
        Log.d(TAG, "setCameraBackFacing: setting camera to back facing.");
        mCameraOrientation = CAMERA_POSITION_BACK;
    }

    @Override
    public void setFrontCameraId(String cameraId){
        CAMERA_POSITION_FRONT = cameraId;
    }


    @Override
    public void setBackCameraId(String cameraId){
        CAMERA_POSITION_BACK = cameraId;
    }

    @Override
    public boolean isCameraFrontFacing() {
        if(mCameraOrientation.equals(CAMERA_POSITION_FRONT)){
            return true;
        }
        return false;
    }

    @Override
    public boolean isCameraBackFacing() {
        if(mCameraOrientation.equals(CAMERA_POSITION_BACK)){
            return true;
        }
        return false;
    }

    @Override
    public String getBackCameraId(){
        return CAMERA_POSITION_BACK;
    }

    @Override
    public String getFrontCameraId(){
        return CAMERA_POSITION_FRONT;
    }

    @Override
    public void hideStatusBar() {

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void showStatusBar() {

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void hideStillshotWidgets() {
        Camera2Fragment camera2Fragment = (Camera2Fragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_camera2));
        if (camera2Fragment != null) {
            if(camera2Fragment.isVisible()){
                camera2Fragment.drawingStarted();
            }
        }
    }

    @Override
    public void showStillshotWidgets() {
        Camera2Fragment camera2Fragment = (Camera2Fragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_camera2));
        if (camera2Fragment != null) {
            if(camera2Fragment.isVisible()){
                camera2Fragment.drawingStopped();
            }
        }
    }

    @Override
    public void toggleViewStickersFragment(){

        ViewStickersFragment viewStickersFragment
                = (ViewStickersFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_view_stickers));
        if (viewStickersFragment != null) {
            if(viewStickersFragment.isVisible()){
                hideViewStickersFragment(viewStickersFragment);
            }
            else{
                showViewStickersFragment(viewStickersFragment);
            }
        }
        else{
            inflateViewStickersFragment();
        }
    }

    private void hideViewStickersFragment(ViewStickersFragment fragment){

        showStillshotWidgets();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
        transaction.hide(fragment);
        transaction.commit();
    }

    private void showViewStickersFragment(ViewStickersFragment fragment){

        hideStillshotWidgets();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
        transaction.show(fragment);
        transaction.commit();
    }

    private void inflateViewStickersFragment(){

        hideStillshotWidgets();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
        transaction.add(R.id.fragment_holder, ViewStickersFragment.newInstance(), getString(R.string.fragment_view_stickers));
        transaction.commit();
    }

    @Override
    public void addSticker(Drawable sticker){
        Camera2Fragment camera2Fragment = (Camera2Fragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_camera2));
        if (camera2Fragment != null) {
            if(camera2Fragment.isVisible()){
                camera2Fragment.addSticker(sticker);
            }
        }
    }

    @Override
    public void setTrashIconSize(int width, int height){
        Camera2Fragment camera2Fragment = (Camera2Fragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_camera2));
        if (camera2Fragment != null) {
            if(camera2Fragment.isVisible()){
                camera2Fragment.setTrashIconSize(width, height);
            }
        }
    }

    public void dragStickerStarted(){
        Camera2Fragment camera2Fragment = (Camera2Fragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_camera2));
        if (camera2Fragment != null) {
            if(camera2Fragment.isVisible()){
                camera2Fragment.dragStickerStarted();
            }
        }
    }

    public void dragStickerStopped(){
        Camera2Fragment camera2Fragment = (Camera2Fragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_camera2));
        if (camera2Fragment != null) {
            if(camera2Fragment.isVisible()){
                camera2Fragment.dragStickerStopped();
            }
        }
    }
}
