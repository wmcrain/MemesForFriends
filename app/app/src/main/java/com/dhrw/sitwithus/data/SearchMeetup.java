package com.dhrw.sitwithus.data;

import java.util.List;

/** */
public class SearchMeetup {

    // The unique identifier of the searching meetup
    public final String entityKey;

    // the list of users in the meetup
    public  final List<String> usernames;

    // The distance in kilometers from the current user to this meetup
    public final double distance;

    public SearchMeetup(String entityKey, List<String> usernames, double distance) {
        this.entityKey = entityKey;
        this.usernames = usernames;
        this.distance = distance;
    }
}
