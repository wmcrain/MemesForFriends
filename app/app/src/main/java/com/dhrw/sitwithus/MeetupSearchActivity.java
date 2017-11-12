package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MeetupSearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meetup_search.xml);

        ImageButton backbutton = (ImageButton) findViewById(R.id.backbuttonSearch);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MeetupSearchActivity.this, MainActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        ImageButton stopSearch = (ImageButton) findViewById(R.id.stopSearch);
        stopSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //stop the searching thread and take user out of screen like the backbutton when
                //the thread has been stopped
            }
        });

        //code for swiping needs to be added
        //code for swapping between already swiped users and to be swiped users needs to be added

    }
}
