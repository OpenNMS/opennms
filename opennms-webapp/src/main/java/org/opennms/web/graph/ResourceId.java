/**
 * 
 */
package org.opennms.web.graph;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.web.Util;

public class ResourceId {
    private String m_parentResourceType;
    private String m_parentResource;
    private String m_resourceType;
    private String m_resource;

    public ResourceId(String parentResourceType, String parentResource,
            String resourceType, String resource) {
        m_parentResourceType = parentResourceType;
        m_parentResource = parentResource;
        m_resourceType = resourceType;
        m_resource = resource;
    }
    
    public static ResourceId parseResourceId(String resourceId) {
        Pattern p = Pattern.compile("([^\\[]+)\\[([^\\]]*)\\]\\.([^\\[]+)\\[([^\\]]*)\\]");
        Matcher m = p.matcher(resourceId);
        if (!m.matches()) {
            throw new IllegalArgumentException("resourceId '" + resourceId
                                               + "' does not match pattern '"
                                               + p.toString() + "'");
        }
        
        String parentResourceType = Util.decode(m.group(1));
        String parentResource = Util.decode(m.group(2));
        String resourceType = Util.decode(m.group(3));
        String resource = Util.decode(m.group(4));

        return new ResourceId(parentResourceType, parentResource,
                            resourceType, resource);
    }
    

    public String getParentResource() {
        return m_parentResource;
    }

    public String getParentResourceType() {
        return m_parentResourceType;
    }

    public String getResource() {
        return m_resource;
    }

    public String getResourceType() {
        return m_resourceType;
    }

    public String getResourceId() {
        return Util.encode(m_parentResourceType) + "[" + Util.encode(m_parentResource) + "]."
               + Util.encode(m_resourceType) + "[" + Util.encode(m_resource) + "]";

    }

}