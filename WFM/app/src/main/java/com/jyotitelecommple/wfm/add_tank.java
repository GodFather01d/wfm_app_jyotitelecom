package com.jyotitelecommple.wfm;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class add_tank extends AppCompatActivity {
    AutoCompleteTextView login_user, userInputEditText;
    Button saveButtontank_id;
    DatabaseReference reference;
    String USERNAME;
    ArrayAdapter<String> cityAdapter, tankIdAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tank);
        USERNAME = getIntent().getStringExtra("username");

        login_user = findViewById(R.id.login_username);
        userInputEditText = findViewById(R.id.userInputEditText);
        saveButtontank_id = findViewById(R.id.saveButtontank_id);


        reference = FirebaseDatabase.getInstance().getReference("users").child(USERNAME);

        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        tankIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);

        login_user.setAdapter(cityAdapter);
        userInputEditText.setAdapter(tankIdAdapter);

        fetchCitiesAndTankIds();

        login_user.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = parent.getItemAtPosition(position).toString();
                fetchTankIds(selectedCity);
            }
        });

        saveButtontank_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTank();
            }
        });



        // Initially, set the confirm button to be invisible

    }

    private void fetchCitiesAndTankIds() {
        reference.child("tank_id").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                    String city = citySnapshot.getKey();
                    cityAdapter.add(city);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(add_tank.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTankIds(String selectedCity) {
        tankIdAdapter.clear();
        reference.child("tank_id").child(selectedCity).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot tankSnapshot : dataSnapshot.getChildren()) {
                    String tankId = tankSnapshot.getValue(String.class);
                    tankIdAdapter.add(tankId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(add_tank.this, "Failed to fetch tank IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTank() {



        String city = login_user.getText().toString();
        String tankId = userInputEditText.getText().toString();
        if (city.isEmpty() || tankId.isEmpty()) {
            Toast.makeText(add_tank.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        reference.child("tank_id").child(city).child(tankId).setValue(tankId)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(add_tank.this, "Tank added successfully", Toast.LENGTH_SHORT).show();
                        userInputEditText.setText("");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(add_tank.this, "Failed to add tank", Toast.LENGTH_SHORT).show();
                    }
                });


        saveButtontank_id.setVisibility(View.VISIBLE);// Hide the button after confirmation
    }
}
