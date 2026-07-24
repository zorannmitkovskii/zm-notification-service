package zm.notification.email;

/** Thrown by an {@link EmailProvider} on any send failure. Retryable by the
 *  consumer; after the retry budget it lands on the dead-letter topic. */
public class EmailSendException extends RuntimeException {
    public EmailSendException(String message) { super(message); }
    public EmailSendException(String message, Throwable cause) { super(message, cause); }
}
