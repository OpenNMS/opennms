package org.opennms.netmgt.provision.config.dao;

import java.util.Set;

import org.opennms.netmgt.provision.config.linkadapter.LinkPattern;

public interface LinkAdapterConfigurationDao {

    public Set<LinkPattern> getPatterns();
    
    public void setPatterns(Set<LinkPattern> patterns);
    
    public void saveCurrent();
    
}
