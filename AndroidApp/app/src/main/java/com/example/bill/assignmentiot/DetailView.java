package com.example.bill.assignmentiot;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

import static android.os.SystemClock.sleep;
import static com.example.bill.assignmentiot.MainActivity.MAC_01;

public class DetailView extends AppCompatActivity {

    TextView textViewPot; // Name of the Pot
    TextView textViewHumid;
    TextView textViewPlant;
    TextView textViewDate;
    ImageView imageWaterCan;


    DatabaseReference databasePlants;
    DatabaseReference databaseControl;
    DatabaseReference databasePot;
//    private Param current = new Param();

    static Integer humidMax;
    static Integer humidMin;
    String id;

    // LineChart Graph
    private LineChart lineChart;
    ArrayList<Entry> yData;
    DatabaseReference mPostReference;
    ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        // Setting up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Linking to Design
        textViewPot = (TextView) findViewById(R.id.textPot);
        textViewHumid = (TextView) findViewById(R.id.textHumid);
        textViewPlant = (TextView) findViewById(R.id.textPlant);
        textViewDate = (TextView) findViewById(R.id.textViewID);

        // Receive Intent from List View
        Intent intent = getIntent();
        final String pot = intent.getStringExtra(MainActivity.POT_NAME);
        String humid = intent.getStringExtra(MainActivity.POT_VALUE);
        String date = intent.getStringExtra(MainActivity.POT_TIME);
        String type = intent.getStringExtra(MainActivity.POT_TYPE);
        id = intent.getStringExtra(MainActivity.POT_ID);
        Toast.makeText(this, type, Toast.LENGTH_SHORT).show();

        textViewPot.setText(pot);
        textViewHumid.setText(humid);
        textViewDate.setText(date);

        databasePlants = FirebaseDatabase.getInstance().getReference("plant").child(type);
        databasePlants.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name;
                Plants plant = dataSnapshot.getValue(Plants.class);
                if (plant != null){
                    name = plant.getName();
                    humidMax = plant.getHumid_max();
                    humidMin = plant.getHumid_min();
                }
                else {
                    name = "undefined";
                    humidMax = 60;
                    humidMin = 10;
                }
                textViewPlant.setText(name);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailView.this, "read plant failed", Toast.LENGTH_SHORT).show();
            }
        });

        lineChart = (LineChart) findViewById(R.id.LineChart);

        mPostReference = FirebaseDatabase.getInstance().getReference("db").child("user0").child(MAC_01).child(pot).child("data");
        databaseControl = FirebaseDatabase.getInstance().getReference("db").child("user0").child(MAC_01).child(pot);
//        databaseControl.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                current = dataSnapshot.getValue(Param.class);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d("Current", "read failed");
//            }
//        });

        imageWaterCan = (ImageView) findViewById(R.id.imageViewWaterCanIcon);
        imageWaterCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetailView.this, "Control sent", Toast.LENGTH_SHORT).show();
                String code = "C" + id + humidMax + MAC_01;Log.d("code","C"+ id + humidMax +MAC_01);
                Commands key = new Commands(code);
                databaseControl.child("commands").setValue(key);
//                try{
//                    MainActivity.mqttControl.sendmessage(code,MainActivity.topicCmd);
//                } catch (MqttException e){
//
//                }

            }
        });
    }

    // Graphing starts when detail view is inflated
    @Override
    protected void onStart() {
        super.onStart();
        mPostReference.addValueEventListener(valueEventListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // yAxis -> Humidity; xAxis -> Date;
                yData = new ArrayList<>();

                float i =0;
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    i=i+1;
                    String SV = ds.child("value").getValue().toString();
                    Float SensorValue = Float.parseFloat(SV);
                    yData.add(new Entry(i,SensorValue));
                }
                final LineDataSet lineDataSet = new LineDataSet(yData,"Humidity");
                LineData data = new LineData(lineDataSet);

                lineChart.setData(data);
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DetailView.this, "Fail to load post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Toolbar processing
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.automation_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
//            case R.id.nowWater:
//                Toast.makeText(this, "Changed to smart-water mode", Toast.LENGTH_SHORT).show();
//
//
//                return true;
            case R.id.autoWater:
                Toast.makeText(this, "Changed to auto-water mode", Toast.LENGTH_SHORT).show();
//                current.setAuto(true);
//                databaseControl.setValue(current);
                String codeB = "B" + id + humidMax + humidMin + MAC_01;
                Commands keyB = new Commands(codeB);
                databaseControl.child("commands").setValue(keyB);
//                try{
//                    MainActivity.mqttControl.sendmessage(codeB,MainActivity.topicCmd);
//                } catch (MqttException e){
//
//                }
                return true;
            case R.id.manualWater:
                Toast.makeText(this, "Changed to manual-water mode", Toast.LENGTH_SHORT).show();
//                current.setAuto(false);
//                databaseControl.setValue(current);
                String codeR = "R" + id + "00" + MAC_01;
                Commands keyR = new Commands(codeR);
                databaseControl.child("commands").setValue(keyR);
//                try{
//                    MainActivity.mqttControl.sendmessage(codeR,MainActivity.topicCmd);
//                } catch (MqttException e){
//
//                }

                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

}
