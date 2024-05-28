package com.jyotitelecommple.wfm;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class activity_settings extends AppCompatActivity {
    private Spinner spinnerCity;
    private Spinner spinnerTankId;
    private TextView textViewTankHeight;
    private TextView analogValueTextView;
    private TextView textViewLowerLimit;
    private TextView textViewUpperLimit;
    private TextView textViewEmail;
    private Button buttonUpdateSettings;
    private DatabaseReference mDatabase;
    private EditText editTextTankHeight;
    private EditText editTextLowerLimit;
    private EditText editTextUpperLimit;
    private EditText editTextEmail;

    private boolean settingsLoaded = false;
    String USERNAME;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
         USERNAME = getIntent().getStringExtra("username");

        // Initialize views
        spinnerCity = findViewById(R.id.cityspinner);
        spinnerTankId = findViewById(R.id.tankIdSpinner);
        textViewTankHeight = findViewById(R.id.textViewTankHeight);
        textViewLowerLimit = findViewById(R.id.textViewLowerLimit);
        textViewUpperLimit = findViewById(R.id.textViewUpperLimit);
        textViewEmail = findViewById(R.id.textViewEmail);
        buttonUpdateSettings = findViewById(R.id.buttonSaveSettings);

        editTextTankHeight = findViewById(R.id.editTextTankHeight);
        editTextUpperLimit = findViewById(R.id.editTextLowerLimit);
          editTextLowerLimit = findViewById(R.id.editTextUpperLimit);
        editTextEmail = findViewById(R.id.editTextEmail);
         analogValueTextView = findViewById(R.id.analogValueTextView);


        // Initialize Firebase Database reference


        // Populate city spinner
        populateCitySpinner();

        // Set click listener for the update settings button
        buttonUpdateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });
    }

    private void populateCitySpinner() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(USERNAME).child("tank_id");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(activity_settings.this, android.R.layout.simple_spinner_item);
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(cityAdapter);

                for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                    String city = citySnapshot.getKey();
                    cityAdapter.add(city);
                }

                // Set spinner item selection listener for city
                spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedCity = parent.getItemAtPosition(position).toString();
                        populateTankIdSpinner(selectedCity);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(activity_settings.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateTankIdSpinner(String selectedCity) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(USERNAME).child("tank_id");
        mDatabase.child(selectedCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayAdapter<String> tankIdAdapter = new ArrayAdapter<>(activity_settings.this, android.R.layout.simple_spinner_item);
                tankIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerTankId.setAdapter(tankIdAdapter);

                for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                    String tankId = tankSnapshot.getValue(String.class);
                    tankIdAdapter.add(tankId);
                }

                // Set spinner item selection listener for tank ID
                spinnerTankId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedTankId = parent.getItemAtPosition(position).toString();
                        loadTankSettings(selectedCity, selectedTankId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(activity_settings.this, "Failed to fetch tank IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTankSettings(String selectedCity, String selectedTankId) {
        mDatabase = FirebaseDatabase.getInstance().getReference(selectedTankId);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Float tankHeight = dataSnapshot.child("tankHeight").getValue(Float.class);
                    Float lowerLimit = dataSnapshot.child("lowerLimit").getValue(Float.class);
                    Float upperLimit = dataSnapshot.child("upperLimit").getValue(Float.class);
                    Float email = dataSnapshot.child("Start_time").getValue(Float.class);
                    Float analogMv = dataSnapshot.child("Analog_mv").getValue(Float.class);
                    if (analogMv != null) {
                        analogValueTextView.setText("Analog mV: " + analogMv);
                    } else {
                        analogValueTextView.setText("N/A");
                    }
                    if (tankHeight != null) {
                        editTextTankHeight.setText(String.format("%.2f", tankHeight));
                    }
                    if (lowerLimit != null) {
                        editTextLowerLimit.setText(String.format("%.2f", lowerLimit));
                    }
                    if (upperLimit != null) {
                        editTextUpperLimit.setText(String.format("%.2f", upperLimit));
                    }

                    if (email != null) {
                        editTextEmail.setText(String.format("%.2f",email));
                    }

                    settingsLoaded = true; // Settings are loaded
                } else {
                    // Tank ID doesn't exist
                    Toast.makeText(activity_settings.this, "Tank ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(activity_settings.this, "Error occurred: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSettings() {
        if (!settingsLoaded) {
            Toast.makeText(this, "Please select a tank ID first", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCity = spinnerCity.getSelectedItem().toString();
        String selectedTankId = spinnerTankId.getSelectedItem().toString();
        mDatabase = FirebaseDatabase.getInstance().getReference(selectedTankId);

        float tankHeight = Float.parseFloat(editTextTankHeight.getText().toString());
        float lowerLimit = Float.parseFloat(editTextLowerLimit.getText().toString());
        float upperLimit = Float.parseFloat(editTextUpperLimit.getText().toString());
        float email = Float.parseFloat(editTextEmail.getText().toString());

        mDatabase.child("tankHeight").setValue(tankHeight);
        mDatabase.child("lowerLimit").setValue(lowerLimit);
        mDatabase.child("upperLimit").setValue(upperLimit);
        mDatabase.child("Start_time").setValue(email);
        mDatabase.child("reset").setValue(1);

        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show();


    }
}
