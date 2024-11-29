package com.futsch1.medtimer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.futsch1.medtimer.database.Patient;
import com.futsch1.medtimer.database.PatientDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private PatientDatabase db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the Room database and ExecutorService
        db = PatientDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        // Bind views from the XML layout
        EditText phoneEditText = findViewById(R.id.phoneno);
        EditText passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);

        // Set click listener on the login button
        loginButton.setOnClickListener(v -> {
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate input fields
            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter both phone and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Execute login logic in a background thread
            executorService.execute(() -> {
                // Fetch patient from the database using phone and password
                Patient patient = db.PatientDao().getPatient(phone, password);

                if (patient != null) {
                    // Login successful, redirect to Dashboard
                    runOnUiThread(() -> {
                        Log.d("LoginActivity", "Login successful, starting MainActivity");
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close the LoginActivity to prevent the user from returning to it
                    });
                } else {
                    // Invalid credentials
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid credentials!", Toast.LENGTH_SHORT).show());
                }
            });
        });

        // Set click listener on the signup button to redirect to signup activity
        signupButton.setOnClickListener(v -> {
            Intent signupIntent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(signupIntent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // Shut down the executor service when the activity is destroyed
    }
}
