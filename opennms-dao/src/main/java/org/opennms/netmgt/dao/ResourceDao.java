package org.opennms.netmgt.dao;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public interface ResourceDao {
    
    public File getRrdDirectory();
    
    public File getRrdDirectory(boolean verify);

    public Collection<OnmsResourceType> getResourceTypes();
    
    public OnmsResource getResourceById(String id);
    
    public List<OnmsResource> findNodeResources();

    public List<OnmsResource> findDomainResources();

}
