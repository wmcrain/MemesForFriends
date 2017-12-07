package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dhrw.sitwithus.server.EncodedBitmap;
import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;
import com.dhrw.sitwithus.view.ProfileListAdapter;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends Activity {

    private ProfileListAdapter adapter;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View empty = findViewById(R.id.friendsEmpty);
        ListView list = (ListView) findViewById(R.id.friend_list);
        list.setEmptyView(empty);
    }

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_friend_list);

        final ListView listView = (ListView) findViewById(R.id.friend_list);

        adapter = new ProfileListAdapter(this,
                Preferences.getUserKey(this));
        listView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //
        ServerRequest getFriends = ServerRequest.createGetFriends(
                Preferences.getUserKey(this));

        getFriends.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);
                adapter.retrieveProfiles(responseMessage.getStringArray(Keys.USERNAME));
            }
        });
    }
}