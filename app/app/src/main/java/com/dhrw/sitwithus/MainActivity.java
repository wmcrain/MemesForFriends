package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dhrw.sitwithus.util.Preferences;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Button meetups = (Button) findViewById(R.id.meetupsMain);
        meetups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Make an InMeetup Activity
                //TODO: Take the user to this Activity
                //TODO: Implement the below code for the search button in InMeetup
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
                attemptLogout();
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

    public void attemptLogout(){
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)   //can't figure out how to make this text black
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Preferences.setUserKey(MainActivity.this, null);
                        Intent myIntent = new Intent(MainActivity.this, UserLoginActivity.class);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("No", null)
                .show();


    }
}
