package com.dhrw.sitwithus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;

import org.json.JSONException;
import org.json.JSONObject;

public class UserCreateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Send a create user request to the server and go back to the login activity
        Button button = (Button) findViewById(R.id.btn_sign_up);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve the information of the user account to be created
                String firstName = ((TextView) findViewById(R.id.txt_first_name))
                        .getText().toString();
                String lastName = ((TextView) findViewById(R.id.txt_last_name))
                        .getText().toString();
                String username = ((TextView) findViewById(R.id.txt_username))
                        .getText().toString();
                final String email = ((TextView) findViewById(R.id.txt_email_address))
                        .getText().toString();
                String phone = ((TextView) findViewById(R.id.txt_phone_number))
                        .getText().toString();

                // Send a create user request to the server
                ServerRequest request = ServerRequest.createUserCreateRequest(firstName, lastName,
                        username, email, phone);
                request.sendRequest(new ServerRequest.Callback() {
                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        Log.d("SitWithUs", responseMessage.toString());

                        if (responseMessage.getInt(Keys.SUCCESS) == 1) {

                            // Send the email address and the device code retrieved from creating the
                            // account back to the login activity
                            Intent data = new Intent();
                            data.putExtra(Keys.EMAIL, email);
                            data.putExtra(Keys.DEVICE_CODE, responseMessage.getString(Keys.DEVICE_CODE));

                            setResult(RESULT_OK, data);
                            UserCreateActivity.this.finish();

                        } else {

                            /*Toast.makeText(UserCreateActivity.this,
                                    responseMessage.getString(Keys.ERROR_MESSAGE),
                                    Toast.LENGTH_LONG).show();*/
                        }
                    }
                });
            }
        });
    }

}
