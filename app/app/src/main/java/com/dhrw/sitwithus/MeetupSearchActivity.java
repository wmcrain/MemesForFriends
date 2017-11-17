package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dhrw.sitwithus.data.UserProfile;
import com.dhrw.sitwithus.data.SearchMeetup;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MeetupSearchActivity extends Activity {

    LinkedHashMap<String, UserProfile> usernameProfiles;
    List<SearchMeetup> searchMeetups;

    private MeetupSearcher searcher;

    private class UserSearchAdapter extends ArrayAdapter {

        public UserSearchAdapter() {
            super(MeetupSearchActivity.this, R.layout.view_match_entry);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final SearchMeetup searchMeetup = searchMeetups.get(position);
            final UserProfile profile = usernameProfiles.get(searchMeetup.usernames.get(0));

            View view = LayoutInflater.from(MeetupSearchActivity.this).inflate(R.layout.view_match_entry, null);
            TextView name = (TextView) view.findViewById(R.id.profileMatchEntry);
            name.setText(profile.firstName + " " + profile.lastName);

            ImageView pic = (ImageView) view.findViewById(R.id.match_entry_picture);
            if (profile.picture != null) {
                pic.setImageBitmap(profile.picture);
            } else {
                pic.setImageResource(R.mipmap.david);
            }

            TextView GPS = (TextView) view.findViewById(R.id.distanceMatchEntry);
            String q = String.valueOf(searchMeetup.distance) + " km";
            GPS.setText(q);

            name.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent viewProfile = new Intent(MeetupSearchActivity.this,
                            ViewProfileActivity.class);
                    viewProfile.putExtra(Keys.USERNAME, profile.username);
                    startActivity(viewProfile);
                }
            });

          /*Switch toggleMatched = (Switch) view.findViewById(R.id.toggleMatchEntry);
            toggleMatched.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //move user out of current listview
                    //place user in "pending" listview
                    //either queue to notify server that user has matched, or immediately push that user
                    //has toggled a match
                }

            });*/
            return view;
        }

        @Override
        public int getCount() {
            return searchMeetups.size();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_search);
        //push the user that called this to the server
        //run a constant thread that updates every ~60s that pushes new user information
        //pull users from database that are located within certain area of the user

        usernameProfiles = new LinkedHashMap<>();
        searchMeetups = new ArrayList<>();

        final ListView listView = (ListView) findViewById(R.id.usersSearch);
        final UserSearchAdapter adapter = new UserSearchAdapter();

        //
        searcher = new MeetupSearcher(Preferences.getUserKey(this)) {
            @Override
            public void onResultUpdate(List<SearchMeetup> nearbyMeetups) {
                searchMeetups = nearbyMeetups;

                // Retrieve a list of all the user names for which the profile has not been retrieved
                ArrayList<String> newUsernames = new ArrayList<>();
                for (SearchMeetup searchMeetup : nearbyMeetups) {
                    for (String username : searchMeetup.usernames) {
                        if (!usernameProfiles.containsKey(username)) {
                            newUsernames.add(username);
                        }
                    }
                }

                ServerRequest getProfiles = ServerRequest.createGetProfileRequest(
                        Preferences.getUserKey(MeetupSearchActivity.this), newUsernames);

                getProfiles.sendRequest(new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);

                        for (UserProfile profile : responseMessage.getProfileArray(Keys.PROFILE)) {
                            usernameProfiles.put(profile.username, profile);
                        }

                        //
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        };

        searcher.start();
        listView.setAdapter(adapter);

        Button stopSearch = (Button) findViewById(R.id.stopSearch);
        stopSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searcher.stop();

                Intent myIntent = new Intent(MeetupSearchActivity.this, MainActivity.class);
                startActivity(myIntent);
                //stop the searching thread and take user out of screen when
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
