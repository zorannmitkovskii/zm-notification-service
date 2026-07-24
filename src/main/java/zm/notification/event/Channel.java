package zm.notification.event;

/** Delivery channel. EMAIL is implemented today; SMS/PUSH/IN_APP are the
 *  reason the event + service are channel-agnostic from day one. */
public enum Channel {
    EMAIL,
    SMS,
    PUSH,
    IN_APP
}
