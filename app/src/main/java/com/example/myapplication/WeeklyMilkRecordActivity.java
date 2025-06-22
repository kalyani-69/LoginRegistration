package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class WeeklyMilkRecordActivity extends AppCompatActivity {

    private EditText dateEditText, milkLitersEditText;
    private Button saveButton;
    private DatabaseReference milkRecordRef;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_milk_record);

        dateEditText = findViewById(R.id.dateEditText);
        milkLitersEditText = findViewById(R.id.milkLitersEditText);
        saveButton = findViewById(R.id.saveMilkRecordButton);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        milkRecordRef = FirebaseDatabase.getInstance().getReference()
                .child("farmers")
                .child(userId)
                .child("milkRecords");

        dateEditText.setOnClickListener(view -> showDatePicker());

        saveButton.setOnClickListener(view -> saveMilkRecord());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, month1 + 1, year1);
                    dateEditText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void saveMilkRecord() {
        String date = dateEditText.getText().toString().trim();
        String liters = milkLitersEditText.getText().toString().trim();

        if (date.isEmpty() || liters.isEmpty()) {
            Toast.makeText(this, "Please fill both date and liters!", Toast.LENGTH_SHORT).show();
            return;
        }

        MilkRecord record = new MilkRecord(date, liters);

        milkRecordRef.push().setValue(record)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Milk record saved!", Toast.LENGTH_SHORT).show();
                    // 👉 After saving, open the GraphActivity
                    startActivity(new Intent(WeeklyMilkRecordActivity.this, GraphActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
