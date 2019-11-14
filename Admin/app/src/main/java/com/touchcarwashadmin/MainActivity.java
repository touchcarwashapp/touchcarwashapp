package com.touchcarwashadmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    UserDatabaseHandler udb = new UserDatabaseHandler(this);
    final DatabaseHandler db=new DatabaseHandler(this);
    ImageView washvehicles,drivers,vehicles,offer,prices;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        washvehicles=findViewById(R.id.washvehicles);
        drivers=findViewById(R.id.drivers);
        vehicles=findViewById(R.id.vehicles);
        offer=findViewById(R.id.offer);
        prices=findViewById(R.id.prices);
        if (db.getscreenwidth().equalsIgnoreCase("")) {
            int width = getResources().getDisplayMetrics().widthPixels;
            db.addscreenwidth(width+"");
        }

        if (udb.get_userid().equalsIgnoreCase("")) {
            startActivity(new Intent(getApplicationContext(), Registration.class));
            finish();
            return;
        }


        washvehicles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),Wash_Vehicles.class));
            }
        });
        drivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),DriverList.class));
            }
        });
        vehicles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Vehicle_Management.class));
            }
        });
        offer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Offer_Management.class));
            }
        });
        prices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),WashPrice_List.class));
            }
        });

    }
}
