package org.opennms.netmgt.provision.adapters.link.config.dao;

import java.util.Set;

import org.opennms.netmgt.provision.adapters.link.config.linkadapter.LinkPattern;

public interface LinkAdapterConfigurationDao {

    public Set<LinkPattern> getPatterns();
    
    public void setPatterns(Set<LinkPattern> patterns);
    
    public void saveCurrent();
    
}
