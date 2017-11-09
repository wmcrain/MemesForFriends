package com.dhrw.sitwithus.server;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dhrw.sitwithus.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/** */
public class ServerRequest {

    // The website to which the requests will be made
    private static final String SERVER_WEB_ADDRESS = "http://sit-with-us-backend.appspot.com";

    // The byte representation of characters sent to and received from the server
    static final String CHARSET = "UTF-8";

    // Text added to strings before the strings are hashed to increase the difficulty of
    private static final String SALT = "5H3$2Mop0Z7q+^490aa&%&1";

    // THe list of directories for the api calls
    private static final String DIR_CREATE_USER = "create";
    private static final String DIR_LOGIN_USER = "login";
    private static final String DIR_LOGIN_PING = "login/ping";
    private static final String DIR_PROFILE_GET = "profile/get";
    private static final String DIR_PROFILE_SET = "profile/set";
    
    /** Holds the methods that will be called when the response has arrived from the server. */
    public static abstract class Callback {

        /**
         * The callback invoked when the request times out, when an error occurs while attempting
         * to retrieve the {@link ServerResponse}, or if the HTTP response code of the
         * {@link ServerResponse} represents that the request could not be handled correctly.
         */
        public void onError(int responseCode, String responseMessage) {
            Log.e("SitWithUs", responseMessage);
        }

        /** The callback invoked when the server handles the request correctly with no errors. */
        public void onSuccess(int responseCode, ServerResponse responseMessage) {
            Log.d("SitWithUs", responseMessage.toString());
        }
    }

    // The data to represented as JSON text to be sent to the server
    private final String requestMessage;

    // The directory of the website the request should be sent to
    private final String directory;

    /** Create a new {@link ServerRequest} which holds the request data to be send to the server. */
    private ServerRequest(String directory, JSONObject requestData) {
        this.directory = directory;
        this.requestMessage = requestData.toString();
    }

    /** */
    public void sendRequest(final Callback callback) {
        sendRequest(callback, 5000);
    }

    /** */
    public void sendRequest(final Callback callback, final int timeOut) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Create the connection to the server url
                    HttpURLConnection con = (HttpURLConnection) new URL(SERVER_WEB_ADDRESS
                            + '/' + directory).openConnection();
                    con.setConnectTimeout(timeOut);
                    con.setReadTimeout(timeOut);

                    // Configures the connection to use POST rather than GET
                    con.setDoOutput(true);
                    con.setRequestMethod("POST");
                    con.setRequestProperty("User-Agent", CHARSET);
                    con.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded;charset=" + CHARSET);

                    // Send the request data to the server
                    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                    wr.writeBytes(requestMessage);
                    wr.flush();
                    wr.close();

                    // Retrieve the HTTP response code. A successful response will have a response
                    // code in the 200s
                    int responseCode = con.getResponseCode();
                    boolean successful = responseCode >= 200 && responseCode < 300;

                    // Read the response from the server
                    BufferedReader in;
                    if (successful) {
                        in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    } else {
                        in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    }

                    // Read the body of the response
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // Call the correct callback depending if the response code represents a
                    // successful response or not.
                    if (successful) {
                        callback.onSuccess(responseCode, new ServerResponse(response.toString()));
                    } else {
                        callback.onError(responseCode, response.toString());
                    }

                } catch (IOException e) {
                    callback.onError(-1, e.toString());
                }

                return null;
            }
        };

        asyncTask.execute();

    }

    /** Salt and hash the specified text using the SHA-256 hashing algorithm. */
    private static String hash(String text) {
        try {
            // Hash the string and salt using SHA-256 and returned the hashed string
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((text + SALT).getBytes(CHARSET));
            return new String(hash);

        } catch (NoSuchAlgorithmException e) {
            // This should only happen if the specified hashing algorithm does not exists
            throw new IllegalStateException("Hashing algorithm does not exist.");

        } catch (UnsupportedEncodingException e) {
            // This should only happen if the specified text format does not exists
            throw new IllegalStateException("Unknown text format");
        }
    }

    /**
     * Creates a request for an account with the specified credentials to be created.
     *
     * If account creation is successful, the form of the resulting JSON will be:
     *      { {@link #} : 1, {@link Keys#DEVICE_CODE} : [device_code] }
     *
     * If account creation is not successful, the form of the resulting JSON will be:
     *      { {@link Keys#SUCCESS} : 0, {@link Keys#ERROR_MESSAGE} : [error_text] }
     *
     * @param firstName The first name of the user
     * @param lastName The last name of the user
     * @param username The username of the account
     * @param email The email used to authenticate the account
     * @param phoneNumber The phone number tied to the account (with or without symbols)
     */
    public static ServerRequest createUserCreateRequest(String firstName, String lastName,
            String username, String email, String phoneNumber) {
        try {
            JSONObject data = new JSONObject();
            data.put(Keys.FIRST_NAME, firstName);
            data.put(Keys.LAST_NAME, lastName);
            data.put(Keys.USERNAME, username);
            data.put(Keys.EMAIL, email);
            data.put(Keys.PHONE_NUMBER, phoneNumber);
            return new ServerRequest(DIR_CREATE_USER, data);

        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to create login request.");
        }
    }

    /**
     * Creates a request that can be sent to the server that attempts to log a user in with the
     * specified username and password.
     *
     * Regardless of if a user with the email exists or not, the form of the resulting JSON will be:
     *      { {@link Keys#SUCCESS} : 1, {@link Keys#DEVICE_CODE} : [device_code] }
     *
     * @param email The username of the account of the user attempting to log in
     */
    public static ServerRequest createLoginRequest(String email) {
        try {
            JSONObject data = new JSONObject();
            data.put(Keys.EMAIL, email);
            return new ServerRequest(DIR_LOGIN_USER, data);

        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to create login request.");
        }
    }

    /**
     * Creates a request that checks if the login verification link has been clicked on so that the
     * user can log into their account.
     *
     * If the link was clicked, the form of the resulting JSON will be:
     *      { {@link Keys#SUCCESS} : 1, {@link Keys#USER_KEY} : [user_key] }
     *
     * If the link was not clicked and the user is not able to login in yet, the form of the
     * resulting JSON will be:
     *      { {@link Keys#SUCCESS} : 0 }
     *
     * @param email The email address of the account to be logged into
     * @param deviceCode The device code retrieved when the login request was submitted
     **/
    public static ServerRequest createLoginPingRequest(String email, String deviceCode) {
        try {
            JSONObject data = new JSONObject();
            data.put(Keys.EMAIL, email);
            data.put(Keys.DEVICE_CODE, deviceCode);
            return new ServerRequest(DIR_LOGIN_PING, data);

        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to create login ping request.");
        }
    }

    public static ServerRequest createUpdateProfileRequest(String userKey, @Nullable String bio,
            @Nullable Bitmap picture) {
        try {
            JSONObject data = new JSONObject();
            data.put(Keys.USER_KEY, userKey);

            // Add the new user bio if it was provided
            if (bio != null) {
                data.put(Keys.BIO, bio);
            }

            // Add the new profile picture if it was provided
            if (picture != null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                picture.compress(Bitmap.CompressFormat.PNG, 100, stream);

                // Put the UTF-8 string representation of the bytes of the file in the request data
                String pictureBytes = new String(stream.toByteArray(), CHARSET);
                data.put(Keys.PICTURE, pictureBytes);
            }

            return new ServerRequest(DIR_PROFILE_SET, data);

        } catch (JSONException|UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to create login ping request.");
        }
    }

    public static ServerRequest createGetProfileRequest(String userKey, String username) {
        ArrayList<String> usernames = new ArrayList<>();
        usernames.add(username);
        return createGetProfileRequest(userKey, usernames);
    }

    public static ServerRequest createGetProfileRequest(String userKey, List<String> usernames) {
        try {
            JSONObject data = new JSONObject();
            data.put(Keys.USER_KEY, userKey);

            JSONArray targetArray = new JSONArray();
            for (int i = 0; i < usernames.size(); i++) {
                targetArray.put(i, usernames.get(i));
            }
            data.put(Keys.USERNAME, targetArray);

            return new ServerRequest(DIR_PROFILE_GET, data);

        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to create get profile request.");
        }
    }
}