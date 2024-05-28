package com.jyotitelecommple.wfm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.john.waveview.WaveView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private Runnable mRunnable;

    private ToggleButton switchMotor, switchMotor2;
    private ImageView ledIndicator, ledIndicator2;
    private TextView distanceTextView, voltageview;
    private TextView percentageTextView;
    private TextView motor2;
    private TextView motor1, TIME;
    private boolean valueZeroSent = false;

    private ImageView imageView, status;
    private ProgressBar progressBar;
    private WaveView waveView;
    private TextView ADDTANK;
    private Spinner citySpinner;
    private Spinner tankIdSpinner;
    private Runnable mSendZeroRunnable = new Runnable() {
        @Override
        public void run() {
            // Update the state to 0
            mDatabase.child("status").setValue(0);
            // Schedule the next update after 1 minute
            mHandler.postDelayed(this, 60000); // 1 minute in milliseconds
        }
    };


    private DatabaseReference tankRef;
    private ValueEventListener cityValueEventListener;
    private ValueEventListener tankDataValueEventListener;
    private DatabaseReference mDatabase;
    Integer waterPercentage;
    String USERNAME;
    private static final String CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID = 1;
    final long[] vibe = {0, 500};
    String selectedCity;
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_NOTIFY_SENT = "notifySent";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    final Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Toolbar toolbar = findViewById(R.id.toolbar);
             setSupportActionBar(toolbar);
        imageView = findViewById(R.id.imageView);
        switchMotor = findViewById(R.id.switch1);
        switchMotor2 =  findViewById(R.id.switch2);
        ledIndicator = findViewById(R.id.ledIndicator);
        ledIndicator2 = findViewById(R.id.ledIndicator2);
        voltageview =  findViewById(R.id.voltage);
        distanceTextView = findViewById(R.id.distanceTextView);
        percentageTextView = findViewById(R.id.percent);
        waveView = findViewById(R.id.wave_view);
        motor1 = findViewById(R.id.M1);
        motor2 = findViewById(R.id.M2);
        ADDTANK = findViewById(R.id.add_tank);
        status = findViewById(R.id.STATE);

        citySpinner = findViewById(R.id.cityspinner);
        tankIdSpinner = findViewById(R.id.tankIdSpinner);

        switchMotor.setVisibility(View.GONE);
        switchMotor2.setVisibility(View.GONE);
        ledIndicator.setVisibility(View.GONE);
        distanceTextView.setVisibility(View.GONE);
        percentageTextView.setVisibility(View.GONE);

        waveView.setVisibility(View.GONE);
        voltageview.setVisibility(View.GONE);

        ledIndicator2.setVisibility(View.GONE);
        motor1.setVisibility(View.GONE);
        motor2.setVisibility(View.GONE);
        status.setVisibility(View.GONE);

        USERNAME = getIntent().getStringExtra("username");

        setUpFirebase();
        createNotificationChannel();
        setUpSwitchListener();
        ADDTANK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, add_tank.class);
                intent.putExtra("username", USERNAME);
                startActivity(intent);
            }
        });

    }

    private void setUpFirebase() {
        DatabaseReference cityRef = FirebaseDatabase.getInstance().getReference("users").child(USERNAME).child("tank_id");
        cityValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> cities = new ArrayList<>();
                for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                    cities.add(citySnapshot.getKey());
                }

                ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, cities);
                cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                citySpinner.setAdapter(cityAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };
        cityRef.addListenerForSingleValueEvent(cityValueEventListener);

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = parent.getItemAtPosition(position).toString();
                DatabaseReference tankIdsRef = FirebaseDatabase.getInstance().getReference("users").child(USERNAME).child("tank_id").child(selectedCity);

                tankIdsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> tankIds = new ArrayList<>();
                        for (DataSnapshot tankIdSnapshot : dataSnapshot.getChildren()) {
                            tankIds.add(tankIdSnapshot.getValue(String.class));
                        }

                        ArrayAdapter<String> tankIdAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, tankIds);
                        tankIdAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        tankIdSpinner.setAdapter(tankIdAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        tankIdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String selectedTankId;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selectedTankId = parent.getItemAtPosition(position).toString();
                DatabaseReference newTankRef = FirebaseDatabase.getInstance().getReference().child(selectedTankId);

                if (tankRef != null && tankDataValueEventListener != null) {
                    tankRef.removeEventListener(tankDataValueEventListener);
                }

                tankRef = newTankRef;

                tankDataValueEventListener = new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            Integer switchState = dataSnapshot.child("switchstate").getValue(Integer.class);
                            Integer switchState2 = dataSnapshot.child("switchstate2").getValue(Integer.class);
                            Float voltage = dataSnapshot.child("averageVoltage").getValue(Float.class);
                            waterPercentage = dataSnapshot.child("water_percentage").getValue(Integer.class);
                            Integer notifys = dataSnapshot.child("notify").getValue(Integer.class);
                            Integer notification2 = dataSnapshot.child("notification2").getValue(Integer.class);
                            Integer state = dataSnapshot.child("status").getValue(Integer.class);
                            Integer hours = dataSnapshot.child("hr").getValue(Integer.class);
                            Integer min = dataSnapshot.child("min").getValue(Integer.class);
                            Integer last_notifiction = dataSnapshot.child("last_notification").getValue(Integer.class);
                            Integer last_notifiction2 = dataSnapshot.child("last_notification2").getValue(Integer.class);

                            Integer feet = dataSnapshot.child("feet").getValue(Integer.class);
                            Integer inch = dataSnapshot.child("inch").getValue(Integer.class);
                            String time = new String(String.valueOf(hours)) +":"+ new String(String.valueOf(min));
                            mDatabase = FirebaseDatabase.getInstance().getReference(selectedTankId);


                            if (feet != null) {
                                distanceTextView.setText("Level  " + feet + "' " + inch + "''  Feet");
                            } else {
                                distanceTextView.setText("N/A");
                            }
                            if (voltage != null) {
                                voltageview.setText("Battery  " + String.format("%.2f", voltage) + " v");

                            } else {
                                voltageview.setText("N/A");
                            }

                            if (switchState != null && switchState == 1) {
                                ledIndicator.setColorFilter(Color.GREEN);
                                mDatabase.child("notify").setValue(3);
                            } else {
                                ledIndicator.setColorFilter(Color.TRANSPARENT);
                                mDatabase.child("notify").setValue(4);
                            }
                            if (switchState2 != null && switchState2 == 1) {
                                ledIndicator2.setColorFilter(Color.GREEN);
                                mDatabase.child("notification2").setValue(3);
                            } else {
                                ledIndicator2.setColorFilter(Color.TRANSPARENT);
                                mDatabase.child("notification2").setValue(4);
                            }

                            if (state != null && state == 1) {
                                status.setColorFilter(Color.GREEN);
                            } else {
                                status.setColorFilter(Color.GRAY);
                            }

                            if (waterPercentage != null) {

                                int progress = Math.round(waterPercentage);
                                waveView.setProgress(progress);
                                percentageTextView.setText(progress + "%");
                            }


                            if (notifys != null && notifys == 3 && !Objects.equals(last_notifiction, notifys)) {
                                createNotificationChannel();
                                sendNotification("Motor_On  ", selectedTankId , time ,selectedCity,notifys);
                                mDatabase.child("last_notification").setValue(3);
                            }
                            if (notifys != null && notifys == 4 && !Objects.equals(last_notifiction, notifys)) {
                                createNotificationChannel();
                                sendNotification("Motor_Off  ", selectedTankId , time ,selectedCity,notifys);
                                mDatabase.child("last_notification").setValue(4);
                            }

                            if (notification2 != null && notification2 == 3 && !Objects.equals(last_notifiction2, notification2)) {
                                createNotificationChannel();
                                sendNotification2("Motor2_On  ", selectedTankId , time ,selectedCity,notification2);
                                mDatabase.child("last_notification2").setValue(3);
                            }
                            if (notification2 != null && notification2 == 4  && !Objects.equals(last_notifiction2, notification2)) {
                                createNotificationChannel();
                                sendNotification2("Motor2_Off  ", selectedTankId , time ,selectedCity,notification2);
                                mDatabase.child("last_notification2").setValue(4);
                            }

                            ADDTANK.setVisibility(View.GONE);
                            switchMotor.setVisibility(View.VISIBLE);
                            switchMotor2.setVisibility(View.VISIBLE);
                            ledIndicator.setVisibility(View.VISIBLE);
                            distanceTextView.setVisibility(View.VISIBLE);
                            waveView.setVisibility(View.VISIBLE);
                            percentageTextView.setVisibility(View.VISIBLE);
                            motor1.setVisibility(View.VISIBLE);
                            motor2.setVisibility(View.VISIBLE);
                            voltageview.setVisibility(View.VISIBLE);
                            citySpinner.setVisibility(View.VISIBLE);
                            tankIdSpinner.setVisibility(View.VISIBLE);
                            ledIndicator2.setVisibility(View.VISIBLE);
                            ledIndicator2.setVisibility(View.VISIBLE);
                            ledIndicator2.setVisibility(View.VISIBLE);
                            status.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(MainActivity.this, "Data for the selected tank ID does not exist", Toast.LENGTH_SHORT).show();
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (!mHandler.hasCallbacks(mSendZeroRunnable)) {
                                mHandler.postDelayed(mSendZeroRunnable, 60000); // Start sending 0 after 1 minute
                            }// This will send the value 0 immediately
                        }


                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle database error
                        Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };
                switchMotor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton switchMotor, boolean isChecked) {
                         tankRef.child("switch_state").setValue(isChecked ? 1 : 0);

                    }
                });
                switchMotor2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton switchMotor, boolean isChecked) {
                        tankRef.child("switch_state2").setValue(isChecked ? 1 : 0);
                    }
                });
                tankRef.addValueEventListener(tankDataValueEventListener);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle when nothing is selected
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel Name";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void setUpSwitchListener() {
        switchMotor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton switchMotor, boolean isChecked) {
                // Update switch state in Firebase database
                tankRef.child("switch_state").setValue(isChecked ? 1 : 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cityValueEventListener != null) {
            DatabaseReference cityRef = FirebaseDatabase.getInstance().getReference("users").child(USERNAME).child("tank_id");
            cityRef.removeEventListener(cityValueEventListener);
        }
        if (tankDataValueEventListener != null && tankRef != null) {
            tankRef.removeEventListener(tankDataValueEventListener);
        }
    }

    private int notificationIdCounter = 0; // Counter for generating unique notification IDs
    int lastNotificationType;
    int currentNotificationType;
    int lastNotificationType2;
    int currentNotificationType2;
    private void sendNotification(String alert,String tankid ,String time , String city, int noti) {
        int uniqueNotificationId = notificationIdCounter++; // Generate unique ID for each notification
        currentNotificationType = noti;


        if ((currentNotificationType != lastNotificationType)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.img)
                    .setSound(notificationSound)
                    .setVibrate(vibe)
                    .setContentTitle(city + " (" + tankid + ")   " + alert)
                    .setContentText("Water_Level :- " + waterPercentage + "%    Tm " + (time))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(uniqueNotificationId, builder.build());
            }



            lastNotificationType = noti;

        }
    }
    private void sendNotification2(String alert,String tankid ,String time , String city, int noti) {
        int uniqueNotificationId = notificationIdCounter++; // Generate unique ID for each notification
        currentNotificationType2 = noti;


        if ((currentNotificationType2 != lastNotificationType2)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.img)
                    .setSound(notificationSound)
                    .setVibrate(vibe)
                    .setContentTitle(city + " (" + tankid + ")   " + alert)
                    .setContentText("Water_Level :- " + waterPercentage + "%    Tm " + (time))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);


            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(uniqueNotificationId, builder.build());
            }



            lastNotificationType2 = noti;

        }
    }




    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            Intent mainIntent = new Intent(MainActivity.this, activity_settings.class);

            mainIntent.putExtra("username", USERNAME);

            startActivity(mainIntent);
            return true;
        }
        if (id == R.id.add_tank) {

            Intent mainIntent = new Intent(MainActivity.this, add_tank.class);

            mainIntent.putExtra("username", USERNAME);

            startActivity(mainIntent);
            return true;
        }
        if (id == R.id.delete_tank) {

            Intent mainIntent = new Intent(MainActivity.this, delete_tank.class);

            mainIntent.putExtra("username", USERNAME);

            startActivity(mainIntent);
            return true;
        }
        if (id == R.id.Log_out) {
            // Clear SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Navigate to LoginActivity
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(mainIntent);

            // Finish MainActivity to prevent going back to it with back button
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

