package app.exceptions;

import java.util.logging.Level;
import java.util.logging.Logger;

public class IllegalStateException extends RuntimeException {

    public IllegalStateException(String message) {
        super(message);
    }

    public IllegalStateException(String userMessage, String systemMessage) {
        super(userMessage);
        Logger.getLogger("web").log(Level.SEVERE, systemMessage);
    }

}
