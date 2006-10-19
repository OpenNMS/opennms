package org.opennms.netmgt.collectd;

import java.io.File;

public interface ResourceIdentifier {
    
    public String getOwnerName();
    
    public File getResourceDir(RrdRepository repository);

}
