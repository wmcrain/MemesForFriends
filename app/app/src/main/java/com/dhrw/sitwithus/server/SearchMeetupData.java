package com.dhrw.sitwithus.server;

import com.dhrw.sitwithus.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** */
public class SearchMeetupData {

    // The unique identifier of the searching meetup
    public final String entityKey;
    public final List<String> usernames;
    public final double distance;

    SearchMeetupData(JSONObject object) throws JSONException {
        this.entityKey = object.getString(Keys.SEARCH_KEY);

        // Retrieve the usernames of the users in the meetup
        usernames = new ArrayList<>();
        JSONArray jsonUsernames = object.getJSONArray(Keys.USERNAME);
        for (int j = 0; j < jsonUsernames.length(); j++) {
            usernames.add(jsonUsernames.getString(j));
        }

        this.distance = object.getDouble(Keys.DISTANCE);
    }
}
