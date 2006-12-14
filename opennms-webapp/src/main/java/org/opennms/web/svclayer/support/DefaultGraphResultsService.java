package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.graph.RelativeTimePeriod;
import org.opennms.web.graph.ResourceId;
import org.opennms.web.graph.GraphResults.GraphResultSet;
import org.opennms.web.performance.GraphAttribute;
import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;
import org.opennms.web.performance.PerformanceModel;
import org.opennms.web.svclayer.GraphResultsService;

public class DefaultGraphResultsService implements GraphResultsService {

    private PerformanceModel m_performanceModel;

    private NodeDao m_nodeDao;

    private RelativeTimePeriod[] m_periods;

    public DefaultGraphResultsService() {
        // Should this be injected, as well?
        m_periods = RelativeTimePeriod.getDefaultPeriods();
    }

    public GraphResults findResults(ResourceId[] resources,
            String[] reports, long start, long end, String relativeTime) {
        if (resources == null) {
            throw new IllegalArgumentException("resources argument cannot be null");
        }
        if (reports == null) {
            throw new IllegalArgumentException("reports argument cannot be null");
        }
        if (end < start) {
            throw new IllegalArgumentException("end time cannot be before start time");
        }

        GraphResults graphResults = new GraphResults();
        graphResults.setStart(new Date(start));
        graphResults.setEnd(new Date(end));
        graphResults.setRelativeTime(relativeTime);
        graphResults.setRelativeTimePeriods(m_periods);
        graphResults.setReports(reports);

        for (ResourceId resource : resources) {
            graphResults.addGraphResultSet(createGraphResultSet(resource, reports, graphResults));
        }
        
        return graphResults;
    }
    
     public GraphResultSet createGraphResultSet(ResourceId r, String[] reports, GraphResults graphResults) {
        String parentResourceType = r.getParentResourceType();
        String parentResource = r.getParentResource();
        String resourceType = r.getResourceType();
        String resource = r.getResource();
        
        GraphResourceType rt = m_performanceModel.getResourceTypeByName(resourceType);
        
        GraphResource graphResource;
        
        GraphResultSet rs = graphResults.new GraphResultSet();

        if ("node".equals(parentResourceType)) {
            int nodeId;
            try {
                nodeId = Integer.parseInt(parentResource);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Could not parse parentResource parameter "
                                                   + "into an integer for a node", e);
            }

            OnmsNode n = m_nodeDao.get(nodeId);
            if (n == null) {
                throw new IllegalArgumentException("could find node with a "
                                                   + "nodeId of " + nodeId);
            }
            rs.setParentResourceLink("element/node.jsp?node=" + nodeId);
            rs.setParentResourceLabel(n.getLabel());


            graphResource =
                m_performanceModel.getResourceForNodeResourceResourceType(nodeId,
                                                                          resource,
                                                                          resourceType);
            rs.setParentResourceTypeLabel("Node");
            rs.setResourceTypeLabel(rt.getLabel());
            rs.setResourceLabel(graphResource.getLabel());
        } else if ("domain".equals(parentResourceType)) {
            graphResource =
                m_performanceModel.getResourceForDomainResourceResourceType(parentResource,
                                                                            resource,
                                                                            resourceType);

            rs.setParentResourceTypeLabel("Domain");
            rs.setParentResourceLabel(parentResource);
            rs.setResourceTypeLabel("Interface");
            rs.setResourceLabel(resource);
        } else {
            throw new IllegalArgumentException("parentResourceType of '"
                                               + parentResourceType
                                               + "' is not supported.");
        }

        if (reports.length == 1 && "all".equals(reports[0])) {
            Set<GraphAttribute> attributes = graphResource.getAttributes();

            PrefabGraph[] queries =
                m_performanceModel.getQueriesByResourceTypeAttributes(rt.getName(), attributes);
            List<String> queryNames = new ArrayList<String>(queries.length);
            for (PrefabGraph query : queries) {
                queryNames.add(query.getName());
            }

            reports = queryNames.toArray(new String[queryNames.size()]);
        }
        
        rs.setParentResourceType(parentResourceType);
        rs.setParentResource(parentResource);
        rs.setResourceType(resourceType);
        rs.setResource(resource);

        rs.initializeGraphs(m_performanceModel, reports);
        
        return rs;
    }

    public PerformanceModel getPerformanceModel() {
        return m_performanceModel;
    }

    public void setPerformanceModel(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
}
