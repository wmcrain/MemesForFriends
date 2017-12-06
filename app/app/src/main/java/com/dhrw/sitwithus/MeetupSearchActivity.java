package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.app.Notification;
import android.app.NotificationManager;

import com.dhrw.sitwithus.server.EncodedBitmap;
import com.dhrw.sitwithus.server.UserProfileData;
import com.dhrw.sitwithus.server.SearchMeetupData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.sync.MeetupSearcher;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MeetupSearchActivity extends Activity {

    private LinkedHashMap<String, UserProfileData> usernameProfiles;
    private LinkedHashMap<String, Boolean> willingMeetups;


    // The list
    private List<SearchMeetupData> searchMeetups;

    private MeetupSearcher searcher;

    private class UserSearchAdapter extends ArrayAdapter {

        public UserSearchAdapter() {
            super(MeetupSearchActivity.this, R.layout.view_match_entry);
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final SearchMeetupData searchMeetup = searchMeetups.get(position);
            final UserProfileData profile = usernameProfiles.get(searchMeetup.usernames.get(0));

            View view = convertView == null ? LayoutInflater.from(MeetupSearchActivity.this)
                    .inflate(R.layout.view_match_entry, null) : convertView;

            TextView name = (TextView) view.findViewById(R.id.profileMatchEntry);
            name.setText(profile.firstName + " " + profile.lastName);

            ImageView pic = (ImageView) view.findViewById(R.id.match_entry_picture);
            if (profile.picture != null) {
                pic.setImageBitmap(getRoundedCornerBitmap(profile.picture, (int) (profile.picture.getWidth() * .7)));
            } else {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.david);
                pic.setImageBitmap(getRoundedCornerBitmap(bm, (int) (bm.getWidth() * .7)));
            }

            TextView GPS = (TextView) view.findViewById(R.id.distanceMatchEntry);
            String q = String.valueOf(searchMeetup.distance) + " km";
            GPS.setText(q);

            name.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent viewProfile = new Intent(MeetupSearchActivity.this,
                            ViewProfileActivity.class);
                    viewProfile.putExtra(Keys.USERNAME, profile.username);
                    startActivity(viewProfile);
                }
            });
            //need to check state of a match to set toggleMatched to the proper state
            //based on if a user has previously matched with them during this session
            //check match status when this switch is toggled

            //when both people toggle the switch on the "matched" ListView, they will be immediately
            //pushed into a group

            // Have the toggle communicate with the searching thread to communicate which meetups
            // the user is willing to meet with
            Switch toggleMatched = (Switch) view.findViewById(R.id.toggleMatchEntry);
            toggleMatched.setChecked(willingMeetups.get(searchMeetup.entityKey));

            toggleMatched.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    searcher.setWilling(searchMeetup, isChecked);
                    willingMeetups.put(searchMeetup.entityKey, isChecked);
                }
            });

            return view;
        }

        @Override
        public int getCount() {
            return searchMeetups.size();
        }
    }

    /** */
    public static class MatchConfirmPopup extends DialogFragment {

        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_search_confirm,
                    container, false);
            Bundle args = getArguments();

            //
            final String searchKey = args.getString(Keys.SEARCH_KEY);
            final String firstName = args.getString(Keys.FIRST_NAME);
            final String lastName = args.getString(Keys.LAST_NAME);
            final String picture = args.containsKey(Keys.PICTURE) ?
                    args.getString(Keys.PICTURE) : null;

            //
            TextView meetupName = (TextView) view.findViewById(R.id.confirm_match_text);
            meetupName.setText(firstName + " " + lastName + " wants to match with you!");

            // The the image of the person this user matched with
            ImageView meetupImage = (ImageView) view.findViewById(R.id.confirm_match_image);
            if (picture != null) {
                meetupImage.setImageBitmap(EncodedBitmap.toBitmap(picture));
            } else {
                meetupImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.david));
            }

            // Send the server that the user wants to match with the other user if this button
            // is pressed
            Button yesButton = (Button) view.findViewById(R.id.confirm_match_yes);
            yesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerRequest.createSearchConfirmMatchRequest(searchKey, true)
                            .sendRequest();
                }
            });

            // Send the server that the user does not want to match with the other user if this
            // button is pressed
            Button noButton = (Button) view.findViewById(R.id.confirm_match_no);
            noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ServerRequest.createSearchConfirmMatchRequest(searchKey, false)
                            .sendRequest();
                    dismiss();
                }
            });

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_search);

        usernameProfiles = new LinkedHashMap<>();
        willingMeetups = new LinkedHashMap<>();
        searchMeetups = new ArrayList<>();

        final ListView listView = (ListView) findViewById(R.id.usersSearch);
        final UserSearchAdapter adapter = new UserSearchAdapter();

        // Create the match confirmation popup
        final FragmentManager fm = getFragmentManager();
        final MatchConfirmPopup confirmPopup = new MatchConfirmPopup();

        // Create the meetup searcher thread that pulls search results periodically from the server
        searcher = new MeetupSearcher(Preferences.getUserKey(this)) {

            /** */
            @Override
            public void onResultUpdate(List<SearchMeetupData> nearbyMeetups) {
                searchMeetups = nearbyMeetups;
                for (SearchMeetupData meetupData : nearbyMeetups) {
                    if (!willingMeetups.containsKey(meetupData.entityKey)) {
                        willingMeetups.put(meetupData.entityKey, false);
                    }
                }

                // Retrieve a list of all the usernames for which the profile has not been cached
                ArrayList<String> newUsernames = new ArrayList<>();
                for (SearchMeetupData searchMeetup : nearbyMeetups) {
                    for (String username : searchMeetup.usernames) {
                        if (!usernameProfiles.containsKey(username)) {
                            newUsernames.add(username);
                        }
                    }
                }

                if (newUsernames.size() == 0) {
                    // Update the list view now if all the profiles are already cached
                    adapter.notifyDataSetChanged();

                } else {
                    // Retrieve the profiles of the users whose profiles have not been cached
                    ServerRequest getProfiles = ServerRequest.createGetProfileRequest(
                            Preferences.getUserKey(MeetupSearchActivity.this),
                            newUsernames);

                    getProfiles.sendRequest(new ServerRequest.Callback() {

                        /** */
                        @Override
                        public void onSuccess(int responseCode, ServerResponse responseMessage) {
                            super.onSuccess(responseCode, responseMessage);

                            // Add the profiles to the cache
                            for (UserProfileData profile : responseMessage.getProfileArray(Keys.PROFILE)) {
                                usernameProfiles.put(profile.username, profile);
                            }

                            // Update the list view
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }

            /** */
            @Override
            public void onConfirmedMatch() {
                finish();
            }

            /** */
            @Override
            public void onDeclinePendingMatch() {
                // TODO : Display a popup saying the user could not match at the time
            }

            /**
             * Display a match confirmation popup when the user has approved someone who has also
             * approved of them.
             **/
            @Override
            public void onPendingMatch(String otherSearchKey) {
                Bundle args = new Bundle();

                for (SearchMeetupData meetup : searchMeetups) {
                    if (meetup.entityKey.equals(otherSearchKey)) {
                        UserProfileData user = usernameProfiles.get(meetup.usernames.get(0));

                        // Set the popup to display the information of the first user
                        args.putString(Keys.SEARCH_KEY, searcher.getSearchKey());
                        args.putString(Keys.FIRST_NAME, user.firstName);
                        args.putString(Keys.LAST_NAME, user.lastName);
                        if (user.picture != null) {
                            args.putString(Keys.PICTURE, EncodedBitmap.toString(user.picture));
                        }
                        //TODO: Figure out what icon to use here.
                        //TODO: Only push this if the application is in background-unsure if possible at this API level
                        NotificationManager notif = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                        //https://developer.android.com/training/notify-user/build-notification.html
                        Notification notify=new Notification.Builder
                                (getApplicationContext()).setContentTitle("Sit With Us").setContentText("You have a pending match!").
                                setContentTitle("Sit With Us").setSmallIcon(R.mipmap.david).build();
                        notify.flags |= Notification.FLAG_AUTO_CANCEL;
                        notif.notify(0, notify);
                        // Show the popup
                        confirmPopup.setArguments(args);
                        confirmPopup.show(fm, "Confirm Match");
                        break;
                    }
                }
            }
        };

        searcher.start();
        listView.setAdapter(adapter);

        //initialize the proper buttons to the proper colors
        //depending on which screen you're on
        //Green for on that screen, gray for not on the screen?
        //Initialize unmatched to green
        //Initialize matched to gray

        //Potentially need a Boolean flag if using one screen and repopulating the ListView
        //When a button is clicked, check the state of the flag, set colors as necessary
        //keep track of two lists for the list adapter
        //flip flags on button presses to signify which list we're looking at
        //easier than repopulating a single list repeatedly
        Button stopSearch = (Button) findViewById(R.id.stopSearch);
        stopSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searcher.stop();
                finish();
                //stop the searching thread and take user out of screen when
                //the thread has been stopped
            }
        });
        Button matched = (Button) findViewById(R.id.matchedSearch);
        matched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MeetupSearchActivity.this, MeetupSearchPendingActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        Button unmatched = (Button) findViewById(R.id.unmatchedSearch);
        unmatched.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        //code for swiping needs to be added, might replace swiping with toggle
        //code for swapping between already toggled users and to be toggled users

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        searcher.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MeetupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
