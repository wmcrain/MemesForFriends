package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

public class UserLoginActivity extends Activity {

    /** */
    private class LoginPing extends Thread {

        // The ping request to continually send to the server
        private final ServerRequest request;

        // Whether to stop attempting to login or not
        private boolean stop;

        // The key of the user retrieved from login succeeds
        private String userKey;

        //
        private final ServerRequest.Callback requestCallback = new ServerRequest.Callback() {

            @Override
            public void onError(int responseCode, String responseMessage) {
                super.onError(responseCode, responseMessage);

                // End the thread if something goes wrong with communicating with the server
                stop = true;
            }

            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                Log.d("SitWithUs", "Receive " + responseMessage.toString());
                if (responseMessage.getInt(Keys.SUCCESS) == 1) {
                    stop = true;
                    userKey = responseMessage.getString(Keys.USER_KEY);

                    //
                    /*getSharedPreferences("u", MODE_PRIVATE).edit()
                            .putString(ServerRequest.KEY_USER_KEY, userKey);*/

                    startActivity(new Intent(UserLoginActivity.this, MainActivity.class));
                }
            }
        };

        public LoginPing(String email, String deviceCode) {
            request = ServerRequest.createLoginPingRequest(email, deviceCode);
            stop = false;
        }

        @Override
        public void run() {
            try {
                while (!stop) {

                    //
                    request.sendRequest(requestCallback, 5000);

                    Log.d("SitWithUs", "Send Ping");

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e("SitWithUs", e.toString());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            // Place the email address the user typed in the account creation box into the email box
            if (data.hasExtra(Keys.EMAIL)) {
                String email = data.getStringExtra(Keys.EMAIL);
                String deviceCode = data.getStringExtra(Keys.DEVICE_CODE);

                //
                TextView emailTextView = (TextView) findViewById(R.id.txt_login_email_address);
                emailTextView.setText(email);

                // Start attempting to log in using the device code from creating the account
                (new LoginPing(email, deviceCode)).start();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Go to the sign up screen when the sign up button is pressed
        Button signUpButton = (Button) findViewById(R.id.btn_goto_sign_up);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(UserLoginActivity.this, UserCreateActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        //
        Button logInButton = (Button) findViewById(R.id.btn_log_in);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve the email of the user to log into
                final String email = ((TextView) findViewById(R.id.txt_login_email_address))
                        .getText().toString().trim();

                //
                ServerRequest request = ServerRequest.createLoginRequest(email);
                request.sendRequest(new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        String device_key = responseMessage.getString(Keys.DEVICE_CODE);

                        // Start checking for confirmation that the email was verified
                        (new LoginPing(email, device_key)).start();
                    }
                });

                // Start pinging for log in

                //
                //Intent myIntent = new Intent(UserLoginActivity.this, MainActivity.class);
                //startActivity(myIntent);
            }
        });

    }
}
