package com.example.cameraapplication.Camera_Preview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

public class FaceView extends View {
    private RectF rect = new RectF();
    private Face face;
    private Paint paint = new Paint();
    private Paint textpaint = new Paint();

    private PointF point;
    public FaceView(Context context) {
        super(context);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFaceRect(RectF rect) {
        this.rect = rect;
        invalidate();
    }

    public void setFace(Face face) {
        this.face = face;
        invalidate();
    }

    public void setCircle(PointF point){
        this.point = point;
    }

    public void setLeft(Boolean Left){
        invalidate();
    }

    public void setRight(Boolean Right){
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);

        // Get the dimensions of the view
        int width = getWidth();
        int height = getHeight();

        // Calculate the rectangle bounds
        int rectWidth = (int) (width * 0.28);
        int rectHeight = (int) (height * 0.8);
        int rectLeft = (int) ((width - rectWidth) / 2);
        int rectTop = (int) ((height - rectHeight) / 2);
        int rectRight = rectLeft + rectWidth;
        int rectBottom = rectTop + rectHeight;

        // Draw the rectangle
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint);

        // Draw the text above the rectangle
        String text = "Position Your Head In The Box";
        float textWidth = textpaint.measureText(text);
        float x = rectLeft + (rectWidth - textWidth) / 2;
        float y = rectTop - 30;

        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(50);
        textpaint.setStyle(Paint.Style.STROKE);
        textpaint.setStrokeWidth(3);

        // Draw the black stroke of the text
        canvas.drawText(text, x, y, textpaint);

        textpaint.setColor(Color.WHITE);
        textpaint.setStyle(Paint.Style.FILL);

        // Draw the white fill of the text
        canvas.drawText(text, x, y, textpaint);

        // Draw face rectangle
        if (!rect.isEmpty()) {
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            canvas.drawRect(rect, paint);
        }

        // Draw face landmarks
        if (face != null){
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            drawFaceLandmarks(canvas, face, paint);
        }

        if (point != null){
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            canvas.drawCircle(point.x, point.y, 10, paint);
        }
    }

    private void drawFaceLandmarks(Canvas canvas, Face face, Paint paint) {
        for (FaceLandmark landmark : face.getAllLandmarks()) {
            Float leftEyeOpenProb;
            Float rightEyeOpenProb;

            // skip drawing if left eye is closed
            if (landmark.getLandmarkType() == FaceLandmark.LEFT_EYE) {
                leftEyeOpenProb = face.getLeftEyeOpenProbability();
                if (leftEyeOpenProb != null && leftEyeOpenProb < 0.5) {
                    continue;
                }
                PointF point = landmark.getPosition();
                canvas.drawCircle(point.x, point.y, 10, paint);
            }
            // skip drawing if right eye is closed
            if (landmark.getLandmarkType() == FaceLandmark.RIGHT_EYE) {
                rightEyeOpenProb = face.getRightEyeOpenProbability();
                if (rightEyeOpenProb != null && rightEyeOpenProb < 0.5) {
                    continue;
                }
                PointF point = landmark.getPosition();
                canvas.drawCircle(point.x, point.y, 10, paint);
            }
        }
    }
}
