package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MeetupSearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_search);
        //push the user that called this to the server
        //run a constant thread that updates every ~60s that pushes new user information
        //pull users from database that are located within certain area of the user

        Button stopSearch = (Button) findViewById(R.id.stopSearch);
        stopSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                //stop the searching thread and take user out of screen like a  when
                //the thread has been stopped
            }
        });
        Button matched = (Button) findViewById(R.id.matchedSearch);
        matched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MeetupSearchActivity.this, MeetupSearchPendingActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        Button unmatched = (Button) findViewById(R.id.unmatchedSearch);
        unmatched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        //code for swiping needs to be added
        //code for swapping between already swiped users and to be swiped users needs to be added

    }
}
