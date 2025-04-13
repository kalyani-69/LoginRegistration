package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Activity_AddCow extends AppCompatActivity {

    private EditText cowName, cowColor, pregnancyDate, medicalHistory, milkProduction;
    private ImageView cowImageView;
    private Button saveButton;
    private FirebaseAuth mAuth;
    private DatabaseReference cowRef;
    private FirebaseUser currentUser;

    private String cowId;
    private boolean isEdit = false;
    private Uri imageUri;
    private String imageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cow);

        // Initialize UI
        cowName = findViewById(R.id.cowName);
        cowColor = findViewById(R.id.cowColor);
        pregnancyDate = findViewById(R.id.pregnancyDate);
        medicalHistory = findViewById(R.id.medicalHistory);
        milkProduction = findViewById(R.id.milkProduction);
        cowImageView = findViewById(R.id.cowImageView);
        saveButton = findViewById(R.id.saveCowButton);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cowRef = FirebaseDatabase.getInstance().getReference("farmers")
                .child(currentUser.getUid())
                .child("cows");

        // Check if editing

        Intent intent = getIntent();
        cowId = intent.getStringExtra("cowId");
        //isEdit = cowId != null;
        if(cowId != null){
            isEdit=true;
        }

        if (isEdit) {
            cowName.setText(intent.getStringExtra("name"));
            cowColor.setText(intent.getStringExtra("color"));
            pregnancyDate.setText(intent.getStringExtra("pregnancyDate"));
            medicalHistory.setText(intent.getStringExtra("medicalHistory"));
            milkProduction.setText(String.valueOf(intent.getDoubleExtra("milkProduction", 0.0)));

            imageUrl = intent.getStringExtra("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(cowImageView);
            }
        }

        cowImageView.setOnClickListener(v -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickImage, 1);
        });

        pregnancyDate.setOnClickListener(v -> showDatePicker());

        saveButton.setOnClickListener(v -> {

            String name = cowName.getText().toString().trim();
            String color = cowColor.getText().toString().trim();
            String pregDate = pregnancyDate.getText().toString().trim();
            String history = medicalHistory.getText().toString().trim();
            String milkStr = milkProduction.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(color) || TextUtils.isEmpty(milkStr)) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double milk;
            try {
                milk = Double.parseDouble(milkStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid milk production value", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                uploadImageAndSaveCow(name, color, pregDate, history, milk);
            } else {
                saveCowData(name, color, pregDate, history, milk, imageUrl); // use existing URL
            }
        });
    }

    // Image picker result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            cowImageView.setImageURI(imageUri);
        }
    }

    private void uploadImageAndSaveCow(String name, String color, String pregDate, String history, double milk) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("cow_images");
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    saveCowData(name, color, pregDate, history, milk, imageUrl);
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                )).addOnFailureListener(e ->
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        );
    }

    private void saveCowData(String name, String color, String pregDate, String history, double milk, String imageUrl) {
        Map<String, Object> cowData = new HashMap<>();
        cowData.put("name", name);
        cowData.put("color", color);
        cowData.put("pregnancyDate", pregDate);
        cowData.put("medicalHistory", history);
        cowData.put("milkProduction", milk);
        cowData.put("imageUrl", imageUrl);

        if (isEdit) {
            cowData.put("cowId", cowId);
            cowRef.child(cowId).updateChildren(cowData).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cow updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to update cow", Toast.LENGTH_SHORT).show()
            );
        }else {
            String newCowId = cowRef.push().getKey();
            cowData.put("cowId", newCowId);
            cowRef.child(newCowId).setValue(cowData).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cow added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Failed to add cow", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> pregnancyDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
