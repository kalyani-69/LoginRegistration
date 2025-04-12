package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_register extends AppCompatActivity {

    private EditText email, password, fullName, userName;
    private TextView txt_login;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private Button btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        userName = findViewById(R.id.username);
        btn_register = findViewById(R.id.btn_register);
        txt_login = findViewById(R.id.txt_login);

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullNameStr = fullName.getText().toString().trim();
                String emailStr = email.getText().toString().trim();
                String passwordStr = password.getText().toString().trim();
                String usernameStr = userName.getText().toString().trim();

                if (fullNameStr.isEmpty() || emailStr.isEmpty() || passwordStr.isEmpty() || usernameStr.isEmpty()) {
                    Toast.makeText(Activity_register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (usernameStr.contains(".") || usernameStr.contains("#") || usernameStr.contains("$") ||
                        usernameStr.contains("[") || usernameStr.contains("]")) {
                    Toast.makeText(Activity_register.this, "Username cannot contain special characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create user using Firebase Authentication
                mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                String uid = firebaseUser.getUid();

                                // Save additional user data to Realtime Database
                                HelperClass helperClass = new HelperClass(emailStr, fullNameStr, usernameStr, ""); // don't store password
                                reference.child(uid).setValue(helperClass)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(Activity_register.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Activity_register.this, Activity_login.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(Activity_register.this, "Database Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(Activity_register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        txt_login.setOnClickListener(view -> {
            startActivity(new Intent(Activity_register.this, Activity_login.class));
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
