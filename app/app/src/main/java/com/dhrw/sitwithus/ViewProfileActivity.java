package com.dhrw.sitwithus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;


public class ViewProfileActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        ServerRequest getProfile = ServerRequest.createGetProfileRequest(
                Preferences.getUserKey(this),
                getIntent().getExtras().getString(Keys.USERNAME));

        final TextView nameView = (TextView) findViewById(R.id.name_age);
        final ImageView pic = (ImageView) findViewById(R.id.viewProfilePic);
        final TextView bio = (TextView) findViewById(R.id.viewProfileBio);

        final Button blockButton = (Button) findViewById(R.id.blockUser);

        getProfile.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

                final UserProfileData profile = responseMessage.getProfileArray(Keys.PROFILE).get(0);

                nameView.setText(profile.firstName+ " " + profile.lastName);

                bio.setText(profile.bio);

                if (profile.picture != null) {
                    pic.setImageBitmap(profile.picture);
                }

                blockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO: Popup are you sure?
                        ServerRequest.createBlockRequest(
                                Preferences.getUserKey(ViewProfileActivity.this),
                                profile.userKey).sendRequest();
                    }
                });
            }
        });

    }
}
