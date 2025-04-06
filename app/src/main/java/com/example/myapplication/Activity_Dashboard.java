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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class Activity_Dashboard extends AppCompatActivity {

    private TextView totalCows, avgMilkProduction, welcomeText;
    private ListView cowListView;
    private Button addCowButton;

    private FirebaseAuth mAuth;
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

        // UI bindings
        totalCows = findViewById(R.id.totalCows);
        avgMilkProduction = findViewById(R.id.avgMilkProduction);
        cowListView = findViewById(R.id.cowListView);
        addCowButton = findViewById(R.id.addCowButton);
        welcomeText = findViewById(R.id.dashboardWelcomeText);

        mAuth = FirebaseAuth.getInstance();

        cowAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cowList);
        cowListView.setAdapter(cowAdapter);

        // Show username
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            welcomeText.setText("Welcome, " + username + "!");
        }

        // Initialize the launcher
        addCowLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadCowData(); // Refresh cow data
                    }
                }
        );

        // Add cow button functionality
        addCowButton.setOnClickListener(v -> {
            Log.d("Dashboard", "Add Cow button clicked");
            Intent intent = new Intent(Activity_Dashboard.this, Activity_AddCow.class);
            addCowLauncher.launch(intent);
        });

        // Load cow data from Firebase
        loadCowData();

        // Padding for Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashboard), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void loadCowData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        FirebaseDatabase.getInstance().getReference("cows")
                .orderByChild("farmerId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        cowList.clear();
                        cowIds.clear();

                        int total = 0;
                        double totalMilk = 0.0;

                        for (DataSnapshot cowSnapshot : snapshot.getChildren()) {
                            String name = cowSnapshot.child("name").getValue(String.class);
                            Double milk = cowSnapshot.child("milkProduction").getValue(Double.class);

                            total++;
                            totalMilk += (milk != null) ? milk : 0.0;

                            cowList.add((name != null ? name : "Unnamed Cow") + " - " + milk + "L/day");
                            cowIds.add(cowSnapshot.getKey());
                        }

                        totalCows.setText("Total Cows: " + total);
                        avgMilkProduction.setText("Avg Milk Production: " + (total > 0 ? (totalMilk / total) : 0) + " L/day");
                        cowAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Activity_Dashboard.this, "Failed to load cows: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
