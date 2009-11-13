package org.opennms.netmgt.provision.adapters.link.endpoint.dao;

import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;

public interface EndPointConfigurationDao {

    public EndPointTypeValidator getValidator();
    
    public void save(EndPointTypeValidator validator);

    public String getXsd();
    
}
