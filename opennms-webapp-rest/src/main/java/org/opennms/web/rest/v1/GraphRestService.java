/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.resource.ResourceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Read-only API for retrieving graph definitions and determining
 * the list of supported graphs on a particular resource.
 *
 * @author jwhite
 */
@Component("graphRestService")
@Path("graphs")
public class GraphRestService extends OnmsRestService {

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private GraphDao m_graphDao;

    @Autowired
    private ResourceDao m_resourceDao;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public GraphNameCollection getGraphNames() {
        List<String> graphNames = Lists.newLinkedList();
        for (PrefabGraph prefabGraph : m_graphDao.getAllPrefabGraphs()) {
            graphNames.add(prefabGraph.getName());
        }

        Collections.sort(graphNames);
        return new GraphNameCollection(graphNames);
    }

    @GET
    @Path("{graphName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public PrefabGraph getGraphByName(@PathParam("graphName") final String graphName) {
        try {
            return m_graphDao.getPrefabGraph(graphName);
        } catch (ObjectRetrievalFailureException e) {
            throw getException(Status.NOT_FOUND, "No graph with name '{}' found.", graphName);
        }
    }

    @GET
    @Path("for/{resourceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public GraphNameCollection getGraphNamesForResource(@PathParam("resourceId") final String resourceId) {
        OnmsResource resource = m_resourceDao.getResourceById(resourceId);
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource with id '{}' found.", resourceId);
        }
        return getGraphNamesForResource(resource);
    }

    private GraphNameCollection getGraphNamesForResource(final OnmsResource resource) {
        List<String> graphNames = Lists.newLinkedList();
        for (PrefabGraph prefabGraph : m_graphDao.getPrefabGraphsForResource(resource)) {
            graphNames.add(prefabGraph.getName());
        }

        Collections.sort(graphNames);
        return new GraphNameCollection(graphNames);
    }

    @GET
    @Path("fornode/{nodeCriteria}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional(readOnly=true)
    public GraphResourceDTO getGraphResourcesForNode(@PathParam("nodeCriteria") final String nodeCriteria, @DefaultValue("-1") @QueryParam("depth") final int depth) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.NOT_FOUND, "No node found with criteria '{}'.", nodeCriteria);
        }

        OnmsResource resource = m_resourceDao.getResourceForNode(node);
        if (resource == null) {
            throw getException(Status.NOT_FOUND, "No resource found for node with id {}.", "" + node.getId());
        }

        final ResourceVisitor visitor = new ResourceVisitor(this);
        final ResourceDTO resourceDTO = ResourceDTO.fromResource(resource, depth);
        visitor.visit(resourceDTO);

        return new GraphResourceDTO(resourceDTO, visitor.getGraphs());
    }

    @XmlRootElement(name = "graph-resource")
    @XmlAccessorType(XmlAccessType.NONE)
    public static final class GraphResourceDTO {
        @XmlElement(name="resource")
        private ResourceDTO m_resource;

        @XmlElement(name="prefab-graphs")
        private PrefabGraphCollection m_prefabGraphs;

        @SuppressWarnings("unused")
        private GraphResourceDTO() {
            super();
        }

        public GraphResourceDTO(final ResourceDTO resource, final PrefabGraphCollection graphs) {
            m_resource = resource;
            m_prefabGraphs = graphs;
        }
    }

    public static final class ResourceVisitor {
        private final Map<String,PrefabGraph> m_graphs = Maps.newLinkedHashMap();
        private final GraphRestService m_service;

        public ResourceVisitor(GraphRestService service) {
            m_service = service;
        }

        public void visit(final ResourceDTO resource) {
            // first, decorate the DTO with the list of graph names
            final GraphNameCollection graphNames = m_service.getGraphNamesForResource(resource.getResource());
            resource.setGraphNames(graphNames.getObjects());

            // then, get the prefab graphs for these graph names if we don't have them already
            for (final String graphName : graphNames) {
                if (!m_graphs.containsKey(graphName)) {
                    m_graphs.put(graphName, m_service.getGraphByName(graphName));
                }
            }

            // finally, recurse if necessary
            if (resource.getChildren() != null) {
                for (final ResourceDTO r : resource.getChildren()) {
                    this.visit(r);
                }
            }
        }

        public PrefabGraphCollection getGraphs() {
            return new PrefabGraphCollection(m_graphs.values());
        }
    }
}
