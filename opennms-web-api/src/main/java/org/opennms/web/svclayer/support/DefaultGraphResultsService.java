/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.svclayer.api.GraphResultsService;
import org.opennms.web.svclayer.model.Graph;
import org.opennms.web.svclayer.model.GraphResults;
import org.opennms.web.svclayer.model.GraphResults.GraphResultSet;
import org.opennms.web.svclayer.model.RelativeTimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>DefaultGraphResultsService class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultGraphResultsService implements GraphResultsService, InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultGraphResultsService.class);


    private static Logger logger = LoggerFactory.getLogger("OpenNMS.WEB." + DefaultGraphResultsService.class);

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

    @Override
    public GraphResults findResults(String[] resourceIds, String[] reports, long start, long end, String relativeTime) {
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
            LOG.debug("findResults: parent, childType, childName = {}, {}, {}", values[0], values[1], values[2]);
            OnmsResource resource = null;
            if (!resourcesMap.containsKey(parent)) {
                List<OnmsResource> resourceList = m_resourceDao.getResourceListById(resourceId);
                if (resourceList == null) {
                    LOG.warn("findResults: zero child resources found for {}", parent);
                } else {
                    resourcesMap.put(parent, resourceList);
                    LOG.debug("findResults: add resourceList to map for {}", parent);
                }
            }
            for (OnmsResource r : resourcesMap.get(parent)) {
                if (childType.equals(r.getResourceType().getName())
                        && childName.equals(r.getName())) {
                    resource = r;
                    LOG.debug("findResults: found resource in map{}", r.toString());
                    break;
                }
            }
            try {
                graphResults.addGraphResultSet(createGraphResultSet(resourceId, resource, reports, graphResults));
            } catch (IllegalArgumentException e) {
                LOG.warn(e.getMessage(), e);
                continue;
            }
        }

        graphResults.setGraphTopOffsetWithText(m_rrdDao.getGraphTopOffsetWithText());
        graphResults.setGraphLeftOffset(m_rrdDao.getGraphLeftOffset());
        graphResults.setGraphRightOffset(m_rrdDao.getGraphRightOffset());

        return graphResults;
    }

    @Override
    public PrefabGraph[] getAllPrefabGraphs(String resourceId) {
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        return m_graphDao.getPrefabGraphsForResource(resource);
    }

    /**
     * <p>parseResourceId</p>
     *
     * @param resourceId a {@link java.lang.String} resource ID
     * @return an array of {@link java.lang.String} objects or null if the
     * string is unparsable.
     */
    public static String[] parseResourceId(String resourceId) {
        try {
            String parent = resourceId.substring(0, resourceId.indexOf(']') + 1);
            String child = resourceId.substring(resourceId.indexOf(']') + 2);
            String childType = child.substring(0, child.indexOf('['));
            String childName = child.substring(child.indexOf('[') + 1, child.indexOf(']'));
            return new String[]{parent, childType, childName};
        } catch (Throwable e) {
            LOG.warn("Illegally formatted resourceId found in DefaultGraphResultsService: {}", resourceId, e);
            return null;
        }
    }

    /**
     * <p>createGraphResultSet</p>
     *
     * @param resourceId a {@link java.lang.String} object.
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @param reports an array of {@link java.lang.String} objects.
     * @param graphResults a {@link org.opennms.web.svclayer.model.GraphResults} object.
     * @return a {@link org.opennms.web.svclayer.model.GraphResults.GraphResultSet}
     * object.
     */
    private GraphResultSet createGraphResultSet(String resourceId, OnmsResource resource, String[] reports, GraphResults graphResults) throws IllegalArgumentException {
        if (resource == null) {
            resource = m_resourceDao.getResourceById(resourceId);
            if (resource == null) {
                throw new IllegalArgumentException("Could not find resource \"" + resourceId + "\"");
            }
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
         * Sort the graphs by their order in the properties file. PrefabGraph
         * implements the Comparable interface.
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
            LOG.warn("Unable to send promotion event to opennms daemon", e);
        }

    }

   

    private void getAttributeFiles(Graph graph, List<String> filesToPromote) {

        Collection<RrdGraphAttribute> attrs = graph.getRequiredRrGraphdAttributes();

        for(RrdGraphAttribute rrdAttr : attrs) {
            LOG.debug("getAttributeFiles: ResourceType, ParentResourceType = {}, {}", rrdAttr.getResource().getResourceType().getLabel(), rrdAttr.getResource().getParent().getResourceType().getLabel());
            if (rrdAttr.getResource().getParent().getResourceType().getLabel().equals("nodeSource")) {
                filesToPromote.add(m_resourceDao.getRrdDirectory()+File.separator+"foreignSource"+File.separator+rrdAttr.getRrdRelativePath());
            } else {
                filesToPromote.add(m_resourceDao.getRrdDirectory()+File.separator+rrdAttr.getRrdRelativePath());
            }
        }

    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.state(m_nodeDao != null, "nodeDao property has not been set");
        Assert.state(m_resourceDao != null, "resourceDao property has not been set");
        Assert.state(m_graphDao != null, "graphDao property has not been set");
        Assert.state(m_rrdDao != null, "rrdDao property has not been set");
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getGraphDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.GraphDao} object.
     */
    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    /**
     * <p>setGraphDao</p>
     *
     * @param graphDao a {@link org.opennms.netmgt.dao.api.GraphDao} object.
     */
    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }

    /**
     * <p>getRrdDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.RrdDao} object.
     */
    public RrdDao getRrdDao() {
        return m_rrdDao;
    }

    /**
     * <p>setRrdDao</p>
     *
     * @param rrdDao a {@link org.opennms.netmgt.dao.api.RrdDao} object.
     */
    public void setRrdDao(RrdDao rrdDao) {
        m_rrdDao = rrdDao;
    }

    /**
     * <p>setEventProxy</p>
     *
     * @param eventProxy a {@link org.opennms.netmgt.events.api.EventProxy}
     * object.
     */
    public void setEventProxy(EventProxy eventProxy) {
        m_eventProxy = eventProxy;
    }
    
}
