package scaryghost.utils;

import java.util.zip.DataFormatException;

/**
 * Exception class for improper time string format
 * @author etsai
 */
public class TimeFormatException extends DataFormatException {
    /** Constructs exception with no message */
    public TimeFormatException() {
        super();
    }
    
    /** Constructs exception with given message */
    public TimeFormatException(String msg) {
        super(msg);
    }
}
