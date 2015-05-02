package com.punchcard.app.exception;

/**
 * Created by jasonpang on 19/4/15.
 */
public class NoCheckinException extends Exception {

    public NoCheckinException() {

    }

    @Override
    public String getMessage() {
        return "No checkin record found";
    }
}
