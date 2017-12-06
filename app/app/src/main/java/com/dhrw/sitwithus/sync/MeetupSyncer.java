package com.dhrw.sitwithus.sync;

import com.dhrw.sitwithus.server.ServerRequest;
import com.dhrw.sitwithus.server.ServerResponse;
import com.dhrw.sitwithus.util.Keys;

import java.util.List;

public abstract class MeetupSyncer extends RequestLooper {

    // The amount of time in milliseconds between searching updates
    private static final long UPDATE_INTERVAL = 10 * 1000;

    private final String userKey;

    public MeetupSyncer(String userKey) {
        super(UPDATE_INTERVAL);
        this.userKey = userKey;
    }

    @Override
    protected final void onStart() {
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
    protected final void onLoop() {
        ServerRequest.createMeetupUpdateRequest(userKey).sendRequest(new ServerRequest.Callback() {
            @Override
            public void onSuccess(int responseCode, ServerResponse responseMessage) {
                super.onSuccess(responseCode, responseMessage);
                onMembersChanged(responseMessage.getStringArray(Keys.USERNAME));
            }
        });
    }

    @Override
    protected final void onStop() {
        ServerRequest.createLeaveMeetupRequest(userKey);
    }

    protected abstract void onMembersChanged(List<String> usernames);
}
