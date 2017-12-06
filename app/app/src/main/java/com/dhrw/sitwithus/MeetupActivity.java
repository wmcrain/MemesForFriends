package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.sync.MeetupSyncer;
import com.dhrw.sitwithus.util.Preferences;

public class MeetupActivity extends Activity {

    private MeetupSyncer syncer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inmeetup);

        syncer = new MeetupSyncer(Preferences.getUserKey(this));
        syncer.start();

        //
        findViewById(R.id.inmeetupSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetupActivity.this,
                        MeetupSearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //
        findViewById(R.id.inmeetupLeave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add are you sure dialog box

                //
                ServerRequest.createLeaveMeetupRequest(
                        Preferences.getUserKey(MeetupActivity.this)).sendRequest();

                // TODO: Stop searching of the user is searching

                syncer.stop();
                finish();
            }
        });
    }


}
