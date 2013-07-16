package org.opennms.netmgt.model.events;

public class EventProcessorException extends Exception {
    private static final long serialVersionUID = 3720890967518487477L;

    public EventProcessorException() {
        super();
    }

    public EventProcessorException(final String message) {
        super(message);
    }

    public EventProcessorException(final Throwable cause) {
        super(cause);
    }

    public EventProcessorException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
