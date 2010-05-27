package org.opennms.netmgt.provision.service;

public interface ScanProgress {
    
    public void abort(String message);
    public boolean isAborted();
}
