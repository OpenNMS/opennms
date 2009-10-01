package org.opennms.netmgt.provision.config.linkadapter;

import java.util.Set;

public interface LinkAdapterConfigurationDao {

    public Set<LinkPattern> getPatterns();
    
    public void setPatterns(Set<LinkPattern> patterns);
    
    public void saveCurrent();
    
}
