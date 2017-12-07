package com.dhrw.sitwithus.server;

import com.dhrw.sitwithus.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MeetupData {

    public final List<String> usernames;

    MeetupData(JSONObject object) throws JSONException {

        // Retrieve the usernames of the users in the meetup
        usernames = new ArrayList<>();
        JSONArray jsonUsernames = object.getJSONArray(Keys.USERNAME);
        for (int j = 0; j < jsonUsernames.length(); j++) {
            usernames.add(jsonUsernames.getString(j));
        }
    }
}
