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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.config.GroupDao;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.opennms.netmgt.surveillance.views.SurveillanceView;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDao;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDataService;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDef;
import org.opennms.web.api.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Surveillance views: CRUD over the view definitions, per-user default-view
 * resolution, and the computed data behind the grid (cell status plus the
 * alarm / notification / node-RTC drill-downs). Replaces the Vaadin
 * surveillance view servlets, which had no REST facade.
 *
 * <p>Reads are open to any authenticated user. Writes require
 * {@code ROLE_ADMIN}, enforced here (the Vaadin config editor was admin-only;
 * the default {@code /api/v2/**} rules would also admit {@code ROLE_REST}).
 *
 * <p>Persistence is the generic JSON key-value store (via
 * {@link SurveillanceViewDao}), which manages its own connections, and the
 * data service runs its own transactions, so no transaction demarcation is
 * needed here.
 */
@Component
@Path("surveillance/views")
@Tag(name = "SurveillanceViews", description = "Surveillance views API")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SurveillanceViewRestService {

    /** Serializes the name-uniqueness check-then-write; see {@link #create}. */
    private static final Object WRITE_LOCK = new Object();

    @Autowired(required = false)
    private SurveillanceViewDao m_dao;

    @Autowired(required = false)
    private SurveillanceViewDataService m_dataService;

    /**
     * Group membership backs the name-convention view targeting (a view named
     * after one of the user's groups). Optional: without it, resolution still
     * covers username matches and the configured default.
     */
    @Autowired(required = false)
    private GroupDao m_groupDao;

    @GET
    public List<SurveillanceViewDTO> list() {
        final String defaultViewId = getDao().getDefaultViewId();
        final List<SurveillanceView> views = getDao().findAll();
        final List<SurveillanceViewDTO> dtos = new ArrayList<>(views.size());
        for (final SurveillanceView view : views) {
            dtos.add(toDto(view, defaultViewId));
        }
        return dtos;
    }

    @GET
    @Path("{id}")
    public SurveillanceViewDTO get(@PathParam("id") final String id) {
        return toDto(require(id), getDao().getDefaultViewId());
    }

    /**
     * The view for the calling user, by the same convention the Vaadin view
     * used: a view named like the username wins, then a view named like one of
     * the user's groups, then the configured default.
     */
    @GET
    @Path("default")
    public SurveillanceViewDTO getDefaultForUser(@Context final SecurityContext securityContext) {
        final String username = usernameOf(securityContext);

        SurveillanceView view = getDao().findByName(username);

        if (view == null && m_groupDao != null) {
            for (final Group group : m_groupDao.findGroupsForUser(username)) {
                view = getDao().findByName(group.getName());
                if (view != null) {
                    break;
                }
            }
        }

        if (view == null) {
            view = getDao().get(getDao().getDefaultViewId());
        }

        if (view == null) {
            throw webException(Response.Status.NOT_FOUND, "No surveillance view matches user '" + username + "' and no default view is configured");
        }
        return toDto(view, getDao().getDefaultViewId());
    }

    @POST
    public Response create(@Context final UriInfo uriInfo, @Context final SecurityContext securityContext, final SurveillanceViewDTO dto) {
        requireAdmin(securityContext);
        final SurveillanceView view = validate(dto);
        view.setOwner(usernameOf(securityContext));
        view.setCreated(new Date());

        final String id;
        // The store has no unique constraint on the name, so serialize the
        // check-then-save against concurrent writers in this JVM.
        synchronized (WRITE_LOCK) {
            if (getDao().findByName(view.getName()) != null) {
                throw webException(Response.Status.CONFLICT, "A view named '" + view.getName() + "' already exists");
            }
            id = getDao().save(view);
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(id).build()).build();
    }

    /** Full replace of the view's name, refresh interval, rows and columns. */
    @PUT
    @Path("{id}")
    public Response update(@Context final SecurityContext securityContext, @PathParam("id") final String id, final SurveillanceViewDTO dto) {
        requireAdmin(securityContext);
        final SurveillanceView existing = require(id);
        final SurveillanceView incoming = validate(dto);

        existing.setRefreshSeconds(incoming.getRefreshSeconds());
        existing.setRows(incoming.getRows());
        existing.setColumns(incoming.getColumns());
        existing.setLastModified(new Date());

        // Same name-uniqueness window as create: hold the lock across the
        // rename collision check and the persist.
        synchronized (WRITE_LOCK) {
            if (!incoming.getName().equals(existing.getName())) {
                final SurveillanceView collision = getDao().findByName(incoming.getName());
                if (collision != null && !collision.getId().equals(id)) {
                    throw webException(Response.Status.CONFLICT, "A view named '" + incoming.getName() + "' already exists");
                }
                existing.setName(incoming.getName());
            }
            getDao().update(existing);
        }
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@Context final SecurityContext securityContext, @PathParam("id") final String id) {
        requireAdmin(securityContext);
        getDao().delete(require(id));
        return Response.noContent().build();
    }

    @PUT
    @Path("default/{id}")
    public Response setDefault(@Context final SecurityContext securityContext, @PathParam("id") final String id) {
        requireAdmin(securityContext);
        require(id);
        getDao().setDefaultViewId(id);
        return Response.noContent().build();
    }

    @GET
    @Path("{id}/status")
    public SurveillanceViewStatusDTO status(@PathParam("id") final String id) {
        final SurveillanceView view = require(id);
        final SurveillanceStatus[][] cellStatus = compute(() -> getDataService().calculateCellStatus(view));

        final SurveillanceViewStatusDTO dto = new SurveillanceViewStatusDTO();
        dto.setViewId(view.getId());
        dto.setViewName(view.getName());
        dto.setRefreshSeconds(view.getRefreshSeconds() != null ? view.getRefreshSeconds() : SurveillanceView.DEFAULT_REFRESH_SECONDS);
        for (final SurveillanceViewDef row : view.getRows()) {
            dto.getRows().add(row.getLabel());
        }
        for (final SurveillanceViewDef column : view.getColumns()) {
            dto.getColumns().add(column.getLabel());
        }
        for (final SurveillanceStatus[] row : cellStatus) {
            final List<SurveillanceViewStatusDTO.Cell> cells = new ArrayList<>(row.length);
            for (final SurveillanceStatus status : row) {
                cells.add(new SurveillanceViewStatusDTO.Cell(status.getDownEntityCount(), status.getTotalEntityCount(), status.getStatus()));
            }
            dto.getCells().add(cells);
        }
        return dto;
    }

    @GET
    @Path("{id}/alarms")
    public List<SurveillanceViewDataService.SurveillanceAlarm> alarms(@PathParam("id") final String id,
            @QueryParam("row") final String rowLabel, @QueryParam("column") final String columnLabel) {
        final SurveillanceView view = require(id);
        return compute(() -> getDataService().getAlarmsForCategories(
                categoriesFor(view.getRows(), rowLabel, "row"),
                categoriesFor(view.getColumns(), columnLabel, "column")));
    }

    @GET
    @Path("{id}/notifications")
    public List<SurveillanceViewDataService.SurveillanceNotification> notifications(@PathParam("id") final String id,
            @QueryParam("row") final String rowLabel, @QueryParam("column") final String columnLabel) {
        final SurveillanceView view = require(id);
        return compute(() -> getDataService().getNotificationsForCategories(
                categoriesFor(view.getRows(), rowLabel, "row"),
                categoriesFor(view.getColumns(), columnLabel, "column")));
    }

    @GET
    @Path("{id}/rtc")
    public List<SurveillanceViewDataService.NodeRtc> rtc(@PathParam("id") final String id,
            @QueryParam("row") final String rowLabel, @QueryParam("column") final String columnLabel) {
        final SurveillanceView view = require(id);
        return compute(() -> getDataService().getNodeRtcsForCategories(
                categoriesFor(view.getRows(), rowLabel, "row"),
                categoriesFor(view.getColumns(), columnLabel, "column")));
    }

    /**
     * The category names behind a drill-down selection: a single row/column
     * def when a label is given, the union of all defs otherwise.
     */
    private static Set<String> categoriesFor(final List<SurveillanceViewDef> defs, final String label, final String axis) {
        final Set<String> categories = new LinkedHashSet<>();
        if (label == null || label.trim().isEmpty()) {
            for (final SurveillanceViewDef def : defs) {
                categories.addAll(def.getCategories());
            }
            return categories;
        }
        for (final SurveillanceViewDef def : defs) {
            if (label.equals(def.getLabel())) {
                categories.addAll(def.getCategories());
                return categories;
            }
        }
        throw webException(Response.Status.BAD_REQUEST, "The view has no " + axis + " labelled '" + label + "'");
    }

    /** Maps the data service's unknown-category complaints to a 400. */
    private static <T> T compute(final java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (final IllegalArgumentException e) {
            throw webException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Validates an incoming body against the same shape the legacy XSD
     * required (a name, and at least one row and column def, each with a label
     * and at least one category) and normalizes it into a domain view.
     */
    private static SurveillanceView validate(final SurveillanceViewDTO dto) {
        if (dto == null) {
            throw webException(Response.Status.BAD_REQUEST, "A view body is required");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw webException(Response.Status.BAD_REQUEST, "A view name is required");
        }
        if (dto.getRefreshSeconds() != null && dto.getRefreshSeconds() <= 0) {
            throw webException(Response.Status.BAD_REQUEST, "refreshSeconds must be positive");
        }

        final SurveillanceView view = new SurveillanceView();
        view.setName(dto.getName().trim());
        view.setRefreshSeconds(dto.getRefreshSeconds() != null ? dto.getRefreshSeconds() : SurveillanceView.DEFAULT_REFRESH_SECONDS);
        view.setRows(validateDefs(dto.getRows(), "row"));
        view.setColumns(validateDefs(dto.getColumns(), "column"));
        return view;
    }

    private static List<SurveillanceViewDef> validateDefs(final List<SurveillanceViewDTO.Def> dtos, final String axis) {
        if (dtos == null || dtos.isEmpty()) {
            throw webException(Response.Status.BAD_REQUEST, "At least one " + axis + " is required");
        }
        final List<SurveillanceViewDef> defs = new ArrayList<>(dtos.size());
        final Set<String> labels = new LinkedHashSet<>();
        for (final SurveillanceViewDTO.Def dto : dtos) {
            if (dto.getLabel() == null || dto.getLabel().trim().isEmpty()) {
                throw webException(Response.Status.BAD_REQUEST, "Every " + axis + " needs a label");
            }
            final String label = dto.getLabel().trim();
            if (!labels.add(label)) {
                throw webException(Response.Status.BAD_REQUEST, "Duplicate " + axis + " label '" + label + "'");
            }
            if (dto.getCategories() == null || dto.getCategories().isEmpty()) {
                throw webException(Response.Status.BAD_REQUEST, "The " + axis + " '" + label + "' needs at least one category");
            }
            final SurveillanceViewDef def = new SurveillanceViewDef();
            def.setLabel(label);
            for (final String category : dto.getCategories()) {
                if (category == null || category.trim().isEmpty()) {
                    throw webException(Response.Status.BAD_REQUEST, "The " + axis + " '" + label + "' has an empty category name");
                }
                def.getCategories().add(category.trim());
            }
            def.setReportCategory(dto.getReportCategory());
            defs.add(def);
        }
        return defs;
    }

    private SurveillanceView require(final String id) {
        final SurveillanceView view = getDao().get(id);
        if (view == null) {
            throw webException(Response.Status.NOT_FOUND, "No surveillance view with id " + id);
        }
        return view;
    }

    private SurveillanceViewDTO toDto(final SurveillanceView view, final String defaultViewId) {
        final SurveillanceViewDTO dto = new SurveillanceViewDTO();
        dto.setId(view.getId());
        dto.setName(view.getName());
        dto.setRefreshSeconds(view.getRefreshSeconds() != null ? view.getRefreshSeconds() : SurveillanceView.DEFAULT_REFRESH_SECONDS);
        dto.setIsDefault(view.getId() != null && view.getId().equals(defaultViewId));
        for (final SurveillanceViewDef def : view.getRows()) {
            dto.getRows().add(toDefDto(def));
        }
        for (final SurveillanceViewDef def : view.getColumns()) {
            dto.getColumns().add(toDefDto(def));
        }
        dto.setOwner(view.getOwner());
        dto.setCreated(view.getCreated());
        dto.setLastModified(view.getLastModified());
        return dto;
    }

    private static SurveillanceViewDTO.Def toDefDto(final SurveillanceViewDef def) {
        final SurveillanceViewDTO.Def dto = new SurveillanceViewDTO.Def();
        dto.setLabel(def.getLabel());
        dto.setCategories(new ArrayList<>(def.getCategories()));
        dto.setReportCategory(def.getReportCategory());
        return dto;
    }

    private static void requireAdmin(final SecurityContext securityContext) {
        if (securityContext == null || !securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            throw webException(Response.Status.FORBIDDEN, "ROLE_ADMIN is required to change surveillance views");
        }
    }

    /**
     * A missing principal means the security layer didn't run; reject rather
     * than resolve or persist anything unattributed.
     */
    private static String usernameOf(final SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw webException(Response.Status.UNAUTHORIZED, "An authenticated user is required");
        }
        return securityContext.getUserPrincipal().getName();
    }

    private SurveillanceViewDao getDao() {
        if (m_dao == null) {
            throw webException(Response.Status.SERVICE_UNAVAILABLE, "Surveillance view persistence is not available");
        }
        return m_dao;
    }

    private SurveillanceViewDataService getDataService() {
        if (m_dataService == null) {
            throw webException(Response.Status.SERVICE_UNAVAILABLE, "The surveillance view data service is not available");
        }
        return m_dataService;
    }

    private static WebApplicationException webException(final Response.Status status, final String message) {
        return new WebApplicationException(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
    }
}
