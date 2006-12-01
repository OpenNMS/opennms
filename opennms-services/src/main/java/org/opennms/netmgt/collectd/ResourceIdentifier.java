package org.opennms.netmgt.collectd;

import java.io.File;

import org.opennms.netmgt.model.RrdRepository;

public interface ResourceIdentifier {
    
    public String getOwnerName();
    
    public File getResourceDir(RrdRepository repository);

}
