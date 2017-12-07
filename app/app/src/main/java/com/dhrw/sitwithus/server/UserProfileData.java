package com.dhrw.sitwithus.server;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.dhrw.sitwithus.EditProfileActivity;
import com.dhrw.sitwithus.R;
import com.dhrw.sitwithus.ViewFriendProfileActivity;
import com.dhrw.sitwithus.ViewProfileActivity;
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

        phoneNumber = object.has(Keys.PHONE_NUMBER) ?
                object.getString(Keys.PHONE_NUMBER) : null;

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
        return phoneNumber != null;
    }

    public void viewProfile(Context context, String userKey) {
        if (userKey.equals(this.userKey)) {
            Intent viewProfile = new Intent(context, EditProfileActivity.class);
            context.startActivity(viewProfile);
        } else {
            if (isFriend()) {
                Intent viewProfile = new Intent(context, ViewFriendProfileActivity.class);
                viewProfile.putExtra(Keys.USERNAME, username);
                context.startActivity(viewProfile);
            } else {
                Intent viewProfile = new Intent(context, ViewProfileActivity.class);
                viewProfile.putExtra(Keys.USERNAME, username);
                context.startActivity(viewProfile);
            }
        }
    }
}
