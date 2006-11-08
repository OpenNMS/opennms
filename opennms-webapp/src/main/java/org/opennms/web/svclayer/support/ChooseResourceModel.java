package org.opennms.web.svclayer.support;

import java.util.List;
import java.util.Map;

import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;

public class ChooseResourceModel {
    private String m_endUrl;
    private String m_resourceTypeName;
    private String m_resourceTypeLabel;
    private String m_resourceLabel;
    private String m_resourceLink;
    
    private Map<GraphResourceType, List<GraphResource>> m_resourceTypes;

    public Map<GraphResourceType, List<GraphResource>> getResourceTypes() {
        return m_resourceTypes;
    }

    public void setResourceTypes(
            Map<GraphResourceType, List<GraphResource>> resourceTypes) {
        m_resourceTypes = resourceTypes;
    }

    public String getResourceLink() {
        return m_resourceLink;
    }

    public void setResourceLink(String resourceLink) {
        m_resourceLink = resourceLink;
    }

    public String getResourceLabel() {
        return m_resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        m_resourceLabel = resourceLabel;
    }

    public String getResourceTypeLabel() {
        return m_resourceTypeLabel;
    }

    public void setResourceTypeLabel(String resourceTypeLabel) {
        m_resourceTypeLabel = resourceTypeLabel;
    }

    public String getResourceTypeName() {
        return m_resourceTypeName;
    }

    public void setResourceTypeName(String resourceTypeName) {
        m_resourceTypeName = resourceTypeName;
    }

    public String getEndUrl() {
        return m_endUrl;
    }

    public void setEndUrl(String endUrl) {
        m_endUrl = endUrl;
    }

}
