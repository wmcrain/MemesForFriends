package com.dhrw.sitwithus.data;

import java.util.List;

public class SearchMeetup {

    public  final List<String> usernames;

    public final double distance;

    public final boolean willingToMatch;

    public final String entityKey;

    public SearchMeetup(String entityKey, List<String> usernames, double distance, boolean willingToMatch) {
        this.entityKey = entityKey;
        this.usernames = usernames;
        this.distance = distance;
        this.willingToMatch = willingToMatch;
    }
}
