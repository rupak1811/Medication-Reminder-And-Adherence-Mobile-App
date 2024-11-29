package com.futsch1.medtimer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.futsch1.medtimer.database.PatientDatabase;
import com.futsch1.medtimer.database.Patient;

import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {

    private PatientDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize the Room database
        db = PatientDatabase.getDatabase(this);

        // Bind views from the XML layout
        EditText nameEditText = findViewById(R.id.name);
        EditText ageEditText = findViewById(R.id.age);
        EditText genderEditText = findViewById(R.id.gender);
        EditText contactEditText = findViewById(R.id.contact);
        EditText addressEditText = findViewById(R.id.address);
        EditText phoneEditText = findViewById(R.id.phoneno);
        EditText passwordEditText = findViewById(R.id.password);
        Button registerButton = findViewById(R.id.registerButton);

        // Set click listener on the register button
        registerButton.setOnClickListener(v -> {
            // Get data from input fields
            String name = nameEditText.getText().toString().trim();
            String ageString = ageEditText.getText().toString().trim();
            String gender = genderEditText.getText().toString().trim();
            String contact = contactEditText.getText().toString().trim();
            String address = addressEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate input data
            if (name.isEmpty() || ageString.isEmpty() || gender.isEmpty() ||
                    contact.isEmpty() || address.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageString); // Parse age to an integer
            } catch (NumberFormatException e) {
                Toast.makeText(RegistrationActivity.this, "Invalid age format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new Patient object
            Patient patient = new Patient(name, age, contact, address, gender, phone, password);

            // Insert the patient data into the Room database using a background thread
            Executors.newSingleThreadExecutor().execute(() -> {
                db.PatientDao().insert(patient); // Insert patient into the database
                runOnUiThread(() -> Toast.makeText(RegistrationActivity.this, "Patient registered successfully!", Toast.LENGTH_SHORT).show());
            });
        });
    }
}
