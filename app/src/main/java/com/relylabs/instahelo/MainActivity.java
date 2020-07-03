package com.relylabs.instahelo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.amplitude.api.Amplitude;
import com.relylabs.instahelo.R;
import com.relylabs.instahelo.camera.Camera2Fragment;
import com.relylabs.instahelo.camera.IMainActivity;
import com.relylabs.instahelo.camera.ViewStickersFragment;
import com.relylabs.instahelo.models.User;
import com.relylabs.instahelo.newsfeed.StoryFeedFragment;
import com.relylabs.instahelo.registration.GetStartedFragment;
import com.relylabs.instahelo.registration.PhoneVerificationFragment;

public class MainActivity extends AppCompatActivity implements IMainActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1234;
    public static String CAMERA_POSITION_FRONT;
    public static String CAMERA_POSITION_BACK;
    public static String MAX_ASPECT_RATIO;

    //widgets

    //vars
    private boolean mPermissions = true;
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

        return new NewsFeedFragment();
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

    private void startCamera2(){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_holder, StoryFeedFragment.newInstance(), getString(R.string.fragment_camera2));
        transaction.commit();
    }

    private void init(){
        if(mPermissions){
            if(checkCameraHardware(this)){

                // Open the Camera
                startCamera2();
            }
            else{
                showSnackBar("You need a camera to use this application", Snackbar.LENGTH_INDEFINITE);
            }
        }
        else{
           // verifyPermissions();
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        // this device has a camera
// no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /*public void verifyPermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED) {
            mPermissions = true;
            init();
        } else {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE){
            if(mPermissions){
                init();
            }
            else{
                verifyPermissions();
            }
        }
    }*/


    private void showSnackBar(final String text, final int length) {
        View view = this.findViewById(android.R.id.content).getRootView();
        Snackbar.make(view, text, length).show();
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
        return mCameraOrientation.equals(CAMERA_POSITION_FRONT);
    }

    @Override
    public boolean isCameraBackFacing() {
        return mCameraOrientation.equals(CAMERA_POSITION_BACK);
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
