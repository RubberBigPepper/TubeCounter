package com.albakm.tubecounter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, View.OnClickListener{
    private JavaCameraView mOpenCvCameraView;
    private static final int PERMISSION_REQUEST_CODE = 1253;
    private static final int AVERAGE_COUNT = 10;//сколько будем помнить шагов для усреднения

    private SeekBar seekBarMinRadius;
    private SeekBar seekBarMaxRadius;
    private SeekBar seekBarMinDistance;

    private TextView textViewMinRadius;
    private TextView textViewMaxRadius;
    private TextView textViewMinDistance;

    private Button btnSnapShot;

    private Mat matRGBASnap=null;//последний кадр с камеры, для режима стоп-кадр
    private boolean snapShotMode=false;//режим показа последнего кадра

    private LinkedList<Integer> counterCircle=new LinkedList<>();//количество кругов на предыдущих шагах для усреднения.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        CheckPermissionsOrRun();
    }

    void ShowWindowContent(){
        setContentView(R.layout.activity_main);
        seekBarMinRadius=(SeekBar)findViewById(R.id.seekMinRadius);
        seekBarMaxRadius=(SeekBar)findViewById(R.id.seekMaxRadius);
        seekBarMinDistance=(SeekBar)findViewById(R.id.seekMinDistance);
        textViewMinRadius=(TextView)findViewById(R.id.textViewMinRadius);
        textViewMaxRadius=(TextView)findViewById(R.id.textViewMaxRadius);
        textViewMinDistance=(TextView)findViewById(R.id.textViewMinDistance);
        btnSnapShot=(Button)findViewById(R.id.buttonSnapshot);
        SharedPreferences cPrefs=getSharedPreferences("common",MODE_PRIVATE);
        seekBarMinRadius.setProgress(cPrefs.getInt("minRadius",seekBarMinRadius.getProgress()));
        seekBarMaxRadius.setProgress(cPrefs.getInt("maxRadius",seekBarMaxRadius.getProgress()));
        seekBarMinDistance.setProgress(cPrefs.getInt("minDistance",seekBarMinDistance.getProgress()));
        showCounts.run();
        btnSnapShot.setOnClickListener(this);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setPadding(0,0,0,0);
        OpenCVLoader.initDebug();
        mOpenCvCameraView.setMaxFrameSize(1280,720);
        mOpenCvCameraView.enableView();
    }

    private void CheckPermissionsOrRun() {
        ArrayList<String> strArNeedPermission = new ArrayList<String>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            strArNeedPermission.add(Manifest.permission.CAMERA);
        if (strArNeedPermission.size() > 0) {
            //спрашиваем пермишен у пользователя
            requestPermission(strArNeedPermission);
        } else {
            ShowWindowContent();
        }
    }

    public void requestPermission(ArrayList<String> strArNeedPermission) {
        String[] strArList = new String[strArNeedPermission.size()];
        strArNeedPermission.toArray(strArList);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, strArList, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0) {
            boolean bCamera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
            for (int n = 0; n < permissions.length; n++) {
                if (grantResults[n] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[n].equals(Manifest.permission.CAMERA))
                        bCamera = true;
                }
            }
            if (bCamera) {
                ShowWindowContent();
            } else {
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        SharedPreferences.Editor cPrefs=getSharedPreferences("common",MODE_PRIVATE).edit();
        cPrefs.putInt("minRadius",seekBarMinRadius.getProgress());
        cPrefs.putInt("maxRadius",seekBarMaxRadius.getProgress());
        cPrefs.putInt("minDistance",seekBarMinDistance.getProgress());
        cPrefs.commit();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        if (!snapShotMode || matRGBASnap == null) {
            matRGBASnap = inputFrame.rgba();
        }
        return makeCirclesOnFrame(matRGBASnap.clone());
    }

    private Mat makeCirclesOnFrame(Mat rgbaFrame){
        Mat grayFrame=new Mat();
        Imgproc.cvtColor(rgbaFrame,grayFrame,Imgproc.COLOR_RGBA2GRAY);
        Imgproc.medianBlur(grayFrame, grayFrame, 5);
        Mat circles = new Mat();
        double minDistance=seekBarMinDistance.getProgress()+1.0d;
        int minRadius=seekBarMinRadius.getProgress()+1;
        int maxRadius=seekBarMaxRadius.getProgress()+1;
        Imgproc.HoughCircles(grayFrame, circles, Imgproc.HOUGH_GRADIENT, 1.0,
                minDistance, // change this value to detect circles with different distances to each other
                100.0, 30.0, minRadius, maxRadius); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles
        for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(rgbaFrame, center, radius, new Scalar(255,0,255), 3, 8, 0 );
        }
        counterCircle.add(circles.cols());
        while (counterCircle.size()>AVERAGE_COUNT)
            counterCircle.remove(0);
        runOnUiThread(showCounts);
        return rgbaFrame;
    }

    private Runnable showCounts=new Runnable() {
        @Override
        public void run() {
            float fCount=0.0f;
            for(int n=0;n<counterCircle.size();n++){
                fCount+=counterCircle.get(n);
            }
            if(counterCircle.size()>0)
                fCount/=counterCircle.size();
            int nCount=(int)(fCount+0.5f);
            setTitle(String.format(Locale.US, "%s - %d шт.",getString(R.string.app_name),nCount));
            textViewMinRadius.setText(String.format(Locale.US,"%s: %d",getString(R.string.minRaduis),seekBarMinRadius.getProgress()+1));
            textViewMaxRadius.setText(String.format(Locale.US,"%s: %d",getString(R.string.maxRaduis),seekBarMaxRadius.getProgress()+1));
            textViewMinDistance.setText(String.format(Locale.US,"%s: %d",getString(R.string.minDistance),seekBarMinDistance.getProgress()+1));
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonSnapshot:
                snapShotMode=!snapShotMode;
                btnSnapShot.setText(snapShotMode?R.string.continuePreview:R.string.takeSnapShot);
                break;
        }
    }
}
