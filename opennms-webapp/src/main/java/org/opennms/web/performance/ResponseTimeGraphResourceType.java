package org.opennms.web.performance;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.graph.GraphModel;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.response.ResponseTimeModel;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

public class ResponseTimeGraphResourceType implements GraphResourceType {
    private ResponseTimeModel m_model;

    public ResponseTimeGraphResourceType(ResponseTimeModel model) {
        m_model = model;
    }

    public List<PrefabGraph> getAvailablePrefabGraphs(
            Set<GraphAttribute> attributes) {
        PrefabGraph[] graphs =
            m_model.getQueriesByResourceTypeAttributes(getName(), attributes);
        return Arrays.asList(graphs);
    }

    public String getLabel() {
        return "Response Time";
    }

    public GraphModel getModel() {
        return m_model;

    }

    public String getName() {
        return "responseTime";
    }

    public PrefabGraph getPrefabGraph(String name) {
        return m_model.getQuery(getName(), name);
    }

    public String getRelativePathForAttribute(String resourceParent,
            String resource, String attribute) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(resource);
        buffer.append(File.separator);
        buffer.append(attribute);
        buffer.append(RrdFileConstants.RRD_SUFFIX);
        return buffer.toString();
    }

    @SuppressWarnings("unchecked")
    public List<GraphResource> getResourcesForDomain(String domain) {
        return Collections.EMPTY_LIST;
    }

    public List<GraphResource> getResourcesForNode(int nodeId) {
        final String preparedSelect = "SELECT DISTINCT "
            + "ipinterface.ipaddr "
            + "FROM ipinterface "
            + "WHERE ipinterface.nodeid = ?";
        
        LinkedList<DefaultGraphResource> resources = new LinkedList<DefaultGraphResource>();

        try {
            Connection conn = Vault.getDbConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement(preparedSelect);
                stmt.setInt(1, nodeId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String ipAddr = rs.getString("ipaddr");
                    
                    File iface = getInterfaceDirectory(ipAddr);
                    
                    if (iface.isDirectory()) {
                        resources.add(createResource(ipAddr));
                    }
                }
                rs.close();
                stmt.close();
            } finally {
                Vault.releaseDbConnection(conn);
            }
        } catch (SQLException e) {
            SQLErrorCodeSQLExceptionTranslator translator =
                new SQLErrorCodeSQLExceptionTranslator();
            throw translator.translate("Getting list of interfaces from the "
                                       + "database for " + nodeId, null, e);

        }
        
        return DefaultGraphResource.sortIntoGraphResourceList(resources);
    }

    private File getInterfaceDirectory(String ipAddr) {
        return new File(getRrdDirectory(), ipAddr);
    }
    
    private DefaultGraphResource createResource(String intf) {
        String label = intf;
        String resource = intf;

        Set<GraphAttribute> set =
            new LazySet(new AttributeLoader(intf));
        return new DefaultGraphResource(resource, label, set);
    }


    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    public boolean isResourceTypeOnNode(int nodeId) {
        return getResourcesForNode(nodeId).size() > 0;
    }

    public File getRrdDirectory() {
        return m_model.getRrdDirectory();
    }

    private Category log() {
        return ThreadCategory.getInstance();
    }

    public class AttributeLoader implements LazySet.Loader {
        private String m_intf;

        public AttributeLoader(String intf) {
            m_intf = intf;
        }

        public Set<GraphAttribute> load() {
            File directory = getInterfaceDirectory(m_intf);
            log().debug("lazy-loading attributes for resource \"" + m_intf
                        + "\" from directory " + directory);
            List<String> dataSources =
                m_model.getDataSourcesInDirectory(directory);

            Set<GraphAttribute> attributes =
                new HashSet<GraphAttribute>(dataSources.size());
            
            for (String dataSource : dataSources) {
                log().debug("Found data source \"" + dataSource + "\" on "
                            + m_intf);
                            
                attributes.add(new RrdGraphAttribute(dataSource));
            }
            
            return attributes;
        }
    }
}
