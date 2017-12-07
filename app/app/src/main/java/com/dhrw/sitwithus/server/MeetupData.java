package com.dhrw.sitwithus.server;

import android.util.Log;

import com.dhrw.sitwithus.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MeetupData {

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public final List<String> usernames;
    public final Date timeFormed;

    MeetupData(JSONObject object) throws JSONException {

        // Retrieve the usernames of the users in the meetup
        usernames = new ArrayList<>();
        JSONArray jsonUsernames = object.getJSONArray(Keys.USERNAME);
        for (int j = 0; j < jsonUsernames.length(); j++) {
            usernames.add(jsonUsernames.getString(j));
        }

        Date d;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            d = dateFormat.parse(object.getString(Keys.TIME_FORMED));
        } catch (ParseException e) {
            d = new Date();
        }

        timeFormed = d;
    }
}
