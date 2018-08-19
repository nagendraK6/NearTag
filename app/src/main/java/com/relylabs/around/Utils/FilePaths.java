package com.relylabs.around.Utils;

import android.os.Environment;

/**
 * Created by nagendra on 8/18/18.
 */

public class FilePaths {

    //"storage/emulated/0"
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String PICTURES = ROOT_DIR + "/";
    public String CAMERA = ROOT_DIR + "/DCIM/camera";

}
