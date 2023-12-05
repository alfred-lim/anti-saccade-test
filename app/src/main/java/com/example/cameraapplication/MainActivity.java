package com.example.cameraapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cameraapplication.User_Interface.UserForm;

public class MainActivity extends AppCompatActivity {
    private static final String CAMERA_PERMISSION = android.Manifest.permission.CAMERA;
    private static final String AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO;
    private static final String STORAGE_PERMISSION = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final int CAMERA_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageViewLogo = findViewById(R.id.image_view_logo);
        ObjectAnimator objectAnimatorY = ObjectAnimator.ofFloat(imageViewLogo, "translationY", -200f, 0f);
        objectAnimatorY.setDuration(800);
        objectAnimatorY.start();

        TextView textView1 = findViewById(R.id.text_view_1);
        ObjectAnimator objectAnimatorAlpha = ObjectAnimator.ofFloat(textView1, "alpha", 0f, 1f);
        objectAnimatorAlpha.setDuration(1400);
        objectAnimatorAlpha.start();

        Button getStarted = findViewById(R.id.getStarted);
        ObjectAnimator objectAnimatorX = ObjectAnimator.ofFloat(getStarted, "translationX", -200f, 0f);
        objectAnimatorX.setDuration(1200);
        objectAnimatorX.start();

        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermissions()) {
                    getStarted();
                } else {
                    requestPermissions();
                }
            }
        });
    }

    private boolean hasCameraPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    //request permission for camera
    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{CAMERA_PERMISSION, AUDIO_PERMISSION, STORAGE_PERMISSION},
                CAMERA_REQUEST_CODE
        );
    }

    private void getStarted() {
        Intent intent = new Intent(this, UserForm.class);
        startActivity(intent);
    }
}