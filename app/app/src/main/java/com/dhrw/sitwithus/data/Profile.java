package com.dhrw.sitwithus.data;

import android.graphics.Bitmap;

public class Profile {

    public String username;
    public String firstName;
    public String lastName;
    public String bio;

    public Bitmap picture;

    public Profile(String username, String firstName, String lastName, String bio,
            Bitmap picture) {

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.picture = picture;
    }
}
