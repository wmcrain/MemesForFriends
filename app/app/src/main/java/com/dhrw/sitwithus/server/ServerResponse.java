package com.dhrw.sitwithus.server;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerResponse extends JSONObject {

    private JSONObject response;

    /** */
    public ServerResponse(String response) {
        try {
            this.response = new JSONObject(response);
        } catch (JSONException e) {
            throw new RuntimeException(e.toString());
        }
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

    @Override
    public String toString() {
        return response.toString();
    }
}
