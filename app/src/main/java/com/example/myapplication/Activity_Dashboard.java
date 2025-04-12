package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;  // Import ArrayList
import java.util.List;

public class Activity_Dashboard extends AppCompatActivity {

    private TextView welcomeText, totalCows, avgMilkProduction;
    private RecyclerView cowRecyclerView;
    private Button addCowButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference cowRef;
    private CowAdapter cowAdapter;
    private ArrayList<Cow> cowList;  // Use ArrayList instead of List

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Bind views
        welcomeText = findViewById(R.id.dashboardWelcomeText);
        totalCows = findViewById(R.id.totalCows);
        avgMilkProduction = findViewById(R.id.avgMilkProduction);
        addCowButton = findViewById(R.id.addCowButton);
        cowRecyclerView = findViewById(R.id.cowRecyclerView);

        // Set up Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        cowRef = FirebaseDatabase.getInstance().getReference("farmers").child(currentUser.getUid()).child("cows");

        // Set up RecyclerView
        cowRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cowList = new ArrayList<>();  // Initialize as ArrayList
        cowAdapter = new CowAdapter(this, cowList);
        cowRecyclerView.setAdapter(cowAdapter);

        // Load cow data from Firebase
        loadCowData();

        // Add Cow Button Logic
        addCowButton.setOnClickListener(v -> {
            Intent intent = new Intent(Activity_Dashboard.this, Activity_AddCow.class);
            startActivity(intent);
        });
    }

    private void loadCowData() {
        cowRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cowList.clear();
                int totalCowsCount = 0;
                double totalMilk = 0;

                for (DataSnapshot cowSnapshot : dataSnapshot.getChildren()) {
                    Cow cow = cowSnapshot.getValue(Cow.class);
                    if (cow != null) {
                        cowList.add(cow);
                        totalCowsCount++;
                        totalMilk += cow.getMilkProduction();
                    }
                }

                // Update UI with total cows and average milk production
                cowAdapter.notifyDataSetChanged();
                totalCows.setText("Total Cows: " + totalCowsCount);
                if (totalCowsCount > 0) {
                    avgMilkProduction.setText("Avg Milk Production: " + (totalMilk / totalCowsCount) + " L");
                } else {
                    avgMilkProduction.setText("Avg Milk Production: 0L");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Activity_Dashboard.this, "Failed to load cow data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
