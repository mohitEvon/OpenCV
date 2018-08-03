package com.example.evon.opencvtest;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final int REQUEST_PERMISSION = 100;
    private int w, h;
    private static  Scalar CONTOUR_COLOR =null;
    private static double areaThreshold = 0.025; //threshold for the area size of an object
    private static final String TAG = "MainActivity";
    private Mat mGrey, mRgba;
    private CameraBridgeViewBase mOpenCvCameraView;
    Button FocusButton;
    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mGrey = inputFrame.gray();
        mRgba = inputFrame.rgba();

        detectObject();

        return mRgba;
    }


   /* private void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.enableView();
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        img1 = new Mat();
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open("a.jpeg");
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        Utils.bitmapToMat(bitmap, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        img1.convertTo(img1, 0); //converting the image to match with the type of the cameras image
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

    }*/


    public MainActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }


        //FULLSCREEN MODE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        FocusButton = (Button) findViewById(R.id.FocusButton);
        FocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOpenCvCameraView.requestFocus();
                    }
                });

                Log.d(TAG,"Focus requested!!");
            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        w = width;
        h = height;
    }

    public void onCameraViewStopped() {
    }




    private void detectObject() {

        CONTOUR_COLOR = new Scalar(255);

        final MatOfKeyPoint keypoint = new MatOfKeyPoint();
        final List<KeyPoint> listpoint;
        KeyPoint kpoint;
        Mat mask = Mat.zeros(mGrey.size(), CvType.CV_8UC1);
        int rectanx1,rectany1,rectanx2,rectany2;
        int imgsize = mGrey.height() * mGrey.width();
        Scalar zeos = new Scalar(0, 0, 0);

        List<MatOfPoint> contour2 = new ArrayList<MatOfPoint>();
        Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
        Mat morbyte = new Mat();
        Mat hierarchy = new Mat();

        Rect rectan3;

        FeatureDetector detector = FeatureDetector
                .create(FeatureDetector.FAST);

        detector.detect(mGrey, keypoint);

        listpoint= keypoint.toList();


        for (int ind = 0; ind < listpoint.size(); ind++) {
            kpoint = listpoint.get(ind);
            rectanx1 = (int) (kpoint.pt.x - 0.5 * kpoint.size);
            rectany1 = (int) (kpoint.pt.y - 0.5 * kpoint.size);
            rectanx2 = (int) (kpoint.size);
            rectany2 = (int) (kpoint.size);
            if (rectanx1 <= 0)
                rectanx1 = 1;
            if (rectany1 <= 0)
                rectany1 = 1;
            if ((rectanx1 + rectanx2) > mGrey.width())
                rectanx2 = mGrey.width() - rectanx1;
            if ((rectany1 + rectany2) > mGrey.height())
                rectany2 = mGrey.height() - rectany1;
            Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
            try {
                Mat roi = new Mat(mask, rectant);
                roi.setTo(CONTOUR_COLOR);
            } catch (Exception ex) {
                Log.d("mylog", "mat roi error " + ex.getMessage());
            }
        }


        Imgproc.morphologyEx(mask, morbyte, Imgproc.MORPH_DILATE, kernel);
        Imgproc.findContours(morbyte, contour2, hierarchy,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        for (int ind = 0; ind < contour2.size(); ind++) {
            rectan3 = Imgproc.boundingRect(contour2.get(ind));

            if(rectan3.area()<imgsize*areaThreshold){
                continue;
            }

            Bitmap bmp=null;
            try {
                Mat croppedPart;
                croppedPart = new Mat(mGrey,rectan3);
                bmp = Bitmap.createBitmap(croppedPart.width(), croppedPart.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(croppedPart, bmp);
                Log.d(TAG,"Cropping Successful");
            } catch (Exception e) {
                Log.d(TAG, "cropped part data error " + e.getMessage());
            }


            if (rectan3.area() > 0.5 * imgsize || rectan3.area() < 100
                    || rectan3.width / rectan3.height < 2) {
                Mat roi = new Mat(morbyte, rectan3);
                roi.setTo(zeos);

            }
            else
            {    Imgproc.rectangle(mRgba, rectan3.br(), rectan3.tl(),
                    CONTOUR_COLOR);
            }

            if (bmp != null) {
                Log.d(TAG,"bitmap found!!");
            }
        }
    }
}