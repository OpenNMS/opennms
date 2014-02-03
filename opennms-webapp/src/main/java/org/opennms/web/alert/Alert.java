package org.opennms.web.alert;

public class Alert {

    private final AlertType type;

    private final String message;

    public Alert(String message) {
        this(message, AlertType.INFO);
    }

    public Alert(String message, AlertType type) {
        this.message = message;
        this.type = type;
    }

    public AlertType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
