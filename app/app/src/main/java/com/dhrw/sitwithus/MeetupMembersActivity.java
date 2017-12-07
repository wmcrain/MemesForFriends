package com.dhrw.sitwithus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;
import com.dhrw.sitwithus.view.ProfileListAdapter;

import java.util.List;

public class MeetupMembersActivity extends Activity{

    private ProfileListAdapter adapter;
    private List<String> usernames;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View empty = findViewById(R.id.friendsEmpty);
        ListView list = (ListView) findViewById(R.id.friend_list);
        list.setEmptyView(empty);
    }

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_meetup_members);

        usernames = getIntent().getStringArrayListExtra(Keys.USERNAME);

        final ListView listView = (ListView) findViewById(R.id.friend_list);

        adapter = new ProfileListAdapter(this,
                Preferences.getUserKey(this));
        listView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.retrieveProfiles(usernames);
    }
}
