package com.dhrw.sitwithus;

import android.util.Log;

import com.dhrw.sitwithus.data.SearchMeetup;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;

import java.util.ArrayList;
import java.util.List;

/** */
public abstract class MeetupSearcher {

    // The amount of time in milliseconds between searching updates
    private static final long UPDATE_INTERVAL = 10 * 1000;

    // The lock used to synchronize the changes to willing matches
    private final Object lock = new Object();

    // The thread that retrieves updated search results from the server periodically
    private final Thread runUpdate = new Thread() {
        @Override
        public void run() {
            try {
                while (state == MeetupSearcher.State.RUNNING) {

                    // Send the request to receive updated search data and call the onResultUpdate
                    // method when the request succeeds
                    synchronized (lock) {
                        ServerRequest updateRequest = ServerRequest.creaateUpdateSearchRequest(
                                searchKey, 40.0f, 40.0f, willingMatches);

                        updateRequest.sendRequest(new ServerRequest.Callback() {
                            @Override
                            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                                super.onSuccess(responseCode, responseMessage);

                                // End the searcher if a match is confirmed
                                if (responseMessage.has(Keys.CONFIRMED)) {
                                    onConfirmedMatch(responseMessage.getString(Keys.MEETUP_KEY));
                                    return;
                                }

                                // Signal that a meetup is willing to join this meetup
                                else if (responseMessage.has(Keys.PENDING_MATCH)) {
                                    String pending = responseMessage.getString(Keys.PENDING_MATCH);
                                    if (pendingMatchKey == null) {
                                        pendingMatchKey = pending;
                                        onPendingMatch(pending, new PendingMatchConfirmer(pending));
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

                    // Wait to retrieve updated match data from the server
                    sleep(UPDATE_INTERVAL);
                }
            } catch (InterruptedException e) {
                Log.e("SitWithUs", e.toString());
            }
        }
    };

    /** */
    public class PendingMatchConfirmer {

        private final String pendingMatchKey;

        private PendingMatchConfirmer(String pendingMatchKey) {
            this.pendingMatchKey = pendingMatchKey;
        }

        public void accept() {

        }

        public void decline() {

        }
    }

    // The list of search entity keys the user is willing to join
    private List<String> willingMatches;

    // The key of the currently logged in user
    private final String userKey;

    // The key of the searching entity the user is a part of
    private String searchKey;

    private String pendingMatchKey;

    /** The set of states in which the updater thread can exists. */
    private enum State {
        INITIALIZED,
        RUNNING,
        STOPPED
    }

    // The current state of the updater thread
    private State state;

    /** */
    public MeetupSearcher(String userKey) {
        this.state = State.INITIALIZED;
        this.userKey = userKey;
        this.willingMatches = new ArrayList<>();
        this.pendingMatchKey = null;
    }

    /** */
    public void setWilling(SearchMeetup meetup, boolean willing) {
        synchronized (lock) {
            if (willing) {
                willingMatches.add(meetup.entityKey);
            } else {
                willingMatches.remove(meetup.entityKey);
            }
        }
    }

    /** */
    public void start() {
        // The thread cannot be restart to raise an exception
        if (state != State.INITIALIZED) {
            throw new IllegalStateException("The searcher cannot be restarted");
        }

        // Add the user to a meetup
        ServerRequest.createStartSearchRequest(userKey, 0.0f, 0.0f)
                .sendRequest(new ServerRequest.Callback() {

                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);

                        //
                        searchKey = responseMessage.getString(Keys.SEARCH_KEY);

                        // Launch the meetup search update loop to retrieve information about
                        // potential matches
                        runUpdate.start();
                        state = State.RUNNING;
                    }
                });
    }

    /** */
    public void stop() {
        if (state == State.RUNNING) {

            // Notify the server that this user has canceled searching for their meetup
            ServerRequest.createStopSearchRequest(searchKey).sendRequest();

            // Set the flag to notify the update loop thread that it should cease
            state = State.STOPPED;
        }
    }

    /** */
    public abstract void onResultUpdate(List<SearchMeetup> nearbyMeetups);

    /** */
    public abstract void onConfirmedMatch(String meetupKey);

    /** */
    public abstract void onPendingMatch(String otherSearchKey, PendingMatchConfirmer confirmer);

    /** */
    public abstract void onDeclinePendingMatch();
}
