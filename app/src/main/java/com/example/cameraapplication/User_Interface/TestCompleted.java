package com.example.cameraapplication.User_Interface;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.cameraapplication.BuildConfig;
import com.example.cameraapplication.R;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;

public class TestCompleted extends AppCompatActivity {
    private File file;
    String firstName;
    String lastName;
    String email;
    XSSFWorkbook workbook = new XSSFWorkbook();
    // Create a new sheet
    XSSFSheet sheet = workbook.createSheet("Eye Movement Data");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testcompleted);

        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        email = getIntent().getStringExtra("email");

        double AccuracyPercentage = calculateAccuracyPercentage(1);

        // Get a reference to the TextView
        TextView textView2 = findViewById(R.id.textview_2);

        // Set the text of the TextView to the double value
        textView2.setText("Accuracy: " + AccuracyPercentage+ "%");

        file = new File(getExternalFilesDir(null), "eye_movement_data.xlsx");

        if (file.exists()) {
            try {
                // read data from the file using the input stream
                FileInputStream inputStream = new FileInputStream(file);
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);

        // Set Button for sending email
        Button sendEmailButton = findViewById(R.id.sendEmailButton);
        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the email intent
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Anti-Saccade Eye Movement Data for " + firstName + " " + lastName);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Please find attached the Anti-Saccade Test Data File");

                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Start the email activity
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
    }

    private double calculateAccuracyPercentage(double value) {
        double percentage = 0;

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

            XSSFSheet sheet = workbook.getSheet("Eye Movement Data");
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip first row
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            // Count occurrences of value in second column
            int count = 0;
            int total = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cell = row.getCell(1);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String str = cell.getStringCellValue();
                    double cellValue = 0;
                    if (str != null && !str.isEmpty() && !str.equals(" ")) {
                        cellValue = Double.parseDouble(str.trim());
                    }
                    if (cellValue == value) {
                        count++;
                    }
                    total++;
                }
            }

            // Calculate percentage
            if (total > 0) {
                percentage = ((double) count / (double) total) * 100;
            }
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.DOWN); // or RoundingMode.UP, RoundingMode.HALF_UP, etc.
        percentage = Double.parseDouble(df.format(percentage));

        // Add the data to the sheet
        int lastRowNum = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRowNum + 1);
        Cell cell = null;
        cell = row.createCell(0);
        cell.setCellValue("Accuracy: ");
        cell = row.createCell(1);
        cell.setCellValue(percentage);

        // Write the workbook to the file
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            outputStream.close();
            Log.d(TAG, "Eye movement data saved to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Error writing eye movement data to file.", e);
        }

        return percentage;
    }

//    // Delete the file on layout close
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (file.exists()) {
//            file.delete();
//        }
//    }
}
