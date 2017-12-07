package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
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

        final Switch requestFriend = (Switch) findViewById(R.id.requestFriend);

        getProfile.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

                final UserProfileData profile = responseMessage.getProfileArray(Keys.PROFILE).get(0);;

                nameView.setText(profile.firstName+ " " + profile.lastName);

                bio.setText(profile.bio);

                pic.setImageBitmap(profile.getPicture(ViewProfileActivity.this));

                blockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupBlock(profile);
                    }
                });

                requestFriend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                         ServerRequest.createToggleFriendRequest(
                                 Preferences.getUserKey(ViewProfileActivity.this),
                                 profile.userKey, isChecked).sendRequest();
                    }
                });
            }
        });

    }
    public void popupBlock(final UserProfileData profile){
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setMessage("Are you sure you want to block this user?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ServerRequest.createBlockRequest(
                                Preferences.getUserKey(ViewProfileActivity.this),
                                profile.userKey).sendRequest();
                        //TODO: If user is a friend, remove them from the user's friends list as well
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();


    }
}
