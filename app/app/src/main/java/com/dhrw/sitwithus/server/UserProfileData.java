package com.dhrw.sitwithus.server;

import android.graphics.Bitmap;

import com.dhrw.sitwithus.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UserProfileData implements Serializable {

    public final String userKey;
    public final String username;
    public final String firstName;
    public final String lastName;
    public final String bio;
    public final Bitmap picture;

    UserProfileData(JSONObject object) throws JSONException {
        userKey = object.getString(Keys.USER_KEY);

        // Retrieve the username and the name of the user
        username = object.getString(Keys.USERNAME);
        firstName = object.getString(Keys.FIRST_NAME);
        lastName = object.getString(Keys.LAST_NAME);

        // Retrieve the profile page assets of the user
        bio = object.getString(Keys.BIO);
        picture = object.has(Keys.PICTURE) ?
                EncodedBitmap.toBitmap(object.getString(Keys.PICTURE)) : null;
    }
}
