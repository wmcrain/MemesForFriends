package com.dhrw.sitwithus.sync;

import android.util.Log;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;

/**
 * Created by ryanmitchell on 12/5/17.
 */

public abstract class RequestLooper {

    /** The set of states in which the updater thread can exists. */
    protected enum State {
        INITIALIZED,
        RUNNING,
        STOPPED
    }

    // The thread that retrieves updated search results from the server periodically
    private final Thread runUpdate = new Thread() {
        @Override
        public void run() {
            try {
                while (state == RequestLooper.State.RUNNING) {
                    onLoop();
                    sleep(loopInterval);
                }
            } catch (InterruptedException e) {
                Log.e("SitWithUs", e.toString());
            }
        }
    };

    // The current state of the updater thread
    private State state;
    private long loopInterval;

    public RequestLooper(long loopInterval) {
        this.state = State.INITIALIZED;
        this.loopInterval = loopInterval;
    }

    protected final void threadStart() {
        // Launch the meetup search update loop to retrieve information about
        // potential matches
        runUpdate.start();
    }

    public void start() {
        // The thread cannot be restart to raise an exception
        if (state != State.INITIALIZED) {
            throw new IllegalStateException("The searcher cannot be restarted");
        }

        state = State.RUNNING;
        onStart();
    }

    /** */
    public void stop() {
        if (state == State.RUNNING) {

            // Set the flag to notify the update loop thread that it should cease
            state = State.STOPPED;
            onStop();
        }
    }

    protected abstract void onLoop();

    protected abstract void onStart();

    protected abstract void onStop();

}
