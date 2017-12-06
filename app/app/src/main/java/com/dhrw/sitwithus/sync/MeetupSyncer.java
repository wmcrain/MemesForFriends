package com.dhrw.sitwithus.sync;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;

public class MeetupSyncer extends RequestLooper {

    // The amount of time in milliseconds between searching updates
    private static final long UPDATE_INTERVAL = 10 * 1000;

    private final String userKey;

    public MeetupSyncer(String userKey) {
        super(UPDATE_INTERVAL);
        this.userKey = userKey;
    }

    @Override
    protected void onStart() {
        ServerRequest.createStartMeetupRequest(userKey)
                .sendRequest(new ServerRequest.Callback() {

                    @Override
                    public void onSuccess(int responseCode, ServerResponse responseMessage) {
                        super.onSuccess(responseCode, responseMessage);
                        threadStart();
                    }
                });

    }

    @Override
    protected void onLoop() {
        ServerRequest.createMeetupUpdateRequest(userKey).sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);

            }
        });
    }

    @Override
    protected void onStop() {
        ServerRequest.createLeaveMeetupRequest(userKey);
    }
}
