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
package org.opennms.netmgt.flows.rest;

import java.util.List;
import java.util.Set;

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
    @Path("dscp/enumerate")
    @Produces(MediaType.APPLICATION_JSON)
    List<Integer> getDscpValues(
            @Context UriInfo uriInfo
    );

    @GET
    @Path("dscp")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSummaryResponse getDscpSummaries(
            @Context UriInfo uriInfo
    );

    @GET
    @Path("dscp/series")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSeriesResponse getDscpSeries(
            @DefaultValue(DEFAULT_STEP_MS) @QueryParam("step") final long step,
            @Context UriInfo uriInfo
    );

    /**
     * Retrieve the list of applications.
     *
     * Supports filtering.
     *
     * @param matchingPrefix a string prefix that can be used to further filter the results
     * @param limit the maximum number of applications to return
     * @return the list of applications
     */
    @GET
    @Path("applications/enumerate")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getApplications(@DefaultValue("") @QueryParam("prefix") final String matchingPrefix,
                                 @DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") final long limit,
                                 @Context UriInfo uriInfo);

    @GET
    @Path("applications")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSummaryResponse getApplicationSummary(
            @QueryParam("N") final Integer N,
            @QueryParam("application") final Set<String> applications,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context UriInfo uriInfo
    );

    @GET
    @Path("applications/series")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSeriesResponse getApplicationSeries(
            @DefaultValue(DEFAULT_STEP_MS) @QueryParam("step") final long step,
            @QueryParam("N") final Integer N,
            @QueryParam("application") final Set<String> applications,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context final UriInfo uriInfo
    );

    /**
     * Retrieve the list of hosts.
     *
     * Supports filtering.
     *
     * @param prefix a string prefix that can be used to further filter the results
     * @param limit the maximum number of hosts to return
     * @return the list of hosts
     */
    @GET
    @Path("hosts/enumerate")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getHosts(@DefaultValue(".*") @QueryParam("pattern") final String regex,
                          @DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") final long limit,
                          @Context UriInfo uriInfo);

    @GET
    @Path("hosts")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSummaryResponse getHostSummary(
            @QueryParam("N") final Integer N,
            @QueryParam("host") final Set<String> hosts,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context UriInfo uriInfo
    );

    @GET
    @Path("hosts/series")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSeriesResponse getHostSeries(
            @DefaultValue(DEFAULT_STEP_MS) @QueryParam("step") final long step,
            @QueryParam("N") final Integer N,
            @QueryParam("host") final Set<String> hosts,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context final UriInfo uriInfo
    );

    /**
     * Retrieve the list of conversations.
     *
     * Supports filtering.
     * 
     * @param locationPattern the regex pattern for the location field
     * @param protocolPattern the regex pattern for the protocol field
     * @param lowerIPPattern the regex pattern for the lower IP field
     * @param upperIPPattern the regex pattern for the upper IP field
     * @param applicationPattern the regex pattern for the application field
     * @param limit limit for how many conversations to return
     * @return the list of conversations
     */
    @GET
    @Path("conversations/enumerate")
    @Produces(MediaType.APPLICATION_JSON)
    List<String> getConversations(@DefaultValue(".*") @QueryParam("location") final String locationPattern,
                                  @DefaultValue(".*") @QueryParam("protocol") final String protocolPattern,
                                  @DefaultValue(".*") @QueryParam("lower") final String lowerIPPattern,
                                  @DefaultValue(".*") @QueryParam("upper") final String upperIPPattern,
                                  @DefaultValue(".*") @QueryParam("application") final String applicationPattern,
                                  @DefaultValue(DEFAULT_LIMIT) @QueryParam("limit") final long limit,
                                  @Context UriInfo uriInfo);
    
    @GET
    @Path("conversations")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSummaryResponse getConversationSummary(
            @QueryParam("N") final Integer N,
            @QueryParam("conversation") final Set<String> conversations,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context final UriInfo uriInfo
    );

    @GET
    @Path("conversations/series")
    @Produces(MediaType.APPLICATION_JSON)
    FlowSeriesResponse getConversationSeries(
            @DefaultValue(DEFAULT_STEP_MS) @QueryParam("step") final long step,
            @QueryParam("N") final Integer N,
            @QueryParam("conversation") final Set<String> conversations,
            @DefaultValue("false") @QueryParam("includeOther") boolean includeOther,
            @Context final UriInfo uriInfo
    );

    @GET
    @Path("flowGraphUrl")
    FlowGraphUrlInfo getFlowGraphUrlInfo(@Context final UriInfo uriInfo);

}
