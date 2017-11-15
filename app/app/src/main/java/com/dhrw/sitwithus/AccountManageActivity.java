package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class AccountManageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountmanagement);

        Button editProfile = (Button) findViewById(R.id.editProfileButtonManage);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this,
                        EditProfileActivity.class);
                startActivity(myIntent);
            }
        });

        Button meetupHistory = (Button) findViewById(R.id.meetupHistoryButtonManage);
        meetupHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this,
                        ViewMeetupHistoryActivity.class);
                startActivity(myIntent);
            }
        });

        Button friendslist = (Button) findViewById(R.id.friendslistManage);
        friendslist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this,
                        FriendListActivity.class);
                startActivity(myIntent);
            }
        });


        Button blocklist = (Button) findViewById(R.id.blocklistManage);
        blocklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this,
                        BlockListActivity.class);
                startActivity(myIntent);
            }
        });
    }
}
