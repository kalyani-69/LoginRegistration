package com.example.myapplication;

import android.annotation.SuppressLint;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_register extends AppCompatActivity {
    private EditText email, password, fullName, userName;
    private TextView txt_login;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Button btn_register;

    @SuppressLint("MissingInflatedId")
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
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullname1 = fullName.getText().toString();
                String email1 = email.getText().toString();
                String password1 = password.getText().toString();
                String userName1 = userName.getText().toString();

                if (fullname1.isEmpty() || email1.isEmpty() || password1.isEmpty() || userName1.isEmpty()) {
                    Toast.makeText(Activity_register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if username contains invalid characters
                if (userName1.contains(".") || userName1.contains("#") || userName1.contains("$") || 
                    userName1.contains("[") || userName1.contains("]")) {
                    Toast.makeText(Activity_register.this, "Username cannot contain special characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                HelperClass helperClass = new HelperClass(fullname1, email1, password1, userName1);

                reference.child(userName1).setValue(helperClass)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(Activity_register.this, "You Have Signed Up Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Activity_register.this, Activity_login.class));
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Activity_register.this, "Sign Up Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Activity_register.this, Activity_login.class));
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
