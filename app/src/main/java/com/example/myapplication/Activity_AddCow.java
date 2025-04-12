package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.text.SimpleDateFormat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class Activity_AddCow extends AppCompatActivity {

    EditText cowName, cowColor, pregnancyDate, medicalHistory, milkProduction;
    Button saveButton;
    ImageView cowImageView;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    Uri imageUri = null;
    String cowImageUrl = "";
    DatabaseReference cowRef;
    ActivityResultLauncher<String> imagePickerLauncher;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_cow);

        // Bind views
        cowName = findViewById(R.id.cowName);
        cowColor = findViewById(R.id.cowColor);
        pregnancyDate = findViewById(R.id.pregnancyDate);
        medicalHistory = findViewById(R.id.medicalHistory);
        milkProduction = findViewById(R.id.milkProduction);
        saveButton = findViewById(R.id.saveCowButton);
        cowImageView = findViewById(R.id.cowImageView);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Activity_login.class));
            finish();
            return;
        }
        cowRef = FirebaseDatabase.getInstance().getReference("farmers")
                .child(currentUser.getUid())
                .child("cows");

        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Activity_login.class));
            finish();
            return;
        }
        if (cowRef == null) {
            Toast.makeText(this, "Database reference is not initialized!", Toast.LENGTH_SHORT).show();
            return;
        }
// 👉 FIX: Initialize cowRef
        cowRef = FirebaseDatabase.getInstance().getReference("farmers")
                .child(currentUser.getUid())
                .child("cows");

        // Image picker setup
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        cowImageView.setImageURI(uri);
                    }
                }
        );

        cowImageView.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        pregnancyDate.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> {
            String name = cowName.getText().toString().trim();
            String color = cowColor.getText().toString().trim();
            String pregDate = pregnancyDate.getText().toString().trim();
            String history = medicalHistory.getText().toString().trim();
            String milkStr = milkProduction.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(color) || TextUtils.isEmpty(milkStr)) {
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double milk;
            try {
                milk = Double.parseDouble(milkStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid milk value", Toast.LENGTH_SHORT).show();
                return;
            }

           // } else {
                saveCowData(name, color, pregDate, history, milk, "");
           // }
        });

//        cowRef = FirebaseDatabase.getInstance().getReference("cows");
//
//        // Date picker
//        pregnancyDate.setOnClickListener(v -> showDatePicker());
//
//        // Save button logic
//        saveButton.setOnClickListener(v -> {
//            String name = cowName.getText().toString().trim();
//            String color = cowColor.getText().toString().trim();
//            String pregDate = pregnancyDate.getText().toString().trim();
//            String history = medicalHistory.getText().toString().trim();
//            String milkStr = milkProduction.getText().toString().trim();
//
//            // Validate required fields
//            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(color) || TextUtils.isEmpty(milkStr)) {
//                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            double milk;
//            try {
//                milk = Double.parseDouble(milkStr);
//            } catch (NumberFormatException e) {
//                Toast.makeText(this, "Invalid milk production value", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            String userId = currentUser.getUid();
//            String cowId = cowRef.push().getKey();
//
//            Map<String, Object> cowData = new HashMap<>();
//            cowData.put("name", name);
//            cowData.put("color", color);
//            cowData.put("pregnancyDate", pregDate);
//            cowData.put("medicalHistory", history);
//            cowData.put("milkProduction", milk);
//            cowData.put("farmerId", userId);
//
//            if (cowId != null) {
//                cowRef.child(cowId).setValue(cowData)
//                        .addOnSuccessListener(unused -> {
//                            Toast.makeText(this, "Cow added successfully", Toast.LENGTH_SHORT).show();
//                            setResult(RESULT_OK);
//                            finish(); // return to dashboard
//                        })
//                        .addOnFailureListener(e -> {
//                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                            Log.e("AddCow", "Firebase Error: ", e);
//                        });
//            }
//        });

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            pregnancyDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

//    private void uploadImageAndSaveCow(String name, String color, String pregDate, String history, double milk, String imageurl) {
//        String uid = currentUser.getUid();
//        String fileName = "cow_images/" + uid + "/" + System.currentTimeMillis() + ".jpg";
//
//        StorageReference storageRef = FirebaseStorage.getInstance().getReference(fileName);
//        storageRef.putFile(imageUri)
//                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                    cowImageUrl = uri.toString();
//                    saveCowData(name, color, pregDate, history, milk, cowImageUrl);
//                }))
//                .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
//    }

    private void saveCowData(String name, String color, String pregDate, String history, Double milk, String imageUrl) {
        // Check if any field is empty
        if (name.isEmpty() || color.isEmpty() || pregDate.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the "cows" node under the current user's UID
        String cowId = cowRef.push().getKey();
        Map<String, Object> cowData = new HashMap<>();
        cowData.put("name", name);
        cowData.put("color", color);
        cowData.put("pregnancyDate", pregDate);
        cowData.put("medicalHistory", history);
        cowData.put("milkProduction", milk);
        cowData.put("imageUrl", imageUrl);  // Set image URL (can be empty for now)

        // Push cow data to Firebase
        cowRef.child(cowId).setValue(cowData).addOnSuccessListener(aVoid -> {
            Toast.makeText(Activity_AddCow.this, "Cow added successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);  // Notify that the cow has been added successfully
            finish();  // Go back to the dashboard
        }).addOnFailureListener(e -> {
            Toast.makeText(Activity_AddCow.this, "Failed to add cow", Toast.LENGTH_SHORT).show();
        });
    }

    private String calculateNextReminder(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(dateStr));
            calendar.add(Calendar.MONTH, 9);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            return "";
        }
    }
}

