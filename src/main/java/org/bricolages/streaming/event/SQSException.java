package org.bricolages.streaming.event;
import org.bricolages.streaming.ApplicationError;

public class SQSException extends ApplicationError {
    SQSException(String message) {
        super(message);
    }

    SQSException(Exception cause) {
        super(cause);
    }
}
