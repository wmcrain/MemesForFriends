package com.dhrw.sitwithus.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.dhrw.sitwithus.data.SearchMeetup;
import com.dhrw.sitwithus.data.UserProfile;
import com.dhrw.sitwithus.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerResponse {

    private JSONObject response;

    /** */
    public ServerResponse(String response) {
        try {
            this.response = new JSONObject(response);
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private ServerResponse(JSONObject response) {
        this.response = response;
    }

    /** */
    public boolean has(String name) {
        return response.has(name);
    }

    /** */
    public int getInt(String name) {
        try {
            return response.getInt(name);
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /** */
    public String getString(String name) {
        try {
            return response.getString(name);
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    private Bitmap stringToImage(String image) {
        byte[] pictureBytes = Base64.decode(image, Base64.URL_SAFE);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        return BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length, options);
    }

    public List<UserProfile> getProfileArray(String name) {
        ArrayList<UserProfile> result = new ArrayList<>();
        try {
            JSONArray a = response.getJSONArray(name);
            for (int i = 0; i < a.length(); i++) {
                JSONObject profileObject = a.getJSONObject(i);

                Bitmap image = profileObject.has(Keys.PICTURE) ?
                        stringToImage(profileObject.getString(Keys.PICTURE)) : null;

                UserProfile profile = new UserProfile(profileObject.getString(Keys.USERNAME),
                        profileObject.getString(Keys.FIRST_NAME),
                        profileObject.getString(Keys.LAST_NAME),
                        profileObject.getString(Keys.BIO),
                        image);

                result.add(profile);
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public List<SearchMeetup> getSearchMeetupArray(String name) {
        ArrayList<SearchMeetup> result = new ArrayList<>();
        try {
            JSONArray a = response.getJSONArray(name);
            for (int i = 0; i < a.length(); i++) {
                JSONObject meetupObject = a.getJSONObject(i);

                // Retrieve the usernames of the users in the meetup
                ArrayList<String> usernames = new ArrayList<>();
                JSONArray jsonUsernames = meetupObject.getJSONArray(Keys.USERNAME);
                for (int j = 0; j < jsonUsernames.length(); j++) {
                    usernames.add(jsonUsernames.getString(j));
                }

                //
                SearchMeetup meetup = new SearchMeetup(
                        meetupObject.getString(Keys.SEARCH_KEY), usernames,
                        meetupObject.getDouble(Keys.DISTANCE));

                result.add(meetup);
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public List<String> getStringArray(String name) {
        ArrayList<String> result = new ArrayList<>();
        try {
            JSONArray a = response.getJSONArray(name);
            for (int i = 0; i < a.length(); i++) {
                result.add(a.getString(i));
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
