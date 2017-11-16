package com.dhrw.sitwithus.data;

import android.graphics.Bitmap;

import com.dhrw.sitwithus.R;

public class MeetupStub {

    public String username;
    public String firstName;
    public String lastName;
    public String bio;

    public Bitmap picture;

    public double GPS;
    //public string searchkey;

    public MeetupStub(String username, String firstName, String lastName, String bio, double GPS) {

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.GPS = GPS;
        this.picture = null;
    }
}
