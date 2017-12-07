package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dhrw.sitwithus.server.BlockedUserData;
import com.dhrw.sitwithus.server.MeetupData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;
import com.dhrw.sitwithus.view.CircleCharacterView;
import com.dhrw.sitwithus.view.ProfileArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewMeetupHistoryActivity extends Activity {

    private List<MeetupData> meetups;
    private MeetupHistoryAdapter meetupAdapter;

    class MeetupHistoryAdapter extends ProfileArrayAdapter {

        MeetupHistoryAdapter() {
            super(ViewMeetupHistoryActivity.this, R.layout.view_meetup_history_entry,
                    Preferences.getUserKey(ViewMeetupHistoryActivity.this));
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final MeetupData meetupData = meetups.get(position);
            View view = convertView == null ? LayoutInflater.from(ViewMeetupHistoryActivity.this)
                    .inflate(R.layout.view_meetup_history_entry, null) : convertView;

            UserProfileData user1 = getProfile(meetupData.usernames.get(0));
            UserProfileData user2 = getProfile(meetupData.usernames.get(1));

            TextView name = (TextView) view.findViewById(R.id.profileMatchEntry);
            name.setText(user1.firstName + " " + user1.lastName);

            ImageView pic = (ImageView) view.findViewById(R.id.match_entry_picture);
            pic.setImageBitmap(user1.getPicture(ViewMeetupHistoryActivity.this));

            if (meetupData.usernames.size() == 2) {
                ImageView pic2 = (ImageView) view.findViewById(R.id.match_entry_picture_2);
                pic2.setImageBitmap(user2.getPicture(ViewMeetupHistoryActivity.this));
                pic2.setVisibility(View.VISIBLE);
            } else {

                CircleCharacterView circleView = (CircleCharacterView)
                        view.findViewById(R.id.match_entry_more);

                char c = (char) ('0' + Math.min(meetupData.usernames.size() - 1, 9));
                circleView.setLetter(c);
                circleView.setVisibility(View.VISIBLE);
            }

            return view;
        }

        @Override
        public int getCount() {
            return meetups.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetuphistory);

        meetups = new ArrayList<>();

        meetupAdapter = new MeetupHistoryAdapter();
        ((ListView) findViewById(R.id.listEntriesMeetupHistory)).setAdapter(meetupAdapter);

        ServerRequest.createMeetupHistoryRequest(Preferences.getUserKey(this))
                .sendRequest(new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);
                        meetups = responseMessage.getMeetupArray(Keys.HISTORY);

                        // Retrieve the profiles of all the users
                        ArrayList<String> usernames = new ArrayList<>();
                        for (MeetupData meetupData : meetups) {
                            usernames.addAll(meetupData.usernames);
                        }

                        // Retrieve the profiles of all users in the meetup history
                        meetupAdapter.retrieveProfiles(usernames);
                    }
                });
    }
}
