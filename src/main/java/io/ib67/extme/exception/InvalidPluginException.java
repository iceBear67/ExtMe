package io.ib67.extme.exception;

public class InvalidPluginException extends IllegalArgumentException{
    public InvalidPluginException(String s) {
        super(s);
    }

    public InvalidPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
