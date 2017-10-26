package com.dhrw.sitwithus.server;

public class ServerResponse {

    // A list of response codes indicating what type of error occurred while handling the request
    public static final int RESPONSE_IO_EXCEPTION = -1;

    public static final int RESPONSE_JSON_EXCEPTION = -2;

    public static final int RESPONSE_SUCCESS = 200;

    // The HTTP response status codes indicting success or error type
    public final int responseCode;

    // The text the response from the sever or the error message
    public final String responseMessage;

    ServerResponse(int responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }
}
