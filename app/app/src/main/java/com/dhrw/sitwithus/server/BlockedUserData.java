package com.dhrw.sitwithus.server;

import com.dhrw.sitwithus.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

public class BlockedUserData {

    public final String userKey;
    public final String username;
    public final String firstName;
    public final String lastName;

    BlockedUserData(JSONObject object) throws JSONException{
        userKey = object.getString(Keys.USER_KEY);
        username = object.getString(Keys.USERNAME);
        firstName = object.getString(Keys.FIRST_NAME);
        lastName = object.getString(Keys.LAST_NAME);
    }
}
