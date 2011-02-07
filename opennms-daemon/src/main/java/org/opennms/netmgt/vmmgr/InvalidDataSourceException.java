package org.opennms.netmgt.vmmgr;

public class InvalidDataSourceException extends Exception {
    private static final long serialVersionUID = -1236387740430245142L;

    public InvalidDataSourceException() {
        // TODO Auto-generated constructor stub
    }

    public InvalidDataSourceException(final String message) {
        super(message);
    }

    public InvalidDataSourceException(final Throwable cause) {
        super(cause);
    }

    public InvalidDataSourceException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
