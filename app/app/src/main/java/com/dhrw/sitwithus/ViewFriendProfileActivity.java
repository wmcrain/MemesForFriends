package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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


public class ViewFriendProfileActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend_profile);

        ServerRequest getProfile = ServerRequest.createGetProfileRequest(
                Preferences.getUserKey(this),
                getIntent().getExtras().getString(Keys.USERNAME));

        final TextView nameView = (TextView) findViewById(R.id.name_ageFriends);
        final ImageView pic = (ImageView) findViewById(R.id.viewProfilePicFriends);
        final TextView bio = (TextView) findViewById(R.id.viewProfileBioFriends);
        final TextView contact = (TextView) findViewById(R.id.contactInfo);

        final Button blockButton = (Button) findViewById(R.id.blockUserFriends);
        final Button removeButton = (Button) findViewById(R.id.removeFriendFriends);

        getProfile.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

                final UserProfileData profile = responseMessage.getProfileArray(Keys.PROFILE).get(0);;

                nameView.setText(profile.firstName+ " " + profile.lastName);

                bio.setText(profile.bio);

                if (profile.isFriend()) {
                    contact.setText(profile.phoneNumber);
                }

                pic.setImageBitmap(profile.getPicture(ViewFriendProfileActivity.this));

                blockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupBlock(profile);
                    }
                });

                removeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFriend(profile);
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
                                Preferences.getUserKey(ViewFriendProfileActivity.this),
                                profile.userKey).sendRequest();
                        //TODO: If user is a friend, remove them from the user's friends list as well
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    public void removeFriend(final UserProfileData profile){
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setMessage("Are you sure you want to remove this user as a friend?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ServerRequest.createToggleFriendRequest(
                                Preferences.getUserKey(ViewFriendProfileActivity.this),
                                profile.userKey, false).sendRequest();

                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

}