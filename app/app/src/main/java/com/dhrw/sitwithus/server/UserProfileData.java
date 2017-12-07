package com.dhrw.sitwithus.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.dhrw.sitwithus.R;
import com.dhrw.sitwithus.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfileData {

    public final String userKey;
    public final String username;
    public final String firstName;
    public final String lastName;
    public final String phoneNumber;
    public final String bio;
    public final Bitmap picture;

    UserProfileData(JSONObject object) throws JSONException {
        userKey = object.getString(Keys.USER_KEY);

        // Retrieve the username and the name of the user
        username = object.getString(Keys.USERNAME);
        firstName = object.getString(Keys.FIRST_NAME);
        lastName = object.getString(Keys.LAST_NAME);
        phoneNumber = object.getString(Keys.PHONE_NUMBER);

        // Retrieve the profile page assets of the user
        bio = object.getString(Keys.BIO);
        picture = object.has(Keys.PICTURE) ?
                EncodedBitmap.toBitmap(object.getString(Keys.PICTURE)) : null;
    }

    /** */
    public Bitmap getPicture(Context context) {
        return (picture != null) ? picture :
                BitmapFactory.decodeResource(context.getResources(), R.mipmap.david);
    }

    public boolean isFriend() {
        return true; // TODO: Change to check if the phone number is present
    }

    public boolean isSelf() {
        return false;
    }
}
