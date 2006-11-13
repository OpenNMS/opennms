package org.opennms.web.performance;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.LazySet;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.RrdFileConstants;
import org.opennms.web.graph.GraphModel;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.response.ResponseTimeModel;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.orm.ObjectRetrievalFailureException;

public class DistributedStatusGraphResourceType implements GraphResourceType {
    public static final String DISTRIBUTED_DIRECTORY = "distributed";
    
    private PerformanceModel m_performanceModel;

    public DistributedStatusGraphResourceType(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

    public String getLabel() {
        return "Distributed Status";
    }

    public String getName() {
        return "distributedStatus";
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
            + "monitors.definitionname, monitors.id, ipinterface.ipaddr "
            + "FROM location_specific_status_changes changes, "
            + "location_monitors monitors, ifservices, ipinterface "
            + "WHERE changes.locationmonitorid = monitors.id "
            + "AND changes.ifserviceid = ifservices.id "
            + "AND ifservices.ipinterfaceid = ipinterface.id "
            + "AND ipinterface.nodeid = ?";
        
        LinkedList<DefaultGraphResource> resources =
            new LinkedList<DefaultGraphResource>();

        try {
            Connection conn = Vault.getDbConnection();

            try {
                PreparedStatement stmt = conn.prepareStatement(preparedSelect);
                stmt.setInt(1, nodeId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String definitionName = rs.getString("definitionname");
                    int id = rs.getInt("id");
                    String ipAddr = rs.getString("ipaddr");
                    
                    File iface = getInterfaceDirectory(id, ipAddr);
                    
                    if (iface.isDirectory()) {
                        resources.add(createResource(definitionName, id,
                                                     ipAddr));
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
    
    public List<GraphResource> getResourcesForLocationMonitor(int locationMonitorId) {
        ArrayList<GraphResource> graphResources =
            new ArrayList<GraphResource>();

        /*
         * Verify that the node directory exists so we can throw a good
         * error message if not.
         */
        File locationMonitorDirectory;
        try {
            locationMonitorDirectory =
                getLocationMonitorDirectory(locationMonitorId, true);
        } catch (DataAccessException e) {
            throw new ObjectRetrievalFailureException("The '" + getName() + "' resource type does not exist on this location Monitor.  Nested exception is: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
        
        File[] intfDirs =
            locationMonitorDirectory.listFiles(RrdFileConstants.INTERFACE_DIRECTORY_FILTER);

        // XXX is this test even needed?
        if (intfDirs == null) {
            return graphResources; 
        }

        // XXX this isn't right at all
        for (File intfDir : intfDirs) {
            String d = intfDir.getName();
            String defName = getDefinitionNameFromLocationMonitorDirectory(d);
            int id = getLocationMonitorIdFromLocationMonitorDirectory(d);
            graphResources.add(createResource(defName, id, intfDir.getName()));
        }

        return graphResources;
    }

    private DefaultGraphResource createResource(String definitionName,
            int locationMonitorId, String intf) {
        String monitor = definitionName + "-" + locationMonitorId;
        
        String label = intf + " from " + monitor;
        String resource = locationMonitorId + "/" + intf;

        Set<GraphAttribute> set =
            new LazySet(new AttributeLoader(definitionName, locationMonitorId,
                                            intf));
        return new DefaultGraphResource(resource, label, set);
    }

    public boolean isResourceTypeOnDomain(String domain) {
        return false;
    }

    public boolean isResourceTypeOnNode(int nodeId) {
        return getResourcesForNode(nodeId).size() > 0;
    }
    
    private int getLocationMonitorIdFromResource(String resource) {
        int index = resource.indexOf("/");
        if (index == -1) {
            throw new IllegalArgumentException("Resource name \"" + resource
                                               + "\" isn't a valid resource "
                                               + "for resource type " +
                                               getName());
        }
        String dir = resource.substring(0, index);
        return getLocationMonitorIdFromLocationMonitorDirectory(dir); 
    }
    
    private String getIpAddressFromResource(String resource) {
        int index = resource.indexOf("/");
        if (index == -1) {
            throw new IllegalArgumentException("Resource name \"" + resource
                                               + "\" isn't a valid resource "
                                               + "for resource type " +
                                               getName());
        }
        return resource.substring(index + 1);
    }

    private String getDefinitionNameFromLocationMonitorDirectory(String dir) {
        int index = dir.indexOf("-");
        if (index == -1) {
            throw new IllegalArgumentException("Location monitor directory \""
                                               + dir + "\" isn't a valid "
                                               + "location monitor directory");
        }
        return dir.substring(0, index);
    }

    private int getLocationMonitorIdFromLocationMonitorDirectory(String dir) {
        int index = dir.indexOf("-");
        if (index == -1) {
            throw new IllegalArgumentException("Location monitor directory \""
                                               + dir + "\" isn't a valid "
                                               + "location monitor directory");
        }
        return Integer.parseInt(dir.substring(index + 1));
    }
    
    public File getRrdDirectory() {
        return new File(m_performanceModel.getResponseTimeModel().getRrdDirectory(), DISTRIBUTED_DIRECTORY);
    }
    
    public File getRrdDirectory(boolean verify) {
        File rrdDirectory = getRrdDirectory();
        
        if (verify && !rrdDirectory.isDirectory()) {
            throw new IllegalArgumentException("RRD directory does not exist: "
                                               + rrdDirectory.getAbsolutePath());
        }
        
        return rrdDirectory;
    }
    
    public File getInterfaceDirectory(int id, String ipAddr) {
        File monitor = new File(getRrdDirectory(), Integer.toString(id));
        return new File(monitor, ipAddr);
    }


    private File getLocationMonitorDirectory(int locationMonitorId, boolean verify) throws ObjectRetrievalFailureException {
        return getLocationMonitorDirectory(Integer.toString(locationMonitorId), verify);
    }
    
    private File getLocationMonitorDirectory(String locationMonitorId, boolean verify) throws ObjectRetrievalFailureException {
        File locationMonitorDirectory = new File(getRrdDirectory(verify), locationMonitorId);

        if (verify && !locationMonitorDirectory.isDirectory()) {
            throw new ObjectRetrievalFailureException(File.class, "No node directory exists for node " + locationMonitorId + ": " + locationMonitorDirectory);
        }
        
        return locationMonitorDirectory;
    }
    
    public List<PrefabGraph> getAvailablePrefabGraphs(Set<GraphAttribute> attributes) {
        PrefabGraph[] graphs =
            m_performanceModel.getResponseTimeModel().getQueriesByResourceTypeAttributes(null,
                                                                  attributes);
        return Arrays.asList(graphs);
    }
    
    public PrefabGraph getPrefabGraph(String name) {
        return m_performanceModel.getResponseTimeModel().getQuery(name);
    }
    
    private Category log() {
        return ThreadCategory.getInstance();
    }
    
    public class AttributeLoader implements LazySet.Loader {
        private String m_definitionName;
        private int m_locationMonitorId;
        private String m_intf;

        public AttributeLoader(String definitionName, int locationMonitorId,
                String intf) {
            m_definitionName = definitionName;
            m_locationMonitorId = locationMonitorId;
            m_intf = intf;
        }

        public Set<GraphAttribute> load() {
            String resource = m_definitionName + "-" + m_locationMonitorId
                + "/" + m_intf;
            
            File directory = getInterfaceDirectory(m_locationMonitorId,
                                                   m_intf);
            log().debug("lazy-loading attributes for resource \"" + resource
                        + "\" from directory " + directory);
            List<String> dataSources =
                m_performanceModel.getDataSourcesInDirectory(directory);

            Set<GraphAttribute> attributes =
                new HashSet<GraphAttribute>(dataSources.size());
            
            for (String dataSource : dataSources) {
                log().debug("Found data source \"" + dataSource + "\" on "
                            + resource);
                            
                attributes.add(new RrdGraphAttribute(dataSource));
            }
            
            return attributes;
        }
        
    }
    
    public GraphModel getModel() {
        return m_performanceModel;
    }
}
