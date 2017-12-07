package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.sync.MeetupSyncer;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;
import com.dhrw.sitwithus.view.ProfileListAdapter;
import com.dhrw.sitwithus.view.ProfileRetrieverAdapter;

import java.util.ArrayList;
import java.util.List;

public class MeetupActivity extends Activity {

    private MeetupSyncer syncer;
    private ProfileListAdapter meetupArrayAdapter;
    private List<String> members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inmeetup);

        members = new ArrayList<>();

        meetupArrayAdapter = new ProfileListAdapter(this,
                Preferences.getUserKey(this));
        ((ListView) findViewById(R.id.usersInMeetup)).setAdapter(meetupArrayAdapter);

        syncer = new MeetupSyncer(Preferences.getUserKey(this)) {
            @Override
            protected void onMembersChanged(List<String> usernames) {
                members = usernames;
                meetupArrayAdapter.retrieveProfiles(usernames);
            }
        };

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        syncer.stop();
    }
}
