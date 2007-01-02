package org.opennms.web.svclayer.support;

import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

public class ChooseResourceModel {
    private OnmsResource m_resource;
    private Map<OnmsResourceType, List<OnmsResource>> m_resourceTypes;
    private String m_endUrl;
    
    public void setResource(OnmsResource resource) {
        m_resource = resource;
    }
    
    public OnmsResource getResource() {
        return m_resource;
    }
    
    public Map<OnmsResourceType, List<OnmsResource>> getResourceTypes() {
        return m_resourceTypes;
    }

    public void setResourceTypes(Map<OnmsResourceType, List<OnmsResource>> resourceTypes) {
        m_resourceTypes = resourceTypes;
    }

    public String getEndUrl() {
        return m_endUrl;
    }

    public void setEndUrl(String endUrl) {
        m_endUrl = endUrl;
    }

}
