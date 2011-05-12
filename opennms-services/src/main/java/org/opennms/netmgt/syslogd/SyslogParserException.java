package org.opennms.netmgt.syslogd;

public class SyslogParserException extends Exception {

    private static final long serialVersionUID = 8185991282482412701L;

    public SyslogParserException() {
        super();
    }

    public SyslogParserException(final String message) {
        super(message);
    }

    public SyslogParserException(final Throwable cause) {
        super(cause);
    }

    public SyslogParserException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
