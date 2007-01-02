package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.web.graph.Graph;
import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.RelativeTimePeriod;
import org.opennms.web.graph.GraphResults.GraphResultSet;
import org.opennms.web.svclayer.GraphResultsService;
import org.springframework.beans.factory.InitializingBean;

public class DefaultGraphResultsService implements GraphResultsService, InitializingBean {

    private ResourceDao m_resourceDao;
    
    private GraphDao m_graphDao;

    private NodeDao m_nodeDao;

    private RelativeTimePeriod[] m_periods;

    public DefaultGraphResultsService() {
        // Should this be injected, as well?
        m_periods = RelativeTimePeriod.getDefaultPeriods();
    }

    public GraphResults findResults(String[] resourceIds,
            String[] reports, long start, long end, String relativeTime) {
        if (resourceIds == null) {
            throw new IllegalArgumentException("resourceIds argument cannot be null");
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

        for (String resourceId : resourceIds) {
            graphResults.addGraphResultSet(createGraphResultSet(resourceId, reports, graphResults));
        }
        
        return graphResults;
    }
    
    public GraphResultSet createGraphResultSet(String resourceId, String[] reports, GraphResults graphResults) {
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        GraphResultSet rs = graphResults.new GraphResultSet();
        rs.setResource(resource);
        
        if (reports.length == 1 && "all".equals(reports[0])) {
            PrefabGraph[] queries = m_graphDao.getPrefabGraphsForResource(resource);
            List<String> queryNames = new ArrayList<String>(queries.length);
            for (PrefabGraph query : queries) {
                queryNames.add(query.getName());
            }

            reports = queryNames.toArray(new String[queryNames.size()]);
        }

        List<Graph> graphs = new ArrayList<Graph>(reports.length);

        for (String report : reports) {
            PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);
            graphs.add(new Graph(prefabGraph, resource, graphResults.getStart(), graphResults.getEnd()));
        }

        /*
         * Sort the graphs by their order in the properties file.
         * PrefabGraph implements the Comparable interface.
         */
        Collections.sort(graphs);

        rs.setGraphs(graphs);
        
        return rs;
    }

    public void afterPropertiesSet() {
        if (m_nodeDao == null) {
            throw new IllegalStateException("nodeDao property has not been set");
        }
        if (m_resourceDao == null) {
            throw new IllegalStateException("resourceDao property has not been set");
        }
        if (m_graphDao == null) {
            throw new IllegalStateException("graphDao property has not been set");
        }
    }

    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }
}
