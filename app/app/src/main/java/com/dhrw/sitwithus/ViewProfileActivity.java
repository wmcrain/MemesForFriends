package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;

import org.json.JSONException;
import org.json.JSONObject;


public class ViewProfileActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        ServerRequest getProfile = ServerRequest.createGetProfileRequest(
                Preferences.getUserKey(this),
                Keys.USERNAME);

        final TextView nameView = (TextView) findViewById(R.id.name_age);
        final ImageView pic = (ImageView) findViewById(R.id.viewProfilePic);
        final TextView bio = (TextView) findViewById(R.id.viewProfileBio);

        getProfile.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

                ServerResponse profile = responseMessage.getDictArray(Keys.PROFILE).get(0);

                nameView.setText(profile.getString(Keys.FIRST_NAME) + " "
                        + profile.getString(Keys.LAST_NAME));

                if (profile.has(Keys.BIO)) {
                    bio.setText(profile.getString(Keys.BIO));
                }

                if (profile.has(Keys.PICTURE)) {
                    pic.setImageBitmap(profile.getImage(Keys.PICTURE));
                }
            }
        });

    }
}
