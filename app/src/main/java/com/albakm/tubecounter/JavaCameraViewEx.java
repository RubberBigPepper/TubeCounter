package com.albakm.tubecounter;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

public class JavaCameraViewEx extends JavaCameraView {
    public JavaCameraViewEx(Context context, int cameraId) {
        super(context, cameraId);
    }

    public JavaCameraViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void turnFlash(boolean bOn) {
        Camera.Parameters params = mCamera.getParameters();

        params.setFlashMode(bOn?params.FLASH_MODE_TORCH:params.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }
}
