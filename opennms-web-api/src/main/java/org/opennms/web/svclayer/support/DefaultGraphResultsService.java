/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
import java.util.Optional;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.RrdDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourceTypeUtils;
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

import com.google.common.base.Strings;
import com.google.gson.Gson;

/**
 * <p>DefaultGraphResultsService class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DefaultGraphResultsService implements GraphResultsService, InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultGraphResultsService.class);

	public static final String RESOURCE_IDS_CONTEXT = "resourceIds";

    private ResourceDao m_resourceDao;

    private GraphDao m_graphDao;

    private NodeDao m_nodeDao;

    private RrdDao m_rrdDao;

    private EventProxy m_eventProxy;

    private JsonStore m_jsonStore;

    private final Gson m_gson = new Gson();

    private RelativeTimePeriod[] m_periods;

    /**
     * <p>Constructor for DefaultGraphResultsService.</p>
     */
    public DefaultGraphResultsService() {
        // Should this be injected, as well?
        m_periods = RelativeTimePeriod.getDefaultPeriods();
    }

    @Override
    public GraphResults findResults(ResourceId[] resourceIds, String[] reports, String generatedId, String nodeCriteria, long start, long end, String relativeTime) {
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
        graphResults.setNodeCriteria(nodeCriteria);
        graphResults.setGeneratedId(generatedId);

        HashMap<ResourceId, List<OnmsResource>> resourcesMap = new HashMap<>();
        for (ResourceId resourceId : resourceIds) {
            LOG.debug("findResults: parent, childType, childName = {}, {}, {}", resourceId.parent, resourceId.type, resourceId.name);
            OnmsResource resource = null;
            if (!resourcesMap.containsKey(resourceId.parent)) {
                List<OnmsResource> resourceList = m_resourceDao.getResourceById(resourceId).getChildResources();
                if (resourceList == null) {
                    LOG.warn("findResults: zero child resources found for {}", resourceId.parent);
                } else {
                    resourcesMap.put(resourceId.parent, resourceList);
                    LOG.debug("findResults: add resourceList to map for {}", resourceId.parent);
                }
            }
            for (OnmsResource r : resourcesMap.get(resourceId.parent)) {
                if (resourceId.type.equals(r.getResourceType().getName())
                        && resourceId.name.equals(r.getName())) {
                    resource = r;
                    LOG.debug("findResults: found resource in map{}", r.toString());
                    break;
                }
            }
            try {
                graphResults.addGraphResultSet(createGraphResultSet(resourceId, resource, reports, graphResults));
            } catch (IllegalArgumentException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        // GraphAll case where all resources are fetched from node.
        if (!Strings.isNullOrEmpty(nodeCriteria)) {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node != null) {
                OnmsResource nodeResource = m_resourceDao.getResourceForNode(node);
                if (nodeResource != null) {
                    List<OnmsResource> childResources = nodeResource.getChildResources();
                    for (OnmsResource resource : childResources) {
                        try {
                            graphResults.addGraphResultSet(createGraphResultSet(null, resource, reports, graphResults));
                        } catch (IllegalArgumentException e) {
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                }
            }
        }
        // GraphSelected case where all resources are fetched from generatedId
        if (!Strings.isNullOrEmpty(generatedId)) {
            Optional<String> result = m_jsonStore.get(generatedId, RESOURCE_IDS_CONTEXT);
            if (result.isPresent()) {
                try {
                    String[] resourceArray = m_gson.fromJson(result.get(), String[].class);
                    for (String resourceId : resourceArray) {
                        try {
                            OnmsResource resource = m_resourceDao.getResourceById(ResourceId.fromString(resourceId));
                            if(resource != null) {
                                graphResults.addGraphResultSet(createGraphResultSet(null, resource, reports, graphResults));
                            }
                        } catch (IllegalArgumentException e) {
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    LOG.warn("Exception while parsing json string {}", result.get());
                }
            }
        }



        graphResults.setGraphTopOffsetWithText(m_rrdDao.getGraphTopOffsetWithText());
        graphResults.setGraphLeftOffset(m_rrdDao.getGraphLeftOffset());
        graphResults.setGraphRightOffset(m_rrdDao.getGraphRightOffset());

        return graphResults;
    }



    @Override
    public PrefabGraph[] getAllPrefabGraphs(ResourceId resourceId) {
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        return m_graphDao.getPrefabGraphsForResource(resource);
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
    private GraphResultSet createGraphResultSet(ResourceId resourceId, OnmsResource resource, String[] reports, GraphResults graphResults) throws IllegalArgumentException {
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

        List<String> filesToPromote = new LinkedList<>();
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

        final String rrdBaseDir = System.getProperty("rrd.base.dir");
        for(RrdGraphAttribute rrdAttr : attrs) {
            LOG.debug("getAttributeFiles: ResourceType, ParentResourceType = {}, {}", rrdAttr.getResource().getResourceType().getLabel(), rrdAttr.getResource().getParent().getResourceType().getLabel());
            if (rrdAttr.getResource().getParent().getResourceType().getLabel().equals("nodeSource")) {
                filesToPromote.add(rrdBaseDir+File.separator+ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY+File.separator+rrdAttr.getRrdRelativePath());
            } else {
                filesToPromote.add(rrdBaseDir+File.separator+rrdAttr.getRrdRelativePath());
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
        Assert.state(m_jsonStore != null, "jsonStore property has not been set");
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


    /**
     * <p>setJsonStore</p>
     *
     * @param jsonStore a @{@link JsonStore} object.
     */
    public void setJsonStore(JsonStore jsonStore) {
        m_jsonStore = jsonStore;
    }
}
