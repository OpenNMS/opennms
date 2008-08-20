package org.opennms.netmgt.protocols.ssh;

public class SshException extends Exception {
    private static final long serialVersionUID = 1L;

    public SshException() {
        super();
    }

    public SshException(String message) {
        super(message);
    }

    public SshException(Throwable cause) {
        super(cause);
    }

    public SshException(String message, Throwable cause) {
        super(message, cause);
    }

}
