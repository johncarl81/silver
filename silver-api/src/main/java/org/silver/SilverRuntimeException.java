package org.silver;

/**
 * @author John Ericksen
 */
public class SilverRuntimeException extends RuntimeException {

    public SilverRuntimeException(String message) {
        super(message);
    }

    public SilverRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SilverRuntimeException(Throwable cause) {
        super(cause);
    }
}
