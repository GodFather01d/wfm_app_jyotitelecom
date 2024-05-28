package com.jyotitelecommple.wfm;

import android.annotation.SuppressLint;
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

public class delete_tank extends AppCompatActivity {
    AutoCompleteTextView login_user, userInputEditText;
    Button deleteTankButton, deleteCityButton,deleteTankButtonconform,deleteCityButtonconform;
    Button cancel;

    DatabaseReference reference;
    String USERNAME;
    ArrayAdapter<String> cityAdapter, tankIdAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_tank);
        USERNAME = getIntent().getStringExtra("username");

        login_user = findViewById(R.id.login_username);
        userInputEditText = findViewById(R.id.userInputEditText);
        deleteTankButton = findViewById(R.id.deleteButtontank_id);
        deleteCityButton = findViewById(R.id.deleteButtoncity);
        deleteTankButtonconform = findViewById(R.id.confirm_button);
        cancel = findViewById(R.id.cancel_button);
        deleteCityButtonconform = findViewById(R.id.deleteconfirm);

        reference = FirebaseDatabase.getInstance().getReference("users").child(USERNAME);

        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        tankIdAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);

        login_user.setAdapter(cityAdapter);
        userInputEditText.setAdapter(tankIdAdapter);
        deleteTankButtonconform.setVisibility(View.GONE);
        deleteCityButtonconform.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);

        fetchCitiesAndTankIds();

        login_user.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = parent.getItemAtPosition(position).toString();
                fetchTankIds(selectedCity);
            }
        });

        deleteCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCity();
            }
        });

        deleteTankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTank();
            }
        });
        deleteTankButtonconform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAction();
            }
        });
        deleteCityButtonconform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCityconfirm();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCancel();
            }
        });
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
                Toast.makeText(delete_tank.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(delete_tank.this, "Failed to fetch tank IDs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteCity() {
        String city = login_user.getText().toString();
        if (city.isEmpty()) {
            Toast.makeText(delete_tank.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        deleteCityButton.setVisibility(View.GONE);
        deleteCityButtonconform.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        userInputEditText.setVisibility(View.GONE);
        deleteTankButton.setVisibility(View.GONE);
    }
    private void deleteCityconfirm() {
        String city = login_user.getText().toString();
        if (city.isEmpty()) {
            Toast.makeText(delete_tank.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        reference.child("tank_id").child(city).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(delete_tank.this, "City deleted successfully", Toast.LENGTH_SHORT).show();
                        login_user.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(delete_tank.this, "Failed to delete city", Toast.LENGTH_SHORT).show();
                    }
                });
        deleteCityButton.setVisibility(View.VISIBLE);
        deleteCityButtonconform.setVisibility(View.GONE);


        cancel.setVisibility(View.GONE);
        userInputEditText.setVisibility(View.VISIBLE);
        deleteTankButton.setVisibility(View.VISIBLE);
    }


    private void deleteTank() {

        String city = login_user.getText().toString();
        String tankId = userInputEditText.getText().toString();
        if (city.isEmpty() || tankId.isEmpty()) {
            Toast.makeText(delete_tank.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        cancel.setVisibility(View.VISIBLE);
        deleteTankButtonconform.setVisibility(View.VISIBLE);
        login_user.setVisibility(View.GONE);
        deleteCityButton.setVisibility(View.GONE);
        deleteTankButton.setVisibility(View.GONE);

    }
    private void confirmAction() {
        cancel.setVisibility(View.VISIBLE);
        String city = login_user.getText().toString();
        String tankId = userInputEditText.getText().toString();
        if (city.isEmpty() || tankId.isEmpty()) {
            Toast.makeText(delete_tank.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return; // Don't proceed further
        }
        reference.child("tank_id").child(city).child(tankId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(delete_tank.this, "Tank deleted successfully", Toast.LENGTH_SHORT).show();
                        userInputEditText.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(delete_tank.this, "Failed to delete tank", Toast.LENGTH_SHORT).show();
                    }
                });

        deleteTankButton.setVisibility(View.VISIBLE);
        deleteTankButtonconform.setVisibility(View.GONE);

        login_user.setVisibility(View.VISIBLE);
        deleteCityButton.setVisibility(View.VISIBLE);
        deleteTankButton.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.GONE);
    }
    private void setCancel() {
        deleteTankButton.setVisibility(View.VISIBLE);
        deleteTankButtonconform.setVisibility(View.GONE);
        login_user.setVisibility(View.VISIBLE);
        deleteCityButton.setVisibility(View.VISIBLE);
        deleteCityButtonconform.setVisibility(View.GONE);
        cancel.setVisibility(View.GONE);
        userInputEditText.setVisibility(View.VISIBLE);

    }

}
