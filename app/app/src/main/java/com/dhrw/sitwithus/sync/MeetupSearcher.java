package com.dhrw.sitwithus.sync;

import com.dhrw.sitwithus.server.SearchMeetupData;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;

import java.util.ArrayList;
import java.util.List;

/** */
public abstract class MeetupSearcher extends RequestLooper {

    // The amount of time in milliseconds between searching updates
    private static final long UPDATE_INTERVAL = 10 * 1000;

    // The lock used to synchronize the changes to willing matches
    private final Object lock = new Object();

    // The list of search entity keys the user is willing to join
    private List<String> willingMatches;

    // The key of the currently logged in user
    private final String userKey;

    // The key of the searching entity the user is a part of
    private String searchKey;

    private String pendingMatchKey;

    /** */
    public MeetupSearcher(String userKey) {
        super(UPDATE_INTERVAL);
        this.userKey = userKey;
        this.willingMatches = new ArrayList<>();
        this.pendingMatchKey = null;
    }

    /** */
    public void setWilling(SearchMeetupData meetup, boolean willing) {
        synchronized (lock) {
            if (willing) {
                willingMatches.add(meetup.entityKey);
            } else {
                willingMatches.remove(meetup.entityKey);
            }
        }
    }

    public String getSearchKey() {
        return searchKey;
    }

    /** */
    @Override
    protected final void onStart() {
        // Add the user to a meetup
        ServerRequest.createStartSearchRequest(userKey, 0.0f, 0.0f)
                .sendRequest(new ServerRequest.Callback() {

                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);

                        //
                        searchKey = responseMessage.getString(Keys.SEARCH_KEY);

                        //
                        threadStart();
                    }
                });
    }

    /** */
    @Override
    protected final void onLoop() {
        synchronized (lock) {

            // Send the request to receive updated search data and call the
            // onResultUpdate method when the request succeeds
            ServerRequest updateRequest = ServerRequest.createUpdateSearchRequest(userKey,
                    searchKey, 40.0f, 40.0f, willingMatches);

            updateRequest.sendRequest(new ServerRequest.Callback() {
                @Override
                public void onSuccess(int responseCode, ServerResponse responseMessage) {
                    super.onSuccess(responseCode, responseMessage);

                    // End the searcher if a match is confirmed
                    if (responseMessage.has(Keys.CONFIRMED)) {
                        onConfirmedMatch();
                        return;
                    }

                    // Signal that a meetup is willing to join this meetup
                    else if (responseMessage.has(Keys.PENDING_MATCH)) {
                        String pending = responseMessage.getString(Keys.PENDING_MATCH);
                        if (pendingMatchKey == null) {
                            pendingMatchKey = pending;
                            onPendingMatch(pending);
                        }
                    }

                    // Signal that the user could not match with the pending meetup
                    else if (pendingMatchKey != null) {
                        onDeclinePendingMatch();
                        pendingMatchKey = null;
                    }

                    onResultUpdate(responseMessage.getSearchMeetupArray(Keys.MATCHES));
                }
            });
        }
    }

    /** */
    @Override
    protected final void onStop() {
        // Notify the server that this user has canceled searching for their meetup
        ServerRequest.createStopSearchRequest(userKey, searchKey).sendRequest();
    }

    /** */
    public abstract void onResultUpdate(List<SearchMeetupData> nearbyMeetups);

    /** */
    public abstract void onConfirmedMatch();

    /** */
    public abstract void onPendingMatch(String otherSearchKey);

    /** */
    public abstract void onDeclinePendingMatch();
}
