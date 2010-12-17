package org.opennms.web.controller.admin.support;

public class SystemReportException extends Exception {
    private static final long serialVersionUID = 1L;

    public SystemReportException(final Throwable t) {
        super(t);
    }

    public SystemReportException(final String message) {
        super(message);
    }

}
