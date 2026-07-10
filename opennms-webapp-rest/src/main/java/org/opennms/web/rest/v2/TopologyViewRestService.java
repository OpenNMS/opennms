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
package org.opennms.web.rest.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.netmgt.topology.views.TopologyView;
import org.opennms.netmgt.topology.views.TopologyViewDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * CRUD over the shared catalog of custom (user-composed) topology views.
 *
 * <p>Reads are open to any authenticated user; writes (POST/PUT/DELETE)
 * require {@code ROLE_REST} or {@code ROLE_ADMIN}. This is enforced by the
 * default {@code /api/v2/**} rules in the Spring Security configuration, so
 * the resource itself carries no role annotations.
 *
 * <p>The canvas {@code definition} travels as nested JSON (see
 * {@link TopologyViewDTO}); this resource converts it to/from the opaque
 * string stored on the {@link TopologyView}. Persistence is the generic JSON
 * key-value store (via {@link TopologyViewDao}), which manages its own
 * connections, so no transaction demarcation is needed here.
 */
@Component
@Path("topology/views")
@Tag(name = "TopologyViews", description = "Custom topology views API")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopologyViewRestService {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyViewRestService.class);

    /** Serializes the name-uniqueness check-then-write; see {@link #create}. */
    private static final Object WRITE_LOCK = new Object();

    @Autowired(required = false)
    private TopologyViewDao m_dao;

    private final ObjectMapper m_mapper = new ObjectMapper();

    @GET
    public List<TopologyViewDTO> list() {
        final List<TopologyView> views = getDao().findAll();
        final List<TopologyViewDTO> dtos = new ArrayList<>(views.size());
        for (final TopologyView view : views) {
            dtos.add(toDto(view));
        }
        return dtos;
    }

    @GET
    @Path("{id}")
    public TopologyViewDTO get(@PathParam("id") final String id) {
        return toDto(require(id));
    }

    @POST
    public Response create(@Context final UriInfo uriInfo, @Context final SecurityContext securityContext, final TopologyViewDTO dto) {
        if (dto == null || dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw webException(Response.Status.BAD_REQUEST, "A view name is required");
        }
        final String name = dto.getName().trim();
        if (dto.getDefinition() == null) {
            throw webException(Response.Status.BAD_REQUEST, "A view definition is required");
        }

        final TopologyView view = new TopologyView();
        view.setName(name);
        view.setDefinition(writeDefinition(dto.getDefinition()));
        view.setOwner(ownerOf(securityContext));
        view.setCreated(new Date());

        final String id;
        // The store has no unique constraint on the name, so serialize the
        // check-then-save against concurrent writers in this JVM.
        synchronized (WRITE_LOCK) {
            if (getDao().findByName(name) != null) {
                throw webException(Response.Status.CONFLICT, "A view named '" + name + "' already exists");
            }
            id = getDao().save(view);
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(id)).build()).build();
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") final String id, final TopologyViewDTO dto) {
        final TopologyView view = require(id);
        if (dto == null) {
            throw webException(Response.Status.BAD_REQUEST, "A view body is required");
        }

        if (dto.getDefinition() != null) {
            view.setDefinition(writeDefinition(dto.getDefinition()));
        }
        view.setLastModified(new Date());

        // Same name-uniqueness window as create: hold the lock across the
        // rename collision check and the persist.
        synchronized (WRITE_LOCK) {
            if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
                final String name = dto.getName().trim(); // same normalization as create
                if (!name.equals(view.getName())) {
                    final TopologyView collision = getDao().findByName(name);
                    if (collision != null && !collision.getId().equals(id)) {
                        throw webException(Response.Status.CONFLICT, "A view named '" + name + "' already exists");
                    }
                    view.setName(name);
                }
            }
            getDao().update(view);
        }
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") final String id) {
        getDao().delete(require(id));
        return Response.noContent().build();
    }

    private TopologyView require(final String id) {
        final TopologyView view = getDao().get(id);
        if (view == null) {
            throw webException(Response.Status.NOT_FOUND, "No topology view with id " + id);
        }
        return view;
    }

    private TopologyViewDTO toDto(final TopologyView view) {
        final TopologyViewDTO dto = new TopologyViewDTO();
        dto.setId(view.getId());
        dto.setName(view.getName());
        dto.setDefinition(readDefinition(view.getDefinition()));
        dto.setOwner(view.getOwner());
        dto.setCreated(view.getCreated());
        dto.setLastModified(view.getLastModified());
        return dto;
    }

    private String writeDefinition(final JsonNode definition) {
        try {
            return m_mapper.writeValueAsString(definition);
        } catch (final IOException e) {
            throw webException(Response.Status.BAD_REQUEST, "Could not serialize view definition: " + e.getMessage());
        }
    }

    private JsonNode readDefinition(final String definition) {
        if (definition == null || definition.isEmpty()) {
            return null;
        }
        try {
            return m_mapper.readTree(definition);
        } catch (final IOException e) {
            // Stored value is not valid JSON; surface it rather than fail the whole list.
            LOG.warn("Stored topology view definition is not valid JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * The owner is always the authenticated principal -- the request body's
     * owner field is never trusted. A missing principal means the security
     * layer didn't run; reject rather than persist an unattributed view.
     */
    private static String ownerOf(final SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw webException(Response.Status.UNAUTHORIZED, "An authenticated user is required");
        }
        return securityContext.getUserPrincipal().getName();
    }

    private TopologyViewDao getDao() {
        if (m_dao == null) {
            throw webException(Response.Status.SERVICE_UNAVAILABLE, "Topology view persistence is not available");
        }
        return m_dao;
    }

    private static javax.ws.rs.WebApplicationException webException(final Response.Status status, final String message) {
        return new javax.ws.rs.WebApplicationException(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
