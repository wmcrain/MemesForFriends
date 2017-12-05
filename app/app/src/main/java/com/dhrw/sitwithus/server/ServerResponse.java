package com.dhrw.sitwithus.server;

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

    public List<UserProfileData> getProfileArray(String name) {
        ArrayList<UserProfileData> result = new ArrayList<>();
        try {
            JSONArray a = response.getJSONArray(name);
            for (int i = 0; i < a.length(); i++) {
                result.add(new UserProfileData(a.getJSONObject(i)));
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public List<SearchMeetupData> getSearchMeetupArray(String name) {
        ArrayList<SearchMeetupData> result = new ArrayList<>();
        try {
            JSONArray a = response.getJSONArray(name);
            for (int i = 0; i < a.length(); i++) {
                result.add(new SearchMeetupData(a.getJSONObject(i)));
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public List<BlockedUserData> getBlockedUserList(String name) {
        ArrayList<BlockedUserData> result = new ArrayList<>();
        try {
            JSONArray a = response.getJSONArray(name);
            for (int i = 0; i < a.length(); i++) {
                result.add(new BlockedUserData(a.getJSONObject(i)));
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
