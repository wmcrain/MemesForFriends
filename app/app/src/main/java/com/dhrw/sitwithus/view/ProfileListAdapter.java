package com.dhrw.sitwithus.view;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhrw.sitwithus.BlockListActivity;
import com.dhrw.sitwithus.R;
import com.dhrw.sitwithus.ViewFriendProfileActivity;
import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.util.Keys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ProfileListAdapter extends ProfileRetrieverAdapter {

    private List<String> usernames;
    private List<UserProfileData> sortedData;

    private Context context;

    public ProfileListAdapter(Context context, String userKey) {
        super(context, R.layout.activity_friend_entry, userKey);
        this.usernames = new ArrayList<>();
        this.context = context;
        this.sortedData = new ArrayList<>();

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final UserProfileData profile = sortedData.get(position);

        //
        View view = convertView == null ? LayoutInflater.from(context)
                .inflate(R.layout.activity_friend_entry, null) : convertView;
        TextView name = (TextView) view.findViewById(R.id.friend_entry_name);
        name.setText(profile.firstName + " " + profile.lastName);

        //
        ImageView pic = (ImageView) view.findViewById(R.id.friend_entry_picture);
        pic.setImageBitmap(profile.getPicture(context));

        //
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewProfile = new Intent(context, ViewFriendProfileActivity.class);
                viewProfile.putExtra(Keys.USERNAME, profile.username);
                context.startActivity(viewProfile);
            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return usernames.size();
    }

    @Override
    public void retrieveProfiles(List<String> usernames) {
        super.retrieveProfiles(usernames);

        this.usernames.clear();
        this.usernames.addAll(usernames);
    }

    @Override
    protected void onRetrieved() {
        TreeMap<String, UserProfileData> fullNameMap = new TreeMap<>();

        for (String username : usernames) {
            UserProfileData profile = getProfile(username);
            fullNameMap.put(profile.firstName + " " + profile.lastName, profile);
        }

        sortedData.clear();
        for (Map.Entry<String, UserProfileData>  fullNameEntry : fullNameMap.entrySet()) {
            Log.d("Sit", fullNameEntry.getValue().username);
            sortedData.add(fullNameEntry.getValue());
        }
    }
}

