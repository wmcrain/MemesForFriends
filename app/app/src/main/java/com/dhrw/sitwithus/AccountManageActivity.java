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
        setContentView(R.layout.activity_accountmanagement.xml);

        Button editProfile = findViewById(R.id.editProfileButtonManage);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this, EditProfileActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        Button meetupHistory = findViewById(R.id.meetupHistoryButtonManage);
        meetupHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this, ViewMeetupHistoryActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

     /* Commenting out until Hazem commits his Friends List stuff
      Button friendslist = findViewById(R.id.friendslistManage);
        friendslist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this, FriendsListActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });*/
        Button blocklist = findViewById(R.id.blocklistManage);
        blocklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(AccountManageActivity.this, BlockListActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }
}
