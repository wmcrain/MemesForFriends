package com.dhrw.sitwithus.data;

import android.graphics.Bitmap;

public class UserProfile {

    public String username;
    public String firstName;
    public String lastName;
    public String bio;

    public Bitmap picture;

    public UserProfile(String username, String firstName, String lastName, String bio,
            Bitmap picture) {

        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.bio = bio;
        this.picture = picture;
    }
}
