package org.opennms.netmgt.vmmgr;

public class MissingDataSourceException extends Exception {
    private static final long serialVersionUID = 5312239709158792029L;

    public MissingDataSourceException() {
        super();
    }

    public MissingDataSourceException(final String message) {
        super(message);
    }

    public MissingDataSourceException(final Throwable cause) {
        super(cause);
    }

    public MissingDataSourceException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
