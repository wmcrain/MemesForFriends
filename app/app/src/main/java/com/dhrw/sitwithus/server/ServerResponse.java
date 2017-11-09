package com.dhrw.sitwithus.server;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

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

    public Bitmap getImage(String name) {
        try {
            byte[] pictureBytes = response.getString(name).getBytes(ServerRequest.CHARSET);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            return BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length, options);
        } catch (JSONException|UnsupportedEncodingException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @Override
    public String toString() {
        return response.toString();
    }
}
