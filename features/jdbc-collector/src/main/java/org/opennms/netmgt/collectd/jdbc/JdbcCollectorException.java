package org.opennms.netmgt.collectd.jdbc;

public class JdbcCollectorException extends RuntimeException {
    private static final long serialVersionUID = 1315895761910431343L;

    public JdbcCollectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcCollectorException(String message) {
        super(message);
    }

}
