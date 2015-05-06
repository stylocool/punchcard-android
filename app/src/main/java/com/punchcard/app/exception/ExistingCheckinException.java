package com.punchcard.app.exception;

/**
 * Created by jasonpang on 19/4/15.
 */
public class ExistingCheckinException extends Exception {

    public ExistingCheckinException() {

    }

    @Override
    public String getMessage() {
        return "Existing checkin record found! Please checkout first before checkin.";
    }
}
