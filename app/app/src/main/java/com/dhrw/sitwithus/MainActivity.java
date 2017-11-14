package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Button meetups = (Button) findViewById(R.id.meetupsMain);
        meetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, MeetupSearchActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        Button accmanage = (Button) findViewById(R.id.accmanageMain);
        accmanage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, AccountManageActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        Button logout = (Button) findViewById(R.id.logoutMain);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, UserLogoutActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        Button contactDevs = (Button) findViewById(R.id.contactdevsMain);
        contactDevs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, UserContactDevs.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }
}
