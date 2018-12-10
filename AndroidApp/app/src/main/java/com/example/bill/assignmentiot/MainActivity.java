package com.example.bill.assignmentiot;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.sleep;
import static com.example.bill.assignmentiot.Notify.CHANNEL_1_ID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    public static final String MAC_01 = "00:18:E4:00:11:E4";
//    private static boolean readPlantDB = false;
    public static final String POT_ID = "potID";
    public static final String POT_VALUE = "someValue";
    public static final String POT_TIME = "timeCurrent";
    public static final String POT_NAME = "potName";
    public static final String POT_TYPE = "potType";

    public static final String NOTIFICATION_TITLE = "Smart Pot";
    public static final String NOTIFICATION_MESSAGE = "Some of your plants are drying out, Water them to keep them healthy!";
    private static boolean notiFlag = true;

    public static final String topicHumid = "humid";
    public static final String topicLog = "log";
    public static final String topicCmd = "command";

    public static MqttControl mqttControl;
    public static MqttControl mqttControlWriteHumid;
    public static MqttControl mqttControlWriteLog;

    // Declare Notification Compat for API 25 and lower
    private NotificationManagerCompat notificationManager;

    DatabaseReference databasePot;
    DatabaseReference databasePlants;
    static Integer type0_min, type1_min, type2_min;

    ListView listViewPots;
    List<Param> Pots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Custom Toolbar for notification Icon
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Notification for API 25 or lower
        notificationManager = NotificationManagerCompat.from(this);
        // Getting data path on database
        databasePot = FirebaseDatabase.getInstance().getReference("db").child("user0").child(MAC_01);

        // Setting Condition for notification
        databasePlants = FirebaseDatabase.getInstance().getReference("plant");
//        databasePlants.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Plants plant0 = dataSnapshot.child("0").getValue(Plants.class);
//                if (plant0 != null){ type0_min = plant0.getHumid_min(); } // 80
//                else { type0_min = 10; }
//                plant0 = dataSnapshot.child("1").getValue(Plants.class);
//                if (plant0 != null){ type1_min = plant0.getHumid_min(); } // 40
//                else { type0_min = 20; }
//                plant0 = dataSnapshot.child("2").getValue(Plants.class);
//                if (plant0 != null){ type2_min = plant0.getHumid_min(); } // 20
//                else { type0_min = 30; }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d("Plant DB", "read failed");
//            }
//        });
        Log.d("temp3", type2_min + "");
        // Try setting value in firebase
//        Param pot = new Param("Pot1","25", "01/12/2018 13:49:30", "0");
//        databasePot.child("1").setValue(pot);

        Pots = new ArrayList<>();

        // Inflate view on list
        listViewPots = (ListView) findViewById(R.id.listPots);

        // Register to MQTT
//        try {
//            Log.d("HELLO","HELLO");
//            mqttControl = new MqttControl(topicCmd, "E", false);
////            mqttControlWriteHumid = new MqttControl(topicHumid, "F", true);
////            mqttControlWriteLog = new MqttControl(topicLog, "G", false);
//        } catch (MqttException e) {
//            Log.d("HELLO","Error tum lum");
//            e.printStackTrace();
//        }

        Log.d("HELLO","HELLO");

        // Get into Detail View on tap
        listViewPots.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Getting the selected pot
                Param param = Pots.get(position);

                // Creating an intent
                Intent intent = new Intent(getApplicationContext(), DetailView.class);

                // Putting parameters to intent for latter use
                intent.putExtra(POT_ID, param.getID());
                intent.putExtra(POT_VALUE, param.getValue());
                intent.putExtra(POT_NAME, param.getName());
                intent.putExtra(POT_TYPE, param.getType());

                // Starting the activity with intent
                startActivity(intent);
            }
        });

    }
    // Setting notification Icon on Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.notification_menu, menu);
        return true;
    }
    // Action when tapping icon
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.itemNotification:
                if (notiFlag){
                    Drawable notiOff = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_notifications_off, null);
                    item.setIcon(notiOff);
                    notiFlag = false;

                    Toast.makeText(this, "Notification turned off", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else{
                    Drawable notiOn = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_notifications,null);
                    item.setIcon(notiOn);
                    notiFlag = true;

                    Toast.makeText(this, "Notification turned on", Toast.LENGTH_SHORT).show();
                    return true;
                }
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        databasePot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Pots.clear();

                for(DataSnapshot paramSnapshot : dataSnapshot.getChildren()){
                    Param pot = paramSnapshot.getValue(Param.class);

                    Pots.add(pot);
                    if (notiFlag) {
                        if (pot != null) {
                            Integer a = Integer.parseInt(pot.getRawValue());
                            // Humidity threshold for Notification
//                            switch (pot.getType()){
//                                case "0":
//                                    if (a<80){sendOnChannel1(); Log.d("go in here","yes");break;}
//                                case "1":
//                                    if (a<40) {
//                                        sendOnChannel1();
//                                        break;
//                                    }
//                                case "2":
//                                    if (a<20) {
//                                        sendOnChannel1();
//                                        break;
//                                    }
//                                default: break;
//                            }
                            if (a < 20) {sendOnChannel1();}
                        }
                    }

                }

                PotList adapter = new PotList(MainActivity.this, Pots);
                listViewPots.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log.w(this, "Failed to read value.", error.toException());
            }
        });
    }

    // For sending out notification
    public void sendOnChannel1(){
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);
        // Set Action
        Intent broadcastIntent = new Intent(this, NotificationReceiver.class);
        broadcastIntent.putExtra("toastMessage", "Control sent");
        PendingIntent actionIntent = PendingIntent.getBroadcast(this,0,broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Icon in Notification
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.plant);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_android_black)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(NOTIFICATION_MESSAGE)
                .setLargeIcon(largeIcon)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.long_dummy_text))
                        .setBigContentTitle("Smart Pot")
                        .setSummaryText("Pot Status"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(Color.BLUE)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .addAction(R.mipmap.ic_launcher,"Water",actionIntent)
                .build();

        notificationManager.notify(1,notification);
    }
}
