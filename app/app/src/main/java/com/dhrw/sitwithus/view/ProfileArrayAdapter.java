package com.dhrw.sitwithus.view;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.dhrw.sitwithus.server.SearchMeetupData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.util.Keys;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class ProfileArrayAdapter extends ArrayAdapter {

    private LinkedHashMap<String, UserProfileData> usernameProfiles;
    private String userKey;

    public ProfileArrayAdapter(Context context, int layout, String userKey) {
        super(context, layout);
        this.usernameProfiles = new LinkedHashMap<>();
        this.userKey = userKey;
    }

    public final void retrieveProfiles(List<String> usernames) {

        // Retrieve a list of all the usernames for which the profile has not been cached
        ArrayList<String> newUsernames = new ArrayList<>();
        for (String username : usernames) {
            if (!usernameProfiles.containsKey(username)) {
                newUsernames.add(username);
            }
        }

        //
        if (newUsernames.size() > 0) {
            ServerRequest getProfileRequest = ServerRequest.createGetProfileRequest(
                    userKey, newUsernames);

            getProfileRequest.sendRequest(new ServerRequest.Callback() {
                @Override
                public void onSuccess(int responseCode, ServerResponse responseMessage) {
                    super.onSuccess(responseCode, responseMessage);

                    // Cache the
                    for (UserProfileData profileData :
                            responseMessage.getProfileArray(Keys.PROFILE)) {
                        usernameProfiles.put(profileData.username, profileData);
                    }

                    //
                    notifyDataSetChanged();
                }
            });
        } else {
            notifyDataSetChanged();
        }
    }

    protected final UserProfileData getProfile(String username) {
        return usernameProfiles.get(username);
    }
}
