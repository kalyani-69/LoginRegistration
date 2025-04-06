package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Activity_AddCow extends AppCompatActivity {

    EditText cowName, cowColor, pregnancyDate, medicalHistory, milkProduction;
    Button saveButton;
    FirebaseAuth mAuth;
    DatabaseReference cowRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_cow);

        cowName = findViewById(R.id.cowName);
        cowColor = findViewById(R.id.cowColor);
        pregnancyDate = findViewById(R.id.pregnancyDate);
        medicalHistory = findViewById(R.id.medicalHistory);
        milkProduction = findViewById(R.id.milkProduction);
        saveButton = findViewById(R.id.saveCowButton);

        mAuth = FirebaseAuth.getInstance();
        cowRef = FirebaseDatabase.getInstance().getReference("cows");

        pregnancyDate.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> {
            String name = cowName.getText().toString().trim();
            String color = cowColor.getText().toString().trim();
            String pregDate = pregnancyDate.getText().toString().trim();
            String history = medicalHistory.getText().toString().trim();
            String milkStr = milkProduction.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(color) || TextUtils.isEmpty(milkStr)) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double milk = Double.parseDouble(milkStr);
            String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown";
            String cowId = cowRef.push().getKey();

            Map<String, Object> cowData = new HashMap<>();
            cowData.put("name", name);
            cowData.put("color", color);
            cowData.put("pregnancyDate", pregDate);
            cowData.put("medicalHistory", history);
            cowData.put("milkProduction", milk);
            cowData.put("farmerId", userId);

            if (cowId != null) {
                cowRef.child(cowId).setValue(cowData)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Cow added successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK); // this lets the dashboard refresh cow list
                            finish(); // go back to dashboard
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            pregnancyDate.setText(date);
        }, year, month, day).show();
    }
}
