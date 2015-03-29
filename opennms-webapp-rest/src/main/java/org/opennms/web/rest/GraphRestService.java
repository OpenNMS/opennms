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

package org.opennms.web.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Read-only API for retrieving graph definitions and determining
 * the list of supported graphs on a particular resource.
 *
 * @author jwhite
 */
@Component
@Scope("prototype")
@Path("graphs")
public class GraphRestService extends OnmsRestService {

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

        List<String> graphNames = Lists.newLinkedList();
        for (PrefabGraph prefabGraph : m_graphDao.getPrefabGraphsForResource(resource)) {
            graphNames.add(prefabGraph.getName());
        }

        Collections.sort(graphNames);
        return new GraphNameCollection(graphNames);
    }

    @XmlRootElement(name = "names")
    public static final class GraphNameCollection extends JaxbListWrapper<String> {

        private static final long serialVersionUID = 1L;

        public GraphNameCollection() {
            super();
        }

        public GraphNameCollection(Collection<? extends String> names) {
            super(names);
        }

        @XmlElement(name = "name")
        public List<String> getObjects() {
            return super.getObjects();
        }
    }
}
