package com.example.cameraapplication.Camera_Preview;

import static android.content.ContentValues.TAG;
import static android.graphics.Bitmap.createBitmap;
import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.cameraapplication.Test_Page.Antisaccade_Test;
import com.example.cameraapplication.R;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraXPreview extends AppCompatActivity {
    PreviewView previewView;
    LifecycleCameraController cameraController;
    Button Start_Test;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    FaceView faceView;
    ImageView imageview;
    TextView eyeDirectionTextView;
    TextView eyeDirectionTextView2;
    int eyeRegionWidth = 100;
    int eyeRegionHeight = 80;
    protected static Bitmap eBitmap;
    String firstName;
    String lastName;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);

        // Load Strings
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        email = getIntent().getStringExtra("email");
        // Log.d(TAG, firstName + lastName + email);

        // Load OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Unable to load OpenCV");
        } else {
            Log.i(TAG, "OpenCV loaded successfully");
        }

        // Set Camera
        previewView = findViewById(R.id.preview_view);

        startCamera();

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Set start test button (Invisible)
        Start_Test = findViewById(R.id.Start_Test);

        // Set button listener
        Start_Test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartTest();
            }
        });
    }

    private void startCamera() {
    // Create FaceView instance
    faceView = new FaceView(this);

    cameraController = new LifecycleCameraController(this);

    // Camera Selector Use Case
    CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build();

    cameraController.setCameraSelector(cameraSelector);

    // Initialize Preview use case
    Preview preview = new Preview.Builder().build();
    preview.setSurfaceProvider(previewView.getSurfaceProvider());

    // Initialize Face Detector
    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .build();
    // Create a FaceDetector object with the options
    faceDetector = FaceDetection.getClient(options);

    cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            new MlKitAnalyzer(List.of(faceDetector), COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this), result -> {
                // Do Face Detection
                List<Face> faces = result.getValue(faceDetector);
                // remove views if no face
                previewView.removeView(faceView);
                previewView.removeView(eyeDirectionTextView);
                previewView.removeView(eyeDirectionTextView2);
                previewView.removeView(imageview);

                // Detect exactly 1 face only
                if (faces != null && faces.size() == 1) {
                    // Log.d(TAG, "Number of faces detected: " + faces.size());

                    // Set Start Test Button Visible
                    Start_Test.setVisibility(View.VISIBLE);
                    for (Face face : faces) {
                        // Create a Bitmap
                        eBitmap = previewView.getBitmap();
                        eBitmap = getEyeBitmap(face, eBitmap);

                        trackIris(eBitmap);

                        imageview = new ImageView(this);
                        FrameLayout.LayoutParams params = GetLayout();
                        // Get Bitmap Preview
                        imageview.setImageBitmap(eBitmap);
                        previewView.addView(imageview, params);

                        // Get the bounding box of the face
                        RectF faceRect = new RectF(face.getBoundingBox());
                        faceView.setFaceRect(faceRect);
                        faceView.setFace(face);

                        // Remove the view from its current parent, if it has one
                        if (faceView.getParent() != null) {
                            ((ViewGroup) faceView.getParent()).removeView(faceView);
                        }

                        // Add Face Drawing to Preview
                        previewView.addView(faceView);
                    }
            } else { Start_Test.setVisibility(View.INVISIBLE); }
        })
    );
    cameraController.bindToLifecycle(this);
    previewView.setController(cameraController);
    }

    public FrameLayout.LayoutParams GetLayout(){
        FrameLayout parentLayout = findViewById(R.id.preview_view);
        // Create a new LayoutParams object for the ImageView
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        // Set the position of the ImageView
        params.gravity = Gravity.TOP | Gravity.START; // change these values to position the ImageView as needed

        // Set the size of the ImageView
        params.width = 400; // change this value to set the width of the ImageView
        params.height = 400; // change this value to set the height of the ImageView

        return params;
    }

    // Create Bitmap for eye region
    protected Bitmap getEyeBitmap(Face face, Bitmap eBitmap){
        FaceLandmark leftEyeLandmark = face.getLandmark(FaceLandmark.LEFT_EYE);
        if(leftEyeLandmark != null && eBitmap != null) {
            int eye_region_left = (int) leftEyeLandmark.getPosition().x - eyeRegionWidth /2;
            int eye_region_top = (int) leftEyeLandmark.getPosition().y - eyeRegionHeight /2;

            // Get the dimensions of the bitmap
            int bitmapWidth = eBitmap.getWidth();
            int bitmapHeight = eBitmap.getHeight();

            // Adjust the eye region to fit bitmap
            if (eye_region_left < 0) {
                eye_region_left = 0;
            } else if (eye_region_left + eyeRegionWidth > bitmapWidth) {
                eye_region_left = bitmapWidth - eyeRegionWidth;
            }

            if (eye_region_top < 0) {
                eye_region_top = 0;
            } else if (eye_region_top + eyeRegionHeight > bitmapHeight) {
                eye_region_top = bitmapHeight - eyeRegionHeight;
            }
            return createBitmap(eBitmap,
                    eye_region_left,
                    eye_region_top,
                    eyeRegionWidth, eyeRegionHeight);
        }
        // return blank bitmap if null
        return Bitmap.createBitmap(eyeRegionWidth, eyeRegionHeight, Bitmap.Config.ARGB_8888);
    }

    // Convert bitmap to grayscale
    protected Bitmap toGrayscale(Bitmap bmp){
        Bitmap grayscale = createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(grayscale);
        Paint paint=new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmp, 0, 0, paint);
        return grayscale;
    }

    double prevCenterX = 0;
    double prevCenterY = 0;
    final int MOVEMENT_THRESHOLD = 10; // in pixels
    final int STATIONARY_THRESHOLD = 5; // in pixels
    int TrialCount = 0;

    // pass bitmap of eye region into OpenCV methods for eye tracking
    public Bitmap trackIris(Bitmap bmp) {
        // return null if null bitmap
        if (bmp == null) {
            return null;
        }
        Mat eyeMat = new Mat();
        Utils.bitmapToMat(bmp, eyeMat);

        // convert the eye image to grayscale
        Imgproc.cvtColor(eyeMat, eyeMat, Imgproc.COLOR_BGR2GRAY);

        // apply thresholding to segment the iris from the background
        Imgproc.adaptiveThreshold(eyeMat, eyeMat,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);

        // apply morphological operations to remove noise and smooth the iris boundary
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Imgproc.erode(eyeMat, eyeMat, kernel);
        Imgproc.dilate(eyeMat, eyeMat, kernel);

        // extract the edges of the iris using Canny edge detection
        Imgproc.Canny(eyeMat, eyeMat, 120, 220);
        Imgproc.GaussianBlur(eyeMat, eyeMat, new Size(9, 9), 2, 2);

        // find the contours of the iris region
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(eyeMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // find the largest contour, which is the iris region
        int maxContourIdx = -1;
        double maxContourArea = 0;
        for (int i = 0; i < contours.size(); i++) {
            double contourArea = Imgproc.contourArea(contours.get(i));
            if (contourArea > maxContourArea) {
                maxContourArea = contourArea;
                maxContourIdx = i;
            }
        }

        // calculate the centroid of the iris contour
        if (maxContourIdx >= 0) {
            Moments moments = Imgproc.moments(contours.get(maxContourIdx));
            double centerX = 0;
            double centerY = 0;
            if (moments.get_m00() != 0) {
                centerX = moments.get_m10() / moments.get_m00();
                centerY = moments.get_m01() / moments.get_m00();
            }

            // store current centroid for next frame
            prevCenterX = centerX;
            prevCenterY = centerY;

            // Get canvas from bitmap
            Canvas canvas = new Canvas(bmp);

            // Draw circle around the iris centroid on canvas
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            canvas.drawCircle((float) centerX, (float) centerY, (float) 30, paint);
        }
        else { Log.d(TAG, "Iris Not Found"); }
        return bmp;
    }

    // Start the Anti-Saccade Test
    private void StartTest(){
        Intent intent = new Intent(this, Antisaccade_Test.class);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("email", email);
        startActivity(intent);
        finish(); // destroy the current activity
    }

    // Destroy the activity if user goes back
    @Override
    public void onBackPressed() {
        // return to previous instance of activity
        super.onBackPressed();
        finish();
    }

    // Shutdown
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        faceDetector.close();
    }
}
