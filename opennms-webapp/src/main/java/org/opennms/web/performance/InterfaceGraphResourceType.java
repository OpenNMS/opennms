package org.opennms.web.performance;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

public class InterfaceGraphResourceType implements GraphResourceType {

    private PerformanceModel m_performanceModel;

    public InterfaceGraphResourceType(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

    public String getName() {
        return "interface";
    }
    
    public String getLabel() {
        return "Interface";
    }
    
    public boolean isResourceTypeOnNode(int nodeId) {
        try {
            return m_performanceModel.getQueryableInterfacesForNode(nodeId).size() > 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public List<GraphResource> getResourcesForNode(int nodeId) {
        ArrayList<GraphResource> graphResources =
            new ArrayList<GraphResource>();

        List<String> ifaces = 
            m_performanceModel.getQueryableInterfacesForNode(nodeId);
        for (String iface : ifaces) {
            graphResources.add(getResourceByNodeAndInterface(nodeId, iface));
        }

        return graphResources;
    }
    
    public GraphResource getResourceByNodeAndInterface(int nodeId,
            String intf) throws DataAccessException {
        String label;
        try {
            label = m_performanceModel.getHumanReadableNameForIfLabel(nodeId,
                                                                      intf);
        } catch (SQLException e) {
            SQLErrorCodeSQLExceptionTranslator translator =
                new SQLErrorCodeSQLExceptionTranslator();
            throw translator.translate("Getting human readable name for "
                                       + "interface label", null, e);
        }

        Set<GraphAttribute> set =
            new LazySet(new AttributeLoader(nodeId, intf));
        return new DefaultGraphResource(intf, label, set);
    }
    
    public class AttributeLoader implements LazySet.Loader {
        
        private int m_nodeId;
        private String m_intf;

        public AttributeLoader(int nodeId, String intf) {
            m_nodeId = nodeId;
            m_intf = intf;
        }

        public Set<GraphAttribute> load() {
            List<String> dataSources =
                m_performanceModel.getDataSourceList(Integer.toString(m_nodeId), m_intf,
                                                     false);
            Set<GraphAttribute> attributes =
                new HashSet<GraphAttribute>(dataSources.size());
            
            for (String dataSource : dataSources) {
                attributes.add(new RrdGraphAttribute(dataSource));
            }
            
            return attributes;
        }
        
    }

    public String getRelativePathForAttribute(String resourceParent, String resource, String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(resourceParent);
        buffer.append(File.separator);
        buffer.append(resource);
        buffer.append(File.separator);
        buffer.append(attribute);
        buffer.append(RrdFileConstants.RRD_SUFFIX);
        return buffer.toString();
    }

    /**
     * This resource type is never available for domains.
     * Only the interface resource type is available for domains.
     */
    public boolean isResourceTypeOnDomain(String domain) {
        return m_performanceModel.getQueryableInterfacesForDomain(domain).size() > 0;
    }
    
    public List<GraphResource> getResourcesForDomain(String domain) {
        ArrayList<GraphResource> graphResources =
            new ArrayList<GraphResource>();

        List<String> ifaces = 
            m_performanceModel.getQueryableInterfacesForDomain(domain);
        for (String iface : ifaces) {
            graphResources.add(getResourceByDomainAndInterface(domain, iface));
        }

        return graphResources;
    }

    private GraphResource getResourceByDomainAndInterface(String domain, String intf) {
        Set<GraphAttribute> set =
            new LazySet(new DomainAttributeLoader(domain, intf));
        return new DefaultGraphResource(intf, intf, set);
    }

    public class DomainAttributeLoader implements LazySet.Loader {
        
        private String m_domain;
        private String m_intf;

        public DomainAttributeLoader(String domain, String intf) {
            m_domain = domain;
            m_intf = intf;
        }

        public Set<GraphAttribute> load() {
            List<String> dataSources =
                m_performanceModel.getDataSourceList(m_domain, m_intf,
                                                     false);
            Set<GraphAttribute> attributes =
                new HashSet<GraphAttribute>(dataSources.size());
            
            for (String dataSource : dataSources) {
                attributes.add(new RrdGraphAttribute(dataSource));
            }
            
            return attributes;
        }
        
    }

}
