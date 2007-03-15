package org.opennms.netmgt.threshd;

public class ThresholdExpressionException extends Exception {
    public ThresholdExpressionException(String message) {
        super(message);
    }
    
    public ThresholdExpressionException(String message, Throwable cause) {
        super(message,cause);
    }
}
