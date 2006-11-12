package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.web.Util;
import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.PrefabGraph;
import org.opennms.web.graph.RelativeTimePeriod;
import org.opennms.web.performance.GraphAttribute;
import org.opennms.web.performance.GraphResource;
import org.opennms.web.performance.GraphResourceType;
import org.opennms.web.performance.PerformanceModel;
import org.opennms.web.response.ResponseTimeModel;
import org.opennms.web.svclayer.GraphResultsService;

public class DefaultGraphResultsService implements GraphResultsService {

    private PerformanceModel m_performanceModel;

    private ResponseTimeModel m_responseTimeModel;
    
    private NodeDao m_nodeDao;

    private RelativeTimePeriod[] m_periods;

    public DefaultGraphResultsService() {
        // Should this be injected, as well?
        m_periods = RelativeTimePeriod.getDefaultPeriods();
    }

    public GraphResults findResults(String graphType, String parentResourceType,
            String parentResource, String resourceType, String resource,
            String[] reports, long start, long end, String relativeTime) {
        if (graphType == null) {
            throw new IllegalArgumentException("graphType argument cannot be null");
        }
        if (parentResourceType == null) {
            throw new IllegalArgumentException("parentResourceType argument cannot be null");
        }
        if (parentResource == null) {
            throw new IllegalArgumentException("parentResource argument cannot be null");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("resourceType argument cannot be null");
        }
        if (resource == null) {
            throw new IllegalArgumentException("resource argument cannot be null");
        }
        if (reports == null) {
            throw new IllegalArgumentException("reports argument cannot be null");
        }
        if (end < start) {
            throw new IllegalArgumentException("end time cannot be before start time");
        }

        GraphResults graphResults = new GraphResults();
        graphResults.setType(graphType);

        if ("performance".equals(graphType)) {
            GraphResourceType rt = m_performanceModel.getResourceTypeByName(resourceType);
            GraphResource r;

            if ("node".equals(parentResourceType)) {
                int nodeId;
                try {
                    nodeId = Integer.parseInt(parentResource);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Could not parse parentResource parameter "
                                               + "into an integer for a node", e);
                }

                graphResults.setNodeId(nodeId);
                OnmsNode n = m_nodeDao.get(nodeId);
                if (n == null) {
                    throw new IllegalArgumentException("could find node with a "
                                                       + "nodeId of " + nodeId);
                }
                graphResults.setNodeLabel(n.getLabel());
                graphResults.setParentResourceLink("element/node.jsp?node=" + nodeId);
                graphResults.setParentResourceLabel(n.getLabel());


                r = m_performanceModel.getResourceForNodeResourceResourceType(
                                                                                            nodeId,
                                                                                            resource,
                                                                                            resourceType);
                graphResults.setParentResourceTypeLabel("Node");
                graphResults.setResourceTypeLabel(rt.getLabel());
                graphResults.setResourceLabel(r.getLabel());

                graphResults.setModel(rt.getModel());
            } else if ("domain".equals(parentResourceType)) {
                graphResults.setModel(m_performanceModel);
                
                r = m_performanceModel.getResourceForDomainResourceResourceType(
                                                                                            parentResource,
                                                                                            resource,
                                                                                            resourceType);

                graphResults.setDomain(parentResource);

                graphResults.setParentResourceTypeLabel("Domain");
                graphResults.setParentResourceLabel(parentResource);
                graphResults.setResourceTypeLabel("Interface");
                graphResults.setResourceLabel(resource);
            } else {
                throw new IllegalArgumentException("parentResourceType of '" + parentResourceType + "' is not supported.");
            }
            
            if (reports.length == 1 && "all".equals(reports[0])) {
                Set<GraphAttribute> attributes = r.getAttributes();

                List<PrefabGraph> queries =
                    rt.getAvailablePrefabGraphs(attributes);
                List<String> queryNames = new ArrayList<String>(queries.size());
                for (PrefabGraph query : queries) {
                    queryNames.add(query.getName());
                }

                reports = queryNames.toArray(new String[queryNames.size()]);
            }
        } else if ("response".equals(graphType)) {
            graphResults.setModel(m_responseTimeModel);

            int nodeId;
            try {
                nodeId = Integer.parseInt(parentResource);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Could not parse node's "
                                                   + "parentResource parameter '"
                                                   + parentResource
                                                   + "' into an integer: "
                                                   + e.getMessage(), e);
            }

            graphResults.setNodeId(nodeId);
            OnmsNode n = m_nodeDao.get(nodeId);
            if (n == null) {
                throw new IllegalArgumentException("could find node with a "
                                                   + "nodeId of " + nodeId);
            }
            graphResults.setNodeLabel(n.getLabel());
            graphResults.setParentResourceLink("element/node.jsp?node=" + nodeId);
            graphResults.setParentResourceLabel(n.getLabel());


            graphResults.setResourceTypeLabel("Interface");
            graphResults.setResourceLabel(resource);
            graphResults.setResourceLink("element/interface.jsp?node="
                                         + nodeId + "&intf="
                                         + Util.encode(resource));
            graphResults.setParentResourceTypeLabel("Node");
        } else {
            throw new IllegalArgumentException("graph type of '" + graphType + "' is not supported.");
        }
        
        Date startDate = new Date(start);
        Date endDate = new Date(end);
        
        graphResults.setParentResourceType(parentResourceType);
        graphResults.setParentResource(parentResource);
        graphResults.setResourceType(resourceType);
        graphResults.setResource(resource);
        graphResults.setReports(reports);
        graphResults.setStart(startDate);
        graphResults.setEnd(endDate);
        graphResults.setRelativeTime(relativeTime);
        graphResults.setRelativeTimePeriods(m_periods);

        if ("performance".equals(graphType) && "domain".equals(parentResourceType)) {
            graphResults.initializeDomainGraphs();
        } else {
            graphResults.initializeGraphs();
        }

        return graphResults;
    }

    public PerformanceModel getPerformanceModel() {
        return m_performanceModel;
    }

    public void setPerformanceModel(PerformanceModel performanceModel) {
        m_performanceModel = performanceModel;
    }

    public ResponseTimeModel getResponseTimeModel() {
        return m_responseTimeModel;
    }

    public void setResponseTimeModel(ResponseTimeModel responseTimeModel) {
        m_responseTimeModel = responseTimeModel;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }
}
