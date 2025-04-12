package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Activity_Dashboard extends AppCompatActivity {

    private TextView totalCows, avgMilkProduction, welcomeText;
    private ListView cowListView;
    private Button addCowButton;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ArrayList<String> cowList = new ArrayList<>();
    private ArrayList<String> cowIds = new ArrayList<>();
    private ArrayAdapter<String> cowAdapter;

    private ActivityResultLauncher<Intent> addCowLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        totalCows = findViewById(R.id.totalCows);
        avgMilkProduction = findViewById(R.id.avgMilkProduction);
        cowListView = findViewById(R.id.cowListView);
        addCowButton = findViewById(R.id.addCowButton);
        welcomeText = findViewById(R.id.dashboardWelcomeText);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Activity_login.class));
            finish();
            return;
        }

        cowAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cowList);
        cowListView.setAdapter(cowAdapter);

        // Set welcome message using user's email
        String email = currentUser.getEmail();
        if (email != null) {
            welcomeText.setText("Welcome, " + email + "!");
        }

        // Register result launcher for cow addition
        addCowLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadCowData();
                    }
                }
        );

        // Add cow button click
        addCowButton.setOnClickListener(v -> {
            Intent intent = new Intent(Activity_Dashboard.this, Activity_AddCow.class);
            addCowLauncher.launch(intent);
        });

        // Load cow list from Firebase
        loadCowData();

        // Padding for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadCowData() {
        String userId = currentUser.getUid();

        // Use reference to correct Firebase path: farmers -> UID -> cows
        FirebaseDatabase.getInstance().getReference("farmers")
                .child(currentUser.getUid())
                .child("cows")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Check if snapshot is empty
                        if (!snapshot.exists()) {
                            Toast.makeText(Activity_Dashboard.this, "No cows found.", Toast.LENGTH_SHORT).show();
                        }

                        // Clear previous data
                        cowList.clear();
                        cowIds.clear();

                        int total = 0;
                        double totalMilk = 0.0;

                        // Iterate through the cows and extract data
                        for (DataSnapshot cowSnapshot : snapshot.getChildren()) {
                            String name = cowSnapshot.child("name").getValue(String.class);
                            Double milk = cowSnapshot.child("milkProduction").getValue(Double.class);

                            total++;
                            totalMilk += (milk != null ? milk : 0.0);

                            cowList.add((name != null ? name : "Unnamed Cow") + " - " + milk + "L/day");
                            cowIds.add(cowSnapshot.getKey());
                        }

                        // Update dashboard views
                        totalCows.setText("Total Cows: " + total);
                        avgMilkProduction.setText("Avg Milk Production: " + (total > 0 ? (totalMilk / total) : 0) + " L/day");

                        // Notify adapter that data has changed
                        cowAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Activity_Dashboard.this, "Failed to load cows: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
