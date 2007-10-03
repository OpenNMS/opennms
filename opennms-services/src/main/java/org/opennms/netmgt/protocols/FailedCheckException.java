package org.opennms.netmgt.protocols;

public class FailedCheckException extends Exception {

    public FailedCheckException(Throwable exception) {
        super(exception);
    }

}
