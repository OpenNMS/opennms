
/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Jan 26: Modified findResults and createGraphResultSet - part of ksc performance improvement. - ayres@opennms.org
 * 2008 Oct 22: Use new ResourceDao methods. - dj@opennms.org
 * 2007 Apr 05: Add the graph offets to the model object. - dj@opennms.org
 * 
 * Created: November 12, 2006
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.GraphDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ResourceDao;
import org.opennms.netmgt.dao.RrdDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.graph.Graph;
import org.opennms.web.graph.GraphResults;
import org.opennms.web.graph.RelativeTimePeriod;
import org.opennms.web.graph.GraphResults.GraphResultSet;
import org.opennms.web.svclayer.GraphResultsService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DefaultGraphResultsService class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class DefaultGraphResultsService implements GraphResultsService, InitializingBean {

    private ResourceDao m_resourceDao;
    
    private GraphDao m_graphDao;

    private NodeDao m_nodeDao;
    
    private RrdDao m_rrdDao;
    
    private EventProxy m_eventProxy;

    private RelativeTimePeriod[] m_periods;

    /**
     * <p>Constructor for DefaultGraphResultsService.</p>
     */
    public DefaultGraphResultsService() {
        // Should this be injected, as well?
        m_periods = RelativeTimePeriod.getDefaultPeriods();
    }

    /** {@inheritDoc} */
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

        HashMap<String, List<OnmsResource>> resourcesMap = new HashMap<String, List<OnmsResource>>();
        
        for (String resourceId : resourceIds) {
            String[] values = parseResourceId(resourceId);
            if (values == null) {
                continue;
            }
            String parent = values[0];
            String childType = values[1];
            String childName = values[2];
            OnmsResource resource = null;
            if (!resourcesMap.containsKey(parent)) {
                List<OnmsResource> resourceList = m_resourceDao.getResourceListById(resourceId);
                if (resourceList == null) {
                    log().warn("findResults: zero child resources found for " + parent);
                } else {
                    resourcesMap.put(parent, resourceList);
                    log().debug("findResults: add resourceList to map for " + parent);
                }
            }
            for (OnmsResource r : resourcesMap.get(parent)) {
                if (childType.equals(r.getResourceType().getName())
                        && childName.equals(r.getName())) {
                    resource = r;
                    log().debug("findResults: found resource in map" + r.toString());
                    break;
                }
            }
            graphResults.addGraphResultSet(createGraphResultSet(resourceId, resource, reports, graphResults));
        }
        
        graphResults.setGraphTopOffsetWithText(m_rrdDao.getGraphTopOffsetWithText());
        graphResults.setGraphLeftOffset(m_rrdDao.getGraphLeftOffset());
        graphResults.setGraphRightOffset(m_rrdDao.getGraphRightOffset());
        
        return graphResults;
    }
    
    /**
     * <p>parseResourceId</p>
     *
     * @param resourceId a {@link java.lang.String} resource ID
     * @return an array of {@link java.lang.String} objects or null if the string is unparsable.
     */
    public static String[] parseResourceId(String resourceId) {
        try {
            String parent = resourceId.substring(0, resourceId.indexOf("]") + 1);
            String child = resourceId.substring(resourceId.indexOf("]") + 2);
            String childType = child.substring(0, child.indexOf("["));
            String childName = child.substring(child.indexOf("[") + 1, child.indexOf("]"));
            return new String[] { parent, childType, childName };
        } catch (Throwable e) {
            log().warn("Illegally formatted resourceId found in DefaultGraphResultsService: " + resourceId, e);
            return null;
        }
    }
    
    /**
     * <p>createGraphResultSet</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param reports an array of {@link java.lang.String} objects.
     * @param graphResults a {@link org.opennms.web.graph.GraphResults} object.
     * @return a {@link org.opennms.web.graph.GraphResults.GraphResultSet} object.
     */
    public GraphResultSet createGraphResultSet(String resourceId, OnmsResource resource, String[] reports, GraphResults graphResults) {
        if (resource == null) {
            resource = m_resourceDao.getResourceById(resourceId);
        }
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

        List<String> filesToPromote = new LinkedList<String>();
        for (String report : reports) {
            PrefabGraph prefabGraph = m_graphDao.getPrefabGraph(report);
            Graph graph = new Graph(prefabGraph, resource, graphResults.getStart(), graphResults.getEnd());
            getAttributeFiles(graph, filesToPromote);
            graphs.add(graph);
        }

        
        
        sendEvent(filesToPromote);
        
        
        /*
         * Sort the graphs by their order in the properties file.
         * PrefabGraph implements the Comparable interface.
         */
        Collections.sort(graphs);

        rs.setGraphs(graphs);
        
        return rs;
    }

    private void sendEvent(List<String> filesToPromote) {

        EventBuilder bldr = new EventBuilder(EventConstants.PROMOTE_QUEUE_DATA_UEI, "OpenNMS.Webapp");
        bldr.addParam(EventConstants.PARM_FILES_TO_PROMOTE, filesToPromote);

        try {
            m_eventProxy.send(bldr.getEvent());
        } catch (EventProxyException e) {
            log().warn("Unable to send promotion event to opennms daemon", e);
        }
        
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(DefaultGraphResultsService.class);
    }

    private void getAttributeFiles(Graph graph, List<String> filesToPromote) {
        
        Collection<RrdGraphAttribute> attrs = graph.getRequiredRrGraphdAttributes();
        for(RrdGraphAttribute rrdAttr : attrs) {
            filesToPromote.add(m_resourceDao.getRrdDirectory()+File.separator+rrdAttr.getRrdRelativePath());
        }
        
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.state(m_nodeDao != null, "nodeDao property has not been set");
        Assert.state(m_resourceDao != null, "resourceDao property has not been set");
        Assert.state(m_graphDao != null, "graphDao property has not been set");
        Assert.state(m_rrdDao != null, "rrdDao property has not been set");
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getGraphDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.GraphDao} object.
     */
    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    /**
     * <p>setGraphDao</p>
     *
     * @param graphDao a {@link org.opennms.netmgt.dao.GraphDao} object.
     */
    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    /**
     * <p>getRrdDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.RrdDao} object.
     */
    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    /**
     * <p>setRrdDao</p>
     *
     * @param rrdDao a {@link org.opennms.netmgt.dao.RrdDao} object.
     */
    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }
    
    /**
     * <p>setEventProxy</p>
     *
     * @param eventProxy a {@link org.opennms.netmgt.model.events.EventProxy} object.
     */
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }
}
