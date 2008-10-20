package org.opennms.netmgt.provision.support;

import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.provision.ServiceDetector;

public class NullDetectorMonitor implements DetectorMonitor{

    public void attempt(ServiceDetector detector, int attempt, String format,
            Object... args) {
    }

    public void error(ServiceDetector detector, Throwable t, String format,
            Object... args) {
        
    }

    public void failure(ServiceDetector detector, String format,
            Object... args) {
        
    }

    public void info(ServiceDetector detector, Exception e, String format,
            Object... args) {
        
    }

    public void start(ServiceDetector detector, String format, Object... args) {
        
    }

    public void stopped(ServiceDetector detector, String format,
            Object... args) {
        
    }

    public void success(ServiceDetector detector, String format,
            Object... args) {
        
    }

}
