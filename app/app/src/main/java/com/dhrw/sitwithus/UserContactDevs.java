package com.dhrw.sitwithus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.view.LayoutInflater;
import android.view.View;

import com.dhrw.sitwithus.util.Preferences;


public class UserContactDevs extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_devs);


        //TODO: Make this user feedback actually go somewhere
        Button submit = (Button) findViewById(R.id.submitDevs);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                thankUser();
            }
        });

    }

    //need a popup upon submission to show that your feedback was successful
    //this popup does not work.

    public void thankUser(){
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)   //can't figure out how to make this text black
                .setMessage("Thank you for your feedback and helping to improve our application!")
                .setCancelable(false)
                .setPositiveButton("You're welcome", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                })
                .show();


    }
/*
    public void onButtonShowPopup(View view) {
        LinearLayout mainlayout = (LinearLayout) findViewById(R.layout.activity_contact_devs);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.fragment_subconfirm, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(mainlayout, Gravity.CENTER, 0, 0);
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }
*/
}
