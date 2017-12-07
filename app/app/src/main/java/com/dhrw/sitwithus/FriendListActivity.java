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

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends Activity {

    List<UserProfileData> friends;

    String[] testArray = {"Hazem", "Alex", "Ryan", "David","Will","Abdul","Casey","Sara","Josh"};
    private class FriendArrayAdapter extends ArrayAdapter {

        public FriendArrayAdapter() {
            super(FriendListActivity.this, R.layout.activity_friend_entry);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final UserProfileData profile = friends.get(position);

            View view = LayoutInflater.from(FriendListActivity.this).inflate(R.layout.activity_friend_entry, null);
            TextView name = (TextView) view.findViewById(R.id.friend_entry_name);
            name.setText(profile.firstName + " " + profile.lastName);

            ImageView pic = (ImageView) view.findViewById(R.id.friend_entry_picture);
            if (profile.picture != null) {
                pic.setImageBitmap(getRoundedCornerBitmap(profile.picture, (int) (profile.picture.getWidth() * .7)));
            } else {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.david);
                pic.setImageBitmap(EncodedBitmap.getRoundBitmap(bm, (int) (bm.getWidth() * .7)));
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent viewProfile = new Intent(FriendListActivity.this,
                            ViewFriendProfileActivity.class);
                    viewProfile.putExtra(Keys.USERNAME, profile.username);
                    startActivity(viewProfile);
                }
            });

            //View view = new View()
            return view;
        }

        @Override
        public int getCount() {
            return friends.size();
        }
    }

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

        friends = new ArrayList<>();

        final ListView listView = (ListView) findViewById(R.id.friend_list);

        //
        ServerRequest getFriends = ServerRequest.createGetFriends(
                Preferences.getUserKey(this));

        final FriendArrayAdapter adapter = new FriendArrayAdapter();
        listView.setAdapter(adapter);

        //
        getFriends.sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

                ServerRequest getProfiles = ServerRequest.createGetProfileRequest(
                        Preferences.getUserKey(FriendListActivity.this),
                        responseMessage.getStringArray(Keys.USERNAME));

                //
                getProfiles.sendRequest(new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);

                        friends = responseMessage.getProfileArray(Keys.PROFILE);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
