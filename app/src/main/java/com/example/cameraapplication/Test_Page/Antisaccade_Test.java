package com.example.cameraapplication.Test_Page;

import static android.content.ContentValues.TAG;
import static android.graphics.Bitmap.createBitmap;
import static androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.cameraapplication.Camera_Preview.FaceView;
import com.example.cameraapplication.R;
import com.example.cameraapplication.User_Interface.TestCompleted;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implement camera x preview view use case, image analysis use case
 * Write to Excel Files from data obtained
 */
public class Antisaccade_Test extends AppCompatActivity {
    // Test handler
    private Handler handler = new Handler();
    PreviewView previewView;
    FaceView faceView;
    LifecycleCameraController cameraController;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;

    // declare a boolean flag to indicate if trackIris is already running
    boolean isTrackIrisRunning;
    private ScheduledExecutorService executor;
    int eyeRegionWidth = 100;
    int eyeRegionHeight = 80;
    // Bitmap for passing eye region to OpenCV
    protected static Bitmap eBitmap;
    // Parameters for Iris Tracking
    double prevCenterX = 0;
    double prevCenterY = 0;
    final int MOVEMENT_THRESHOLD = 6; // in pixels
    final int STATIONARY_THRESHOLD = 4; // in pixels
    int TrialNum = 0;
    // Create a new XSSFWorkbook object
    XSSFWorkbook workbook = new XSSFWorkbook();
    // Create a new sheet
    XSSFSheet sheet = workbook.createSheet("Eye Movement Data");
    String firstName;
    String lastName;
    String email;
    int counter; //to set left or right
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_antisaccade_test);

        // Load Strings
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        email = getIntent().getStringExtra("email");

        // Set Property for reading and writing to excel files
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        // Load OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Unable to load OpenCV");
        } else {
            Log.i(TAG, "OpenCV loaded successfully");
        }

        // Delete prev eye movement data if it exists
        File file = new File(getExternalFilesDir(null), "eye_movement_data.xlsx");
        if (file.exists()) {
            file.delete();
        }

        // Get Circle
        ImageView circle = findViewById(R.id.circle);
        ImageView cross = findViewById(R.id.cross);
        cross.setVisibility(View.INVISIBLE);

        // Get the width of the screen
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Called when the animation starts
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Called when the animation ends
                circle.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Called when the animation repeats
            }
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Called when the animation starts
                cross.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Called when the animation ends
                counter +=1;
                if (counter % 2 == 0){
                    circle.setTranslationX(-600);
                    circle.setTranslationY(0);
                } else{
                    circle.setTranslationX(600);
                    circle.setTranslationY(0);
                }
                circle.startAnimation(fadeIn);
                cross.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Called when the animation repeats
            }
        });

        // Start test from right side
        counter = 0;
        cross.setVisibility(View.VISIBLE);
        circle.setTranslationX(-600);
        circle.setTranslationY(0);
        circle.startAnimation(fadeIn);

        // Initialize camera controller and preview view
        cameraController = new LifecycleCameraController(this);

        previewView = findViewById(R.id.preview_view_test);

        startCamera();

        cameraExecutor = Executors.newSingleThreadExecutor();

        // Post a delayed action to switch to the next layout after 40 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Create an intent to start the next activity
                Intent intent = new Intent(Antisaccade_Test.this, TestCompleted.class);
                intent.putExtra("firstName", firstName);
                intent.putExtra("lastName", lastName);
                intent.putExtra("email", email);
                startActivity(intent);

                // Finish the current activity
                finish();
            }
        }, 40000);
    }

    // Start camera preview in background to read data
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

        isTrackIrisRunning = false;

        cameraController.setImageAnalysisAnalyzer(
                ContextCompat.getMainExecutor(this),
                new MlKitAnalyzer(List.of(faceDetector), COORDINATE_SYSTEM_VIEW_REFERENCED,
                        ContextCompat.getMainExecutor(this), result -> {
                    // Do Face Detection
                    List<Face> faces = result.getValue(faceDetector);

                    // remove views if no face
                    previewView.removeView(faceView);

                    // Detect exactly 1 face only
                    if (faces != null && faces.size() == 1) {
                        // Log.d(TAG, "Number of faces detected: " + faces.size());

                        for (Face face : faces) {
                            // Create a Bitmap
                            eBitmap = previewView.getBitmap();
                            eBitmap = getEyeBitmap(face, eBitmap);

                            // Set the trackIris method to run every 1 second
                            if (!isTrackIrisRunning) {
                                executor = Executors.newSingleThreadScheduledExecutor();
                                executor.scheduleAtFixedRate(new Runnable() {
                                    @Override
                                    public void run() {
//                                        // Debugging
//                                        Handler handler = new Handler(Looper.getMainLooper());
//                                        Bitmap processedBitmap = trackIris(eBitmap);
//                                        handler.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                // Update the ImageView with the processed bitmap
//                                                // imageview.setImageBitmap(processedBitmap);
//                                            }
//                                        });
                                        trackIris(eBitmap);
                                    }
                                }, 0, 1, TimeUnit.SECONDS);
                                isTrackIrisRunning = true;
                            }
                        }
                    }
                })
        );
        cameraController.bindToLifecycle(this);
        previewView.setController(cameraController);
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

            Log.d(TAG, "Iris Centroid: (" + centerX + ", " + centerY + ")");
            String Movement = " ";
            String TrialType = getTrialType(TrialNum);
            TrialNum++;

            // check for movement or stationary position
            if (prevCenterX > 0 && prevCenterY > 0) { // check if we have previous centroid
                double deltaX = centerX - prevCenterX;
                double deltaY = centerY - prevCenterY;
                double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
                if (distance > MOVEMENT_THRESHOLD) {
                    if (deltaX > 0) {
                        Log.d(TAG, "Right movement detected");
                        if(TrialType.equals("Right")) {
                            Movement = "0";
                        }
                        else if(TrialType.equals("Left")){
                            Movement = "1";
                        }
                    } else {
                        Log.d(TAG, "Left movement detected");
                        if(TrialType.equals("Right")) {
                            Movement = "1";
                        }
                        else if(TrialType.equals("Left")){
                            Movement = "0";
                        }
                    }
                } else if (distance < STATIONARY_THRESHOLD) {
                    Log.d(TAG, "Eye is stationary");
                    Movement = "-1";
                }
            }

            // write to excel file
            logData(TrialType, Movement, centerX, centerY);

            // store current centroid for next frame
            prevCenterX = centerX;
            prevCenterY = centerY;
        }
        else { Log.d(TAG, "Iris Not Found"); }
        return bmp;
    }

    // Change between Right and Left
    private String getTrialType(int trialnum) {
        if (trialnum % 2 == 1) {
            return "Right";
        } else {
            return "Left";
        }
    }

    // Write data to an excel sheet
    private void logData(String Trialtype, String accuracy, double xPos, double yPos) {
        // Create or open the workbook
        XSSFWorkbook workbook = null;
        File file = new File(getExternalFilesDir(null), "eye_movement_data.xlsx");
        if (file.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(file);
                workbook = new XSSFWorkbook(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            workbook = new XSSFWorkbook();
        }

        // Get the first sheet
        XSSFSheet sheet = workbook.getSheet("Eye Movement Data");
        if (sheet == null) {
            sheet = workbook.createSheet("Eye Movement Data");
            addHeaderRow(sheet);
        }

        // Add the data to the sheet
        int lastRowNum = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRowNum + 1);
        Cell cell = null;
        cell = row.createCell(0);
        cell.setCellValue(Trialtype);
        cell = row.createCell(1);
        cell.setCellValue(accuracy);
        cell = row.createCell(2);
        cell.setCellValue(xPos);
        cell = row.createCell(3);
        cell.setCellValue(yPos);

        // Write the workbook to the file
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            Log.d(TAG, "Eye movement data saved to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error writing eye movement data to file.", e);
        }
    }

    // Add header row to the excel sheet
    private void addHeaderRow(XSSFSheet sheet) {
        Row row = sheet.createRow(0);
        Cell cell1 = row.createCell(0);
        Cell cell2 = row.createCell(1);
        Cell cell3 = row.createCell(2);
        Cell cell4 = row.createCell(3);
        cell1.setCellValue("Trial Type");
        cell2.setCellValue("Accuracy");
        cell3.setCellValue("X position");
        cell4.setCellValue("Y position");
    }

    // Destroy the activity if user goes back
    @Override
    public void onBackPressed() {
        // return to previous instance of activity
        super.onBackPressed();
        finish();
    }

    // Shutdown executors/ handlers
    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        cameraController.unbind();
        cameraExecutor.shutdown();
        faceDetector.close();
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }
}
