package com.example.trevor.listtothemax;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.Float;
import java.util.Iterator;
import java.util.List;


public class PhoneActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OPENCV";
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
        }
        else {
            Log.d("SUCCESS", "OpenCV loaded");
        }
    }
    private CameraBridgeViewBase mOpenCvCameraView;
    Context context;
    Bitmap bitmap= null;
    Rect maxRet=new Rect(0,0,0,0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Required code to set view
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.opencv);
        context=this;
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Log.i(TAG, metrics.widthPixels+" "+metrics.heightPixels);
        mOpenCvCameraView.setMaxFrameSize(metrics.heightPixels,metrics.widthPixels);
        mOpenCvCameraView.setVisibility( SurfaceView.VISIBLE );
        mOpenCvCameraView.setCvCameraViewListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_phone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onClickAdd(View v) {
            Log.i(TAG, "ONCLICK");
        mOpenCvCameraView.disableView();
        try {
            //Write file
            String filename = "bitmap.png";
            FileOutputStream stream = this.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

            //Cleanup
            stream.close();

            //Pop intent
            Intent in1 = new Intent();
            in1.putExtra("image", filename);
            setResult(Activity.RESULT_OK,in1);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onPause()
    {
        Log.i(TAG, "ONPAUSE");
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        Log.i(TAG, "ONDESTROY");
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final double MIN_AREA=0.1;
        final double SCREEN_SIZE = inputFrame.gray().width()*inputFrame.gray().height();
        MatOfPoint2f thisContour2f = new MatOfPoint2f();
        MatOfPoint approxContour = new MatOfPoint();
        MatOfPoint2f approxContour2f = new MatOfPoint2f();
        Scalar CONTOUR_COLOR = new Scalar(255,0,0,255);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
        List<Rect> rectangles = new ArrayList<Rect>();
        List<Rect> mRectangles = new ArrayList<Rect>();
        Mat mHierarchy = new Mat();
        Mat mRgba = inputFrame.rgba();
        Mat mIntermediateMat = inputFrame.gray();
        maxRet=new Rect(0,0,0,0);
        Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
        Imgproc.findContours(mIntermediateMat, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        Iterator<MatOfPoint> each = contours.iterator();


        while(each.hasNext()){
            Rect ret =null;
            MatOfPoint thisContour =each.next();
            thisContour.convertTo(thisContour2f, CvType.CV_32FC2);
            Imgproc.approxPolyDP(thisContour2f, approxContour2f, 10, true);
            approxContour2f.convertTo(approxContour, CvType.CV_32S);
            ret = Imgproc.boundingRect(approxContour);
            rectangles.add(ret);
            if(ret.area()>maxRet.area())
            {
                maxRet=ret;
            }
        }

        /*Iterator<Rect> rects = rectangles.iterator();
        while (rects.hasNext()) {
            Rect ret = rects.next();
            if (ret.area() > MIN_AREA*SCREEN_SIZE) {
                //mRectangles.add(ret);
                Core.rectangle(mRgba, new Point(ret.x,ret.y),new Point(ret.x+ret.width,ret.y+ret.height), new Scalar(255,0,0,255),3);
            }
        }*/
        if(maxRet.area()>0) {
            Core.rectangle(mRgba, new Point(maxRet.x, maxRet.y), new Point(maxRet.x + maxRet.width, maxRet.y + maxRet.height), new Scalar(255, 0, 0, 255), 3);
            Mat ROI = mRgba.submat(maxRet.y, maxRet.y + maxRet.height, maxRet.x, maxRet.x + maxRet.width);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(ROI.cols(), ROI.rows(), Bitmap.Config.ARGB_8888);
            }
            else
            {
                bitmap.recycle();
                bitmap = Bitmap.createBitmap(ROI.cols(), ROI.rows(), Bitmap.Config.ARGB_8888);
            }
            Utils.matToBitmap(ROI, bitmap);
            //Imgproc.drawContours(mRgba, mContours, -1, CONTOUR_COLOR);
        }

        return mRgba;
    }


    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Create and set View

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }

    };

    /** Call on every application resume **/
    @Override
    protected void onResume()
    {
        Log.i(TAG, "Called onResume");
        super.onResume();

        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_10, this, mOpenCVCallBack))
        {

            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }

        mOpenCVCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);
    }



}