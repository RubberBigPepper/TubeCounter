package com.albakm.tubecounter;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.List;

public class JavaCameraViewEx extends JavaCameraView implements Camera.AutoFocusCallback {
    private static  final int FOCUS_AREA_SIZE= 300;

    public JavaCameraViewEx(Context context, int cameraId) {
        super(context, cameraId);
    }

    public JavaCameraViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setFpsRange(){
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange(30000, 30000);
        mCamera.setParameters(params);
    }

    public void turnFlash(boolean bOn) {
        Camera.Parameters params = mCamera.getParameters();

        params.setFlashMode(bOn?params.FLASH_MODE_TORCH:params.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }

    public void focusOnTouch(MotionEvent event) {
        if (mCamera != null ) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0){
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 1000));
                parameters.setFocusAreas(meteringAreas);

                mCamera.setParameters(parameters);
                mCamera.autoFocus(this);
            }else {
                mCamera.autoFocus(this);
            }
        }
    }

    public void resetFocus(){
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getMaxNumMeteringAreas() > 0){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            /*List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
            Rect rect =new Rect(-1000,-1000,1000,1000);
            meteringAreas.add(new Camera.Area(rect, 1000));*/
            parameters.setFocusAreas(null);
            mCamera.setParameters(parameters);
            mCamera.autoFocus(this);
        }else {
            mCamera.autoFocus(this);
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {

    }
}
