package com.example.cameraapplication.User_Interface;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cameraapplication.Camera_Preview.CameraXPreview;
import com.example.cameraapplication.R;

public class UserForm extends AppCompatActivity {
    String firstName;
    String lastName;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userform);

        ImageView imageViewLogo = findViewById(R.id.image_view_logo);
        ObjectAnimator objectAnimatorY1 = ObjectAnimator.ofFloat(imageViewLogo, "translationY", -200f, 0f);
        objectAnimatorY1.setDuration(1000);
        objectAnimatorY1.start();

        ImageView imageViewLogo1 = findViewById(R.id.image_view_logo1);
        ObjectAnimator objectAnimatorY2 = ObjectAnimator.ofFloat(imageViewLogo1, "translationY", -200f, 0f);
        objectAnimatorY2.setDuration(1000);
        objectAnimatorY2.start();

        TextView textView1 = findViewById(R.id.text_view_1);
        ObjectAnimator objectAnimatorAlpha = ObjectAnimator.ofFloat(textView1, "alpha", 0f, 1f);
        objectAnimatorAlpha.setDuration(2000);
        objectAnimatorAlpha.start();

        TextView textView2 = findViewById(R.id.text_view_2);
        float startX = -textView2.getWidth(); // start from off-screen left
        float endX = 0; // end at the original position
        textView2.setTranslationX(startX); // set initial position

        ObjectAnimator objectAnimatorAlpha2 = ObjectAnimator.ofFloat(textView2, "alpha", 0f, 1f);
        ObjectAnimator objectAnimatorScaleX2 = ObjectAnimator.ofFloat(textView2, "scaleX", 0f, 1f);
        ObjectAnimator objectAnimatorTranslateX2 = ObjectAnimator.ofFloat(textView2, "translationX", startX, endX);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimatorAlpha2, objectAnimatorScaleX2, objectAnimatorTranslateX2);
        animatorSet.setDuration(1600);
        animatorSet.start();

        Button Next = findViewById(R.id.Next);

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Next();
            }
        });
    }

    private void Next() {
        EditText firstNameEditText = findViewById(R.id.firstNameEditText);
        firstName = firstNameEditText.getText().toString();
        EditText lastNameEditText = findViewById(R.id.lastNameEditText);
        lastName = lastNameEditText.getText().toString();
        EditText emailEditText = findViewById(R.id.emailEditText);
        email = emailEditText.getText().toString();
        Intent intent = new Intent(this, CameraXPreview.class);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }
}
