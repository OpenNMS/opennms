package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.web.Util;
import org.opennms.web.graph.ResourceId;
import org.opennms.web.performance.GraphAttribute;
import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;

public class ChooseResourceModel {
    private String m_endUrl;
    private String m_parentResourceTypeName;
    private String m_parentResourceTypeLabel;
    private String m_parentResourceName;
    private String m_parentResourceLabel;
    private String m_parentResourceLink;
    private Map<GraphResourceType, List<ChooseGraphResource>> m_resourceTypes;

    public Map<GraphResourceType, List<ChooseGraphResource>> getResourceTypes() {
        return m_resourceTypes;
    }

    public void setResourceTypes(
            Map<GraphResourceType, List<GraphResource>> resourceTypes) {
        Map<GraphResourceType, List<ChooseGraphResource>> newResourceTypes =
            new LinkedHashMap<GraphResourceType, List<ChooseGraphResource>>();
        
        for (Map.Entry<GraphResourceType, List<GraphResource>> entry : resourceTypes.entrySet()) {
            List<ChooseGraphResource> newResources = new ArrayList<ChooseGraphResource>();
            for (GraphResource resource : entry.getValue()) {
                newResources.add(new ChooseGraphResource(resource, entry.getKey().getName()));
            }
            newResourceTypes.put(entry.getKey(), newResources);
        }
        m_resourceTypes = newResourceTypes;
    }

    public String getEndUrl() {
        return m_endUrl;
    }

    public void setEndUrl(String endUrl) {
        m_endUrl = endUrl;
    }

    public String getParentResourceLabel() {
        return m_parentResourceLabel;
    }

    public void setParentResourceLabel(String parentResourceLabel) {
        m_parentResourceLabel = parentResourceLabel;
    }

    public String getParentResourceLink() {
        return m_parentResourceLink;
    }

    public void setParentResourceLink(String parentResourceLink) {
        m_parentResourceLink = parentResourceLink;
    }

    public String getParentResourceTypeLabel() {
        return m_parentResourceTypeLabel;
    }

    public void setParentResourceTypeLabel(String parentResourceTypeLabel) {
        m_parentResourceTypeLabel = parentResourceTypeLabel;
    }

    public String getParentResourceTypeName() {
        return m_parentResourceTypeName;
    }

    public void setParentResourceTypeName(String parentResourceTypeName) {
        m_parentResourceTypeName = parentResourceTypeName;
    }

    public String getParentResourceName() {
        return m_parentResourceName;
    }

    public void setParentResourceName(String parentResourceName) {
        m_parentResourceName = parentResourceName;
    }
    
    public class ChooseGraphResource implements GraphResource {
        private GraphResource m_delegate;
        private String m_resourceTypeName;

        public ChooseGraphResource(GraphResource resource, String resourceTypeName) {
            m_delegate = resource;
            m_resourceTypeName = resourceTypeName;
        }

        public String getResourceId() {
            ResourceId r = 
                new ResourceId(m_parentResourceTypeName, m_parentResourceName,
                               m_resourceTypeName, m_delegate.getName());
            return r.getResourceId();
        }

        public Set<GraphAttribute> getAttributes() {
            return m_delegate.getAttributes();
        }

        public String getLabel() {
            return m_delegate.getLabel();
        }

        public String getName() {
            return m_delegate.getName();
        }
    }

}
