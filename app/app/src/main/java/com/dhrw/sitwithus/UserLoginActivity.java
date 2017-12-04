package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;
import com.dhrw.sitwithus.util.Preferences;

import org.json.JSONException;
import org.json.JSONObject;

public class UserLoginActivity extends Activity {
    LoginPing lp;

    public static class LoginPopup extends DialogFragment {
        //needs code somewhere further down to create this at the proper time and pass in correct address and code

        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            final View rootView=inflater.inflate(R.layout.fragment_login_popup, container, false);
            final TextView instructions = (TextView) rootView.findViewById(R.id.loginPopupText);
            final TextView deviceNumber = (TextView) rootView.findViewById(R.id.loginDeviceCode);
            final TextView bottomInstructions = (TextView) rootView.findViewById(R.id.loginPopupBottom);
            final String address = getArguments().getString("address");
            final String code = getArguments().getString("code");
            final boolean registered = getArguments().getBoolean("registered");
            final String display;
            final String bottom;

            if(registered){
                display = "An email has been sent to " + address + " with device code: ";
                bottom = "To login, click the link provided.";
            }
            else{
                display = "An email has been sent to " + address + " with device code: ";
                bottom = "To complete registration, click the link provided.";
            }

            instructions.setText(display);
            deviceNumber.setText(code);
            bottomInstructions.setText(bottom);


            Button cancel = (Button) rootView.findViewById(R.id.loginPopupCancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((UserLoginActivity)getActivity()).cancelRequest();
                    dismiss();
                }
            });
            return rootView;
        }

    }

    /** */
    private class LoginPing extends Thread {

        // The ping request to continually send to the server
        private final ServerRequest request;

        // Whether to stop attempting to login or not
        public boolean stop;


        //
        private final ServerRequest.Callback requestCallback = new ServerRequest.Callback() {

            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                Log.d("SitWithUs", "Receive " + responseMessage.toString());
                if (responseMessage.getInt(Keys.SUCCESS) == 1) {
                    stop = true;

                    //
                    Preferences.setUserKey(UserLoginActivity.this,
                            responseMessage.getString(Keys.USER_KEY));

                    Preferences.setUsername(UserLoginActivity.this,
                            responseMessage.getString(Keys.USERNAME));

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
        final FragmentManager fm = getFragmentManager();
        final LoginPopup popup = new LoginPopup();
        final Bundle args = new Bundle();
        if (resultCode == RESULT_OK) {

            // Place the email address the user typed in the account creation box into the email box
            if (data.hasExtra(Keys.EMAIL)) {
                String email = data.getStringExtra(Keys.EMAIL);
                String deviceCode = data.getStringExtra(Keys.DEVICE_CODE);

                //
                TextView emailTextView = (TextView) findViewById(R.id.txt_login_email_address);
                emailTextView.setText(email);

                // Start attempting to log in using the device code from creating the account
                lp = new LoginPing(email, deviceCode);
                lp.start();

                args.putString("address",email);
                args.putString("code",deviceCode);
                args.putBoolean("registered",false);
                popup.setArguments(args);
                popup.show(fm, "Email Sent");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final FragmentManager fm = getFragmentManager();
        final LoginPopup popup = new LoginPopup();
        final Bundle args = new Bundle();

        //
        if (Preferences.getUserKey(UserLoginActivity.this) != null) {
            startActivity(new Intent(UserLoginActivity.this, MainActivity.class));
        }

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

                // Start pinging for log in
                ServerRequest request = ServerRequest.createLoginRequest(email);
                request.sendRequest(new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        String device_key = responseMessage.getString(Keys.DEVICE_CODE);

                        // Start checking for confirmation that the email was verified
                        lp = new LoginPing(email, device_key);
                        lp.start();
                        args.putString("address",email);
                        args.putString("code",device_key);
                        args.putBoolean("registered",true);
                        popup.setArguments(args);
                        popup.show(fm, "Email Sent");

                    }
                });
            }
        });


    }
    public void cancelRequest(){
        lp.stop = true;
    }
}
