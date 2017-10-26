package com.dhrw.sitwithus.server;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** */
public class ServerRequest {

    // The website to which the requests will be made
    private static final String SERVER_WEB_ADDRESS = "http://sit-with-us-backend.appspot.com";

    // The byte representation of characters sent to and received from the server
    private static final String CHARSET = "UTF-8";

    // Text added to strings before the strings are hashed to increase the difficulty of
    private static final String SALT = "5H3$2Mop0Z7q+^490aa&%&1";

    // THe list of directories for the api calls
    private static final String DIR_CREATE_USER = "create";
    private static final String DIR_LOGIN_USER = "login";

    // The list of request JSON keys
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_USERNAME = "username";

    /** Holds the methods that will be called when the response has arrived from the server. */
    public interface Callback {

        /**
         * The callback invoked when the request times out, when an error occurs while attempting
         * to retrieve the {@link ServerResponse}, or if the HTTP response code of the
         * {@link ServerResponse} represents that the request could not be handled correctly.
         * @param response the response from the server containing the error code and the
         *                 response message
         */
        void onError(ServerResponse response);

        /** The callback invoked when the server handles the request correctly with no errors. */
        void onSuccess(ServerResponse response);
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
    public void sendRequest(final Callback callback, final int timeOut) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    // Create the connection to the server url
                    HttpURLConnection con = (HttpURLConnection) new URL(SERVER_WEB_ADDRESS)
                            .openConnection();
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
                    JSONObject responseData = new JSONObject(response.toString());
                    if (successful && responseData.getInt(KEY_SUCCESS) == 1) {
                        callback.onSuccess(new ServerResponse(responseCode, response.toString()));
                    } else {
                        callback.onError(new ServerResponse(responseCode, response.toString()));
                    }

                } catch (IOException e) {
                    callback.onError(new ServerResponse(ServerResponse.RESPONSE_IO_EXCEPTION,
                            e.toString()));

                } catch (JSONException e) {
                    callback.onError(new ServerResponse(ServerResponse.RESPONSE_JSON_EXCEPTION,
                            e.toString()));
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
     * Create a request that can be sent to the server that attempts to log a user in with the
     * specified username and password.
     * @param userName The username of the account of the user attempting to log in
     * @param password The password of the account of the user attempting to log in
     */
    public static ServerRequest createLoginRequest(String userName, String password) {
        try {
            JSONObject loginData = new JSONObject();
            loginData.put(KEY_USERNAME, userName);
            loginData.put(KEY_PASSWORD, hash(password));
            return new ServerRequest(DIR_LOGIN_USER, loginData);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Unable to create login request.");
        }
    }
}
