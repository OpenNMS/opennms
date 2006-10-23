package org.opennms.web.graph;

import java.sql.SQLException;

import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;

public interface GraphModel {
    public PrefabGraph getQuery(String resourceType, String report);

    public String getHumanReadableNameForIfLabel(int nodeId, String intf) throws SQLException;

    public String getRelativePathForAttribute(String resourceType,
            String resourceParent, String resource, String attribute);

    public String getType();
    
    public GraphResourceType getResourceTypeByName(String name);
    
    public GraphResource getResourceForNodeResourceResourceType(int nodeId, String resourceName, String resourceTypeName);
    
    public GraphResource getResourceForDomainResourceResourceType(String domain, String resourceName, String resourceTypeName);

}
