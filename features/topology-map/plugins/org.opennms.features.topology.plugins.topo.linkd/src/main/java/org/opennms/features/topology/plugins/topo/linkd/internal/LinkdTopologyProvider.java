/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;


import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.WrappedGraph;
import org.opennms.features.topology.api.topo.WrappedVertex;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * This class is no longer used in favor of the EnhancedLinkdTopologyProvider
 */
public class LinkdTopologyProvider extends AbstractLinkdTopologyProvider {
	
	private static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProvider.class);

    public static final String GROUP_ICON_KEY = "linkd:group";
    public static final String SERVER_ICON_KEY = "linkd:system";

    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    /**
     * Used as an init-method in the OSGi blueprint
     * @throws JAXBException 
     * @throws MalformedURLException 
     */
    public void onInit() throws MalformedURLException, JAXBException {
        LOG.debug("init: loading topology.");
        load(null);
    }
    
    public LinkdTopologyProvider() { }

    @Override
    public void refresh() {
        try {
            load(null);
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        } catch (JAXBException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void load(String filename) throws MalformedURLException, JAXBException {
        if (filename != null) {
            LOG.warn("Filename that was specified for linkd topology will be ignored: " + filename + ", using " + getConfigurationFile() + " instead");
        }
        LOG.debug("loadtopology: resetContainer ");
        resetContainer();

        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            LOG.debug("loadtopology: parsing link: " + link.getDataLinkInterfaceId());

            OnmsNode node = getNodeDao().get(link.getNode().getId());
            LOG.debug("loadtopology: found source node: " + node.getLabel());
            String sourceId = node.getNodeId();
            Vertex source = getVertex(getVertexNamespace(), sourceId);
            if (source == null) {
                LOG.debug("loadtopology: adding source node as vertex: " + node.getLabel());
                source = getVertex(node);
                addVertices(source);
            }

            OnmsNode parentNode = getNodeDao().get(link.getNodeParentId());
            LOG.debug("loadtopology: found target node: " + parentNode.getLabel());
            String targetId = parentNode.getNodeId();
            Vertex target = getVertex(getVertexNamespace(), targetId);
            if (target == null) {
                LOG.debug("loadtopology: adding target as vertex: " + parentNode.getLabel());
                target = getVertex(parentNode);
                addVertices(target);
            }
            
            // Create a new edge that connects the vertices
            // TODO: Make sure that all properties are set on this object
            AbstractEdge edge = connectVertices(link.getDataLinkInterfaceId(), source, target, getEdgeNamespace());
            edge.setTooltipText(getEdgeTooltipText(link, source, target));
        }
        
        LOG.debug("loadtopology: adding nodes without links: " + isAddNodeWithoutLink());
        if (isAddNodeWithoutLink()) {

            List<OnmsNode> allNodes;
            allNodes = getAllNodesNoACL();

            for (OnmsNode onmsnode: allNodes) {
                String nodeId = onmsnode.getNodeId();
                if (getVertex(getVertexNamespace(), nodeId) == null) {
                    LOG.debug("loadtopology: adding link-less node: " + onmsnode.getLabel());
                    addVertices(getVertex(onmsnode));
                }
            }




        }
        
        File configFile = new File(getConfigurationFile());
        if (configFile.exists() && configFile.canRead()) {
            LOG.debug("loadtopology: loading topology from configuration file: " + getConfigurationFile());
            WrappedGraph graph = getGraphFromFile(configFile);

            // Add all groups to the topology
            for (WrappedVertex eachVertexInFile: graph.m_vertices) {
                if (eachVertexInFile.group) {
                    LOG.debug("loadtopology: adding group to topology: " + eachVertexInFile.id);
                    if (eachVertexInFile.namespace == null) {
                        eachVertexInFile.namespace = getVertexNamespace();
                        LoggerFactory.getLogger(this.getClass()).warn("Setting namespace on vertex to default: {}", eachVertexInFile);
                    } 
                    if (eachVertexInFile.id == null) {
                        LoggerFactory.getLogger(this.getClass()).warn("Invalid vertex unmarshalled from {}: {}", getConfigurationFile(), eachVertexInFile);
                    }
                    AbstractVertex newGroupVertex = addGroup(eachVertexInFile.id, eachVertexInFile.iconKey, eachVertexInFile.label);
                    newGroupVertex.setIpAddress(eachVertexInFile.ipAddr);
                    newGroupVertex.setLocked(eachVertexInFile.locked);
                    if (eachVertexInFile.nodeID != null) newGroupVertex.setNodeID(eachVertexInFile.nodeID);
                    if (!newGroupVertex.equals(eachVertexInFile.parent)) newGroupVertex.setParent(eachVertexInFile.parent);
                    newGroupVertex.setSelected(eachVertexInFile.selected);
                    newGroupVertex.setStyleName(eachVertexInFile.styleName);
                    newGroupVertex.setTooltipText(eachVertexInFile.tooltipText);
                    if (eachVertexInFile.x != null) newGroupVertex.setX(eachVertexInFile.x);
                    if (eachVertexInFile.y != null) newGroupVertex.setY(eachVertexInFile.y);
                }
            }
            for (Vertex vertex: getVertices()) {
                if (vertex.getParent() != null && !vertex.equals(vertex.getParent())) {
                    LOG.debug("loadtopology: setting parent of " + vertex + " to " + vertex.getParent());
                    setParent(vertex, vertex.getParent());
                }
            }
            // Add all children to the specific group
            // Attention: We ignore all other attributes, they do not need to be merged!
            for (WrappedVertex eachVertexInFile : graph.m_vertices) {
                if (!eachVertexInFile.group && eachVertexInFile.parent != null) {
                    final Vertex child = getVertex(eachVertexInFile);
                    final Vertex parent = getVertex(eachVertexInFile.parent);
                    if (child == null || parent == null) continue;
                    LOG.debug("loadtopology: setting parent of " + child + " to " + parent);
                    if (!child.equals(parent)) setParent(child, parent);
                }
            }
        } else {
            LOG.debug("loadtopology: could not load topology configFile:" + getConfigurationFile());
        }
        LOG.debug("Found " + getGroups().size() + " groups");        
        LOG.debug("Found " + getVerticesWithoutGroups().size() + " vertices");
        LOG.debug("Found " + getEdges().size() + " edges");
    }

    @Override
    public String getSearchProviderNamespace() {
        return TOPOLOGY_NAMESPACE_LINKD;
    }


    //@Override
    public List<SearchResult> slowQuery(SearchQuery searchQuery, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->query: called with search query: '{}'", searchQuery);
        List<SearchResult> searchResults = Lists.newArrayList();
        
        CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        String ilike = "%"+searchQuery.getQueryString()+"%";  //check this for performance reasons
//        cb.alias("assetRecord", "asset").match("any").ilike("label", ilike).ilike("sysDescription", ilike).ilike("asset.comment", ilike);
        cb.match("any").ilike("label", ilike).ilike("sysDescription", ilike);
        List<OnmsNode> nodes = getNodeDao().findMatching(cb.toCriteria());
        
        if (nodes.size() == 0) {
            return searchResults;
        }
        
        for (OnmsNode node : nodes) {
            searchResults.add(createSearchResult(node, searchQuery.getQueryString()));
        }
        
        return searchResults;
    }

    private SearchResult createSearchResult(OnmsNode node, String queryString) {
    	return new SearchResult("node", node.getId().toString(), node.getLabel(), queryString);
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
    	//LOG.debug("SearchProvider->query: called with search query: '{}'", searchQuery);
    	
        List<Vertex> vertices = getFilteredVertices();
        List<SearchResult> searchResults = Lists.newArrayList();

        for(Vertex vertex : vertices){
            if(searchQuery.matches(vertex.getLabel())) {
                searchResults.add(new SearchResult(vertex));
            }
        }
        
        //LOG.debug("SearchProvider->query: found {} search results.", searchResults.size());
        return searchResults;
    }

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        LOG.debug("SearchProvider->onFocusSearchResult: called with search result: '{}'", searchResult);
    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        LOG.debug("SearchProvider->onDefocusSearchResult: called with search result: '{}'", searchResult);
    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return AbstractSearchProvider.supportsPrefix("nodes=", searchPrefix);
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->getVertexRefsBy: called with search result: '{}'", searchResult);
        org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);
        
        Set<VertexRef> vertices = ((VertexHopCriteria)criterion).getVertices();
        LOG.debug("SearchProvider->getVertexRefsBy: found '{}' vertices.", vertices.size());
        
        return vertices;
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        Criteria criterion = findCriterion(searchResult.getId(), container);

        if (criterion != null) {
            LOG.debug("SearchProvider->removeVertexHopCriteria: found criterion: {} for searchResult {}.", criterion, searchResult);
            container.removeCriteria(criterion);
        } else {
            LOG.debug("SearchProvider->removeVertexHopCriteria: did not find criterion for searchResult {}.", searchResult);
        }
        
        logCriteriaInContainer(container);
    }
    
    private org.opennms.features.topology.api.topo.Criteria findCriterion(String resultId, GraphContainer container) {
        
        org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
        for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
            if (criterion instanceof LinkdHopCriteria ) {
                
                String id = ((LinkdHopCriteria) criterion).getId();
                
                if (id.equals(resultId)) {
                    return criterion;
                }
            }
            
            if (criterion instanceof FocusNodeHopCriteria) {
                String id = ((FocusNodeHopCriteria)criterion).getId();
                
                if (id.equals(resultId)) {
                    return criterion;
                }
            }
            
        }
        return null;
    }


    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onCenterSearchResult: called with search result: '{}'", searchResult);
    }

    @Override
    public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onToggleCollapse: called with search result: '{}'", searchResult);
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);
        
        VertexHopCriteria criterion = LinkdHopCriteriaFactory.createCriteria(searchResult.getId(), searchResult.getLabel());
        container.addCriteria(criterion);
        
        LOG.debug("SearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        
        logCriteriaInContainer(container);
    }

    private void logCriteriaInContainer(GraphContainer container) {
        Criteria[] criteria = container.getCriteria();
        LOG.debug("SearchProvider->addVertexHopCriteria: there are now {} criteria in the GraphContainer.", criteria.length);
        for (Criteria crit : criteria) {
            LOG.debug("SearchProvider->addVertexHopCriteria: criterion: '{}' is in the GraphContainer.", crit);
        }
    }


}
