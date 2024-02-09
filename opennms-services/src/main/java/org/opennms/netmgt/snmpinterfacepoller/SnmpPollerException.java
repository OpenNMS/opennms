package org.opennms.netmgt.snmpinterfacepoller;

public class SnmpPollerException extends RuntimeException {
    public SnmpPollerException(Throwable throwable) {
        super(throwable);
    }
}
