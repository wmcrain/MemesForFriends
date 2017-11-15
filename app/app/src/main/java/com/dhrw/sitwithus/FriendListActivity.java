package com.dhrw.sitwithus;

/**
 * Created by Hazem on 11/9/17.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dhrw.sitwithus.data.Profile;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends Activity {

    List<Profile> friends;

    String[] testArray = {"Hazem", "Alex", "Ryan", "David","Will","Abdul","Casey","Sara","Josh"};
    private class FriendArrayAdapter extends ArrayAdapter {

        public FriendArrayAdapter() {
            super(FriendListActivity.this, R.layout.activity_friend_entry);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final Profile profile = friends.get(position);

            View view = LayoutInflater.from(FriendListActivity.this).inflate(R.layout.activity_friend_entry, null);
            TextView name = (TextView) view.findViewById(R.id.friend_entry_name);
            name.setText(profile.firstName + " " + profile.lastName);

            ImageView pic = (ImageView) view.findViewById(R.id.friend_entry_picture);
            if (profile.picture != null) {
                pic.setImageBitmap(profile.picture);
            } else {
                pic.setImageResource(R.mipmap.david);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent viewProfile = new Intent(FriendListActivity.this,
                            ViewProfileActivity.class);
                    viewProfile.putExtra(Keys.USERNAME, profile.username);
                    startActivity(viewProfile);
                }
            });

            //View view = new View()
            return view;
        }

        @Override
        public int getCount() {
            return friends.size();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_friend_list);

        friends = new ArrayList<>();

        final ListView listView = (ListView) findViewById(R.id.friend_list);

        //
        ServerRequest getFriends = ServerRequest.createGetFriends(
                Preferences.getUserKey(this));

        final FriendArrayAdapter adapter = new FriendArrayAdapter();
        listView.setAdapter(adapter);

        //
        getFriends.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

                ServerRequest getProfiles = ServerRequest.createGetProfileRequest(
                        Preferences.getUserKey(FriendListActivity.this),
                        responseMessage.getStringArray(Keys.USERNAME));

                //
                getProfiles.sendRequest(new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);

                        friends = responseMessage.getProfileArray(Keys.PROFILE);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
}