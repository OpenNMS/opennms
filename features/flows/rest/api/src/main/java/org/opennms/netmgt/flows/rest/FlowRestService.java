/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.rest;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.flows.rest.model.FlowGraphUrlInfo;
import org.opennms.netmgt.flows.rest.model.FlowNodeDetails;
import org.opennms.netmgt.flows.rest.model.FlowNodeSummary;
import org.opennms.netmgt.flows.rest.model.FlowSeriesResponse;
import org.opennms.netmgt.flows.rest.model.FlowSummaryResponse;

@Path("flows")
public interface FlowRestService {

    String DEFAULT_STEP_MS = "300000"; // 5 minutes
    String DEFAULT_TOP_N = "10";
    String DEFAULT_LIMIT = "10";

    /**
     * Retrieves the number of flows persisted in the repository.
     *
     * Supports filtering.
     *
     * @param uriInfo JAX-RS context
     * @return number of flows that match the given query
     */
    @GET
    @Path("count")
    Long getFlowCount(@Context final UriInfo uriInfo);

    /**
     * Retrieves a summary of the nodes that have exported flows.
     *
     * Supports filtering.
     *
     * @return node summaries
     */
    @GET
    @Path("exporters")
    @Produces(MediaType.APPLICATION_JSON)
    List<FlowNodeSummary> getFlowExporters();

    /**
     * Retrieved detailed information about a specific node.
     *
     * Supports filtering.
     *
     * @param nodeId node id
     * @return node details
     */
    @GET
    @Path("exporters/{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    FlowNodeDetails getFlowExporter(@PathParam("nodeId") final Integer nodeId);

    @GET
    @Path("applications")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSummaryResponse getTopNApplications(
            @DefaultValue(DEFAULT_TOP_N) @QueryParam("N") final int N,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context UriInfo uriInfo
    );

    @GET
    @Path("applications/series")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSeriesResponse getTopNApplicationSeries(
            @DefaultValue(DEFAULT_STEP_MS) @QueryParam("step") final long step,
            @DefaultValue(DEFAULT_TOP_N) @QueryParam("N") final int N,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context final UriInfo uriInfo
    );

    @GET
    @Path("conversations")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSummaryResponse getTopNConversations(
            @DefaultValue(DEFAULT_TOP_N) @QueryParam("N") final int N,
            @Context final UriInfo uriInfo
    );

    @GET
    @Path("conversations/series")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSeriesResponse getTopNConversationsSeries(
            @DefaultValue(DEFAULT_STEP_MS) @QueryParam("step") final long step,
            @DefaultValue(DEFAULT_TOP_N) @QueryParam("N") final int N,
            @Context final UriInfo uriInfo
    );

    @GET
    @Path("flowGraphUrl")
    FlowGraphUrlInfo getFlowGraphUrlInfo(@Context final UriInfo uriInfo);

}
