package com.dhrw.sitwithus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dhrw.sitwithus.data.SearchMeetup;
import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;

import java.util.ArrayList;
import java.util.List;

public abstract class MeetupSearcher {

    //
    private final Object lock = new Object();

    //
    private ArrayList<SearchMeetup> nearbyMeetups;

    private final String userKey;

    private String searchKey;

    //
    private boolean isStarted;
    private boolean isStopped;

    private static final long UPDATE_INTERVAL = 10 * 1000;

    /** */
    public MeetupSearcher(String userKey) {
        this.userKey = userKey;
        isStarted = false;
        isStopped = false;
    }

    /** */
    private final Thread runUpdate = new Thread() {
        @Override
        public void run() {
            try {
                while (!isStopped) {

                    //
                    ServerRequest updateRequest = ServerRequest.creaateUpdateSearchRequest(
                            searchKey, 40.0f, 40.0f, new ArrayList<String>());

                    //
                    updateRequest.sendRequest(new ServerRequest.Callback() {
                        @Override
                        public void onSuccess(int responseCode, ServerResponse responseMessage) {
                            super.onSuccess(responseCode, responseMessage);

                            // TODO : Only call this method if the results change
                            onResultUpdate(responseMessage.getSearchMeetupArray(Keys.MATCHES));
                        }
                    });

                    // Wait to retrieve updated match data from the server
                    sleep(UPDATE_INTERVAL);
                }
            } catch (InterruptedException e) {
                Log.e("SitWithUs", e.toString());
            }
        }
    };

    /** Acquire the lock for the instance of the class to synchronize access. */
    private void acquire() {
        try {
            lock.wait();
        } catch (InterruptedException e) {
            Log.d("SitWithUs", e.toString());
        }
    }

    /** Release the lock for the instance of the class to synchronize access. */
    private void release() {
        lock.notify();
    }

    /** */
    public void start() {
        // The thread cannot be restart to raise an exception
        if (isStopped) {
            throw new IllegalStateException(
                    "The searcher has already been stopped and cannot be restarted");
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
                        isStarted = true;
                    }
                });
    }

    /** */
    public void stop() {
        if (isStarted && !isStopped) {

            // Notify the server that this user has canceled searching for their meetup
            ServerRequest.createStopSearchRequest(searchKey).sendRequest();

            // Set the flag to notify the update loop thread that it should cease
            isStopped = true;
        }
    }

    public abstract void onResultUpdate(List<SearchMeetup> nearbyMeetups);
}
