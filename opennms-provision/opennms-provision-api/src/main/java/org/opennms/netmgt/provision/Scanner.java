package org.opennms.netmgt.provision;

public interface Scanner {

    public void init();
    
    public void scan(ScanContext context) throws Exception;
}
