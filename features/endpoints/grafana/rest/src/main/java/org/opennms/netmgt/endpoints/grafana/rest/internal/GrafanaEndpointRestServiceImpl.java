/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.endpoints.grafana.rest.internal;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpoint;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpointException;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaEndpointService;
import org.opennms.netmgt.endpoints.grafana.rest.GrafanaEndpointRestService;

import com.google.common.base.Strings;

public class GrafanaEndpointRestServiceImpl implements GrafanaEndpointRestService {

    private final GrafanaEndpointService grafanaEndpointService;

    public GrafanaEndpointRestServiceImpl(final GrafanaEndpointService grafanaEndpointService) {
        this.grafanaEndpointService = Objects.requireNonNull(grafanaEndpointService);
    }

    @Override
    public Response listEndpoints() {
        final List<GrafanaEndpoint> endpoints = grafanaEndpointService.findEndpoints();
        if (endpoints.isEmpty()) {
            return Response.noContent().build();
        }
        final JSONArray resultArray = new JSONArray();
        endpoints.forEach(s -> resultArray.put(new JSONObject(s)));
        return Response.ok().entity(resultArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response deleteAllEndpoints() {
        grafanaEndpointService.deleteAllEndpoints();
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @Override
    public Response verifyEndpoint(GrafanaEndpoint grafanaEndpoint) {
        GrafanaClient client;
        if (Strings.isNullOrEmpty(grafanaEndpoint.getUid())) {
            final GrafanaEndpoint endpointToUse = new GrafanaEndpoint();
            endpointToUse.setApiKey(grafanaEndpoint.getApiKey());
            endpointToUse.setUrl(grafanaEndpoint.getUrl());
            endpointToUse.setConnectTimeout(grafanaEndpoint.getConnectTimeout());
            endpointToUse.setReadTimeout(grafanaEndpoint.getReadTimeout());
            client = grafanaEndpointService.getClient(endpointToUse);
        } else {
            client = grafanaEndpointService.getClient(grafanaEndpoint.getUid());
        }
        try {
            client.getDashboards();
            return Response.ok().build();
        } catch (IOException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorObject(ex).toString()).build();
        }
    }

    @Override
    public Response getEndpoint(Long endpointId) {
        final GrafanaEndpoint grafanaEndpoint = grafanaEndpointService.getEndpointById(endpointId);
        return Response.ok().entity(new JSONObject(grafanaEndpoint).toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response updateEndpoint(final GrafanaEndpoint grafanaEndpoint) {
        try {
            grafanaEndpointService.updateEndpoint(grafanaEndpoint);
            return Response.accepted().build();
        } catch (GrafanaEndpointException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorObject(ex).toString()).build();
        }
    }

    @Override
    public Response createEndpoint(GrafanaEndpoint newGrafanaEndpoint) {
        try {
            grafanaEndpointService.saveEndpoint(newGrafanaEndpoint);
            return Response.accepted().build();
        } catch (GrafanaEndpointException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorObject(ex).toString()).build();
        }
    }

    @Override
    public Response deleteEndpoint(final Long endpointId) {
        try {
            grafanaEndpointService.deleteEndpoint(grafanaEndpointService.getEndpointById(endpointId));
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (GrafanaEndpointException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorObject(ex).toString()).build();
        }
    }

    @Override
    public Response listDashboards(final String uid) {
        try {
            final List<Dashboard> dashboards = grafanaEndpointService.getClient(uid).getDashboards();
            return Response.ok().entity(new JSONArray(dashboards).toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorObject(ex).toString()).build();
        }
    }

    private static JSONObject createErrorObject(Exception ex) {
        return createErrorObject(ex.getMessage(), "entity");
    }

    private static JSONObject createErrorObject(GrafanaEndpointException ex) {
        return createErrorObject(ex.getRawMessage(), ex.getContext());
    }

    private static JSONObject createErrorObject(String message, String context) {
        final JSONObject errorObject = new JSONObject()
                .put("message", message)
                .put("context", context);
        return errorObject;
    }
}
