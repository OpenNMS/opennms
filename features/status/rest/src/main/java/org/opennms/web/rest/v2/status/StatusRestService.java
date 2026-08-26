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
package org.opennms.web.rest.v2.status;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.features.status.api.Query;
import org.opennms.features.status.api.SeverityFilter;
import org.opennms.features.status.api.StatusEntity;
import org.opennms.features.status.api.StatusSummary;
import org.opennms.features.status.api.application.ApplicationStatusService;
import org.opennms.features.status.api.bsm.BusinessServiceStatusService;
import org.opennms.features.status.api.node.NodeQuery;
import org.opennms.features.status.api.node.NodeStatusService;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculationStrategy;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.v2.status.model.ApplicationDTO;
import org.opennms.web.rest.v2.status.model.ApplicationDTOList;
import org.opennms.web.rest.v2.status.model.BusinessServiceDTO;
import org.opennms.web.rest.v2.status.model.BusinessServiceDTOList;
import org.opennms.web.rest.v2.status.model.NodeDTO;
import org.opennms.web.rest.v2.status.model.NodeDTOList;
import org.opennms.web.utils.QueryParameters;
import org.opennms.web.utils.QueryParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@Component("statusRestService")
@Path("status")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Tag(name = "Status", description = """
        Status API.

        Severity rollups for nodes, applications and business services. The `/summary/*` endpoints return
        one count per severity; the list endpoints return a page of entities each carrying its own
        severity.

        `@Produces` lists XML before JSON, so a request whose `Accept` is `*/*` is answered as XML. The
        three `/summary/*` endpoints return a raw `List<Object[]>` for which no XML writer is registered,
        so they only work with `Accept: application/json`.

        The list endpoints take `limit`, `offset`, `orderBy`, `order` and a repeatable `severityFilter`.
        `severityFilter` has to be spelled as a severity label (`Normal`, `Warning`, `Minor`, `Major`,
        `Critical`); any other value fails with a 500. On `/status/applications` and
        `/status/business-services`, supplying `severityFilter` without `orderBy` also fails with a 500.
        An empty page is reported as 204 rather than as an empty list, and a non-empty page carries a
        `Content-Range: items <first>-<last>/<total>` header.""")
public class StatusRestService {

    @Autowired
    private BusinessServiceStatusService businessServiceStatusService;

    @Autowired
    private ApplicationStatusService applicationStatusService;

    @Autowired
    private NodeStatusService nodeStatusService;

    @GET
    @Path("/summary/nodes/{type}")
    @Operation(
            summary = "Summarise node status by severity",
            description = """
        Return one count per severity across all nodes, using the named calculation strategy.
        Requires `Accept: application/json`: the body is a raw list for which no XML writer exists, so
        an XML or `*/*` request fails with a 500.
        `None` is listed as a supported strategy by the 400 message but is not implemented for this
        query and fails with a 500.""",
            operationId = "getNodeStatusSummary"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Per-severity counts, ordered from least to most severe. Each element is a two-element array of severity label and count.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "array")),
                            examples = @ExampleObject(value = "[[\"Normal\",3676],[\"Warning\",0],[\"Minor\",1],[\"Major\",3],[\"Critical\",0]]"))),
            @ApiResponse(responseCode = "400", description = "`type` did not name a calculation strategy.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Strategy 'bogus' not supported. Supported values are:[None, Alarms, Outages]"))),
            @ApiResponse(responseCode = "500", description = "`type` was `None`, or the client asked for XML.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Query for CalculationStrategy is not implemented.")))
    })
    public Response getNodeStatus(
            @Parameter(description = "Node status calculation strategy. `Alarms` rolls up unacknowledged alarms, `Outages` rolls up open outages. `None` is rejected at runtime.",
                    example = "Alarms", required = true,
                    schema = @Schema(allowableValues = {"None", "Alarms", "Outages"}))
            @PathParam("type") String type) {
        final NodeStatusCalculationStrategy strategy = NodeStatusCalculationStrategy.createFrom(type);
        if (strategy == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Strategy '" + type + "' not supported. Supported values are:" + Arrays.toString(NodeStatusCalculationStrategy.values()))
                    .build();
        }
        final StatusSummary summary = nodeStatusService.getSummary(strategy);
        return Response.ok().entity(convert(summary)).build();
    }

    @GET
    @Path("/summary/applications")
    @Operation(
            summary = "Summarise application status by severity",
            description = """
        Return one count per severity across all applications.
        Requires `Accept: application/json`: the body is a raw list for which no XML writer exists, so
        an XML or `*/*` request fails with a 500.""",
            operationId = "getApplicationStatusSummary"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Per-severity counts, ordered from least to most severe. Each element is a two-element array of severity label and count.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "array")),
                            examples = @ExampleObject(value = "[[\"Normal\",4],[\"Warning\",0],[\"Minor\",0],[\"Major\",0],[\"Critical\",0]]"))),
            @ApiResponse(responseCode = "500", description = "The client asked for XML.",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No message body writer has been found for class java.util.ArrayList, ContentType: application/xml")))
    })
    public List<Object[]> getApplicationStatus() {
        StatusSummary summary = applicationStatusService.getSummary();
        return convert(summary);
    }

    @GET
    @Path("/summary/business-services")
    @Operation(
            summary = "Summarise business service status by severity",
            description = """
        Return one count per severity across all business services.
        Requires `Accept: application/json`: the body is a raw list for which no XML writer exists, so
        an XML or `*/*` request fails with a 500.""",
            operationId = "getBusinessServiceStatusSummary"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Per-severity counts, ordered from least to most severe. Each element is a two-element array of severity label and count.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            array = @ArraySchema(schema = @Schema(type = "array")),
                            examples = @ExampleObject(value = "[[\"Normal\",0],[\"Warning\",0],[\"Minor\",0],[\"Major\",0],[\"Critical\",0]]"))),
            @ApiResponse(responseCode = "500", description = "The client asked for XML.",
                    content = @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No message body writer has been found for class java.util.ArrayList, ContentType: application/xml")))
    })
    public List<Object[]> getBusinessServiceStatus() {
        StatusSummary summary = businessServiceStatusService.getSummary();
        return convert(summary);
    }

    @GET
    @Path("/applications")
    @Operation(
            summary = "List applications with their status",
            description = """
        Page through the applications, each carrying its rolled-up severity. `limit`, `offset`,
        `orderBy`, `order` and a repeatable `severityFilter` are read straight off the query string.
        Supplying `severityFilter` without `orderBy` fails with a 500.""",
            operationId = "getApplicationStatusList"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One page of applications. `Content-Range` carries the offsets and the total.",
                    content = {
                        @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = ApplicationDTOList.class),
                                examples = @ExampleObject(value = """
                    {
                      "totalCount": 4,
                      "count": 2,
                      "offset": 0,
                      "applications": [
                        { "id": 1, "name": "Review Billing", "severity": "NORMAL" },
                        { "id": 2, "name": "Review Storefront", "severity": "NORMAL" }
                      ]
                    }""")),
                        @Content(mediaType = MediaType.APPLICATION_XML,
                                schema = @Schema(implementation = ApplicationDTOList.class),
                                examples = @ExampleObject(value = """
                    <applications count="2" offset="0" totalCount="4"><application><id>1</id><name>Review Billing</name><severity>NORMAL</severity></application><application><id>2</id><name>Review Storefront</name><severity>NORMAL</severity></application></applications>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No application matched."),
            @ApiResponse(responseCode = "500", description = "An unrecognised `severityFilter`, or a `severityFilter` sent without `orderBy`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.web.utils.QueryParameters$Order.getColumn()\" because the return value of \"org.opennms.web.utils.QueryParameters.getOrder()\" is null")))
    })
    public Response getApplications(
            @Parameter(description = "Reads `limit`, `offset`, `orderBy`, `order` and a repeatable `severityFilter` (`Normal`, `Warning`, `Minor`, `Major`, `Critical`) from the query string.", hidden = true)
            @Context final UriInfo uriInfo) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SeverityFilter severityFilter = getSeverityFilter(uriInfo);
        final Query query = new Query(queryParameters, severityFilter);

        final List<StatusEntity<OnmsApplication>> applications = applicationStatusService.getStatus(query);
        final int totalCount = applicationStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final List<ApplicationDTO> statusEntities = applications.stream().map(a -> {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setId(a.getEntity().getId());
            dto.setName(a.getEntity().getName());
            dto.setSeverity(a.getStatus());
            return dto;
        }).collect(Collectors.toList());
        final ApplicationDTOList list = new ApplicationDTOList(statusEntities);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        return createResponse(list, offset, totalCount);
    }

    @GET
    @Path("/business-services")
    @Operation(
            summary = "List business services with their status",
            description = """
        Page through the business services, each carrying its rolled-up severity. `limit`, `offset`,
        `orderBy`, `order` and a repeatable `severityFilter` are read straight off the query string.
        Supplying `severityFilter` without `orderBy` fails with a 500.""",
            operationId = "getBusinessServiceStatusList"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One page of business services. `Content-Range` carries the offsets and the total.",
                    content = {
                        @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = BusinessServiceDTOList.class),
                                examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "businessservices": [
                        { "id": 23663, "name": "Storefront", "severity": "MINOR" }
                      ]
                    }""")),
                        @Content(mediaType = MediaType.APPLICATION_XML,
                                schema = @Schema(implementation = BusinessServiceDTOList.class))
                    }),
            @ApiResponse(responseCode = "204", description = "No business service matched, including when none is defined."),
            @ApiResponse(responseCode = "500", description = "An unrecognised `severityFilter`, or a `severityFilter` sent without `orderBy`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.model.OnmsSeverity.getId()\" because \"s\" is null")))
    })
    public Response getBusinessServices(
            @Parameter(description = "Reads `limit`, `offset`, `orderBy`, `order` and a repeatable `severityFilter` (`Normal`, `Warning`, `Minor`, `Major`, `Critical`) from the query string.", hidden = true)
            @Context final UriInfo uriInfo) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SeverityFilter severityFilter = getSeverityFilter(uriInfo);
        final Query query = new Query(queryParameters, severityFilter);

        final List<StatusEntity<BusinessService>> services = businessServiceStatusService.getStatus(query);
        final int totalCount = businessServiceStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final List<BusinessServiceDTO> statusEntities = services.stream().map(bs -> {
            BusinessServiceDTO statusDTO = new BusinessServiceDTO();
            statusDTO.setId(bs.getEntity().getId().intValue());
            statusDTO.setName(bs.getEntity().getName());
            statusDTO.setSeverity(bs.getStatus());
            return statusDTO;
        }).collect(Collectors.toList());
        final BusinessServiceDTOList list = new BusinessServiceDTOList(statusEntities);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        return createResponse(list, offset, totalCount);
    }

    @GET
    @Path("/nodes/{type}")
    @Operation(
            summary = "List nodes with their status",
            description = """
        Page through the nodes, each carrying the severity the named strategy computed. `limit`,
        `offset`, `orderBy`, `order` and a repeatable `severityFilter` are read straight off the query
        string.

        `orderBy` has to name a database column, so `nodeid` and `nodelabel` work. `label` is rewritten
        to `node.nodelabel` and then rejected with a 500, as is any other unrecognised value.""",
            operationId = "getNodeStatusList"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One page of nodes. `Content-Range` carries the offsets and the total.",
                    content = {
                        @Content(mediaType = MediaType.APPLICATION_JSON,
                                schema = @Schema(implementation = NodeDTOList.class),
                                examples = @ExampleObject(value = """
                    {
                      "totalCount": 3680,
                      "count": 2,
                      "offset": 0,
                      "nodes": [
                        { "id": 1, "name": "loopback-004", "severity": "NORMAL" },
                        { "id": 2, "name": "loopback-001", "severity": "MINOR" }
                      ]
                    }""")),
                        @Content(mediaType = MediaType.APPLICATION_XML,
                                schema = @Schema(implementation = NodeDTOList.class),
                                examples = @ExampleObject(value = """
                    <nodes count="2" offset="0" totalCount="3680"><node><id>1</id><name>loopback-004</name><severity>NORMAL</severity></node><node><id>2</id><name>loopback-001</name><severity>MINOR</severity></node></nodes>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "No node matched."),
            @ApiResponse(responseCode = "400", description = "`type` did not name a calculation strategy.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Strategy 'bogus' not supported. Supported values are:[None, Alarms, Outages]"))),
            @ApiResponse(responseCode = "500", description = "An `orderBy` the DAO does not accept, or an unrecognised `severityFilter`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Invalid order column: node.nodelabel")))
    })
    public Response getNodes(
            @Parameter(description = "Reads `limit`, `offset`, `orderBy` (a database column such as `nodeid` or `nodelabel`), `order` and a repeatable `severityFilter` (`Normal`, `Warning`, `Minor`, `Major`, `Critical`) from the query string.", hidden = true)
            @Context final UriInfo uriInfo,
            @Parameter(description = "Node status calculation strategy. `Alarms` rolls up unacknowledged alarms, `Outages` rolls up open outages.",
                    example = "Alarms", required = true,
                    schema = @Schema(allowableValues = {"None", "Alarms", "Outages"}))
            @PathParam("type") String type) {
        final NodeStatusCalculationStrategy strategy = NodeStatusCalculationStrategy.createFrom(type);
        if (strategy == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Strategy '" + type + "' not supported. Supported values are:" + Arrays.toString(NodeStatusCalculationStrategy.values()))
                    .build();
        }

        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SeverityFilter severityFilter = getSeverityFilter(uriInfo);
        final NodeQuery query = new NodeQuery(queryParameters, severityFilter);
        query.setStatusCalculationStrategy(strategy);

        // Adjust order parameters
        if (query.getParameters().getOrder() != null && query.getParameters().getOrder().getColumn().equals("label")) {
            query.getParameters().setOrder(new QueryParameters.Order("node.nodelabel", query.getParameters().getOrder().isDesc()));
        }

        final List<StatusEntity<OnmsNode>> nodes = nodeStatusService.getStatus(query);
        final int totalCount = nodeStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final List<NodeDTO> statusEntities = nodes.stream().map(node -> {
            NodeDTO nodeDTO = new NodeDTO();
            nodeDTO.setId(node.getEntity().getId());
            nodeDTO.setName(node.getEntity().getLabel());
            nodeDTO.setSeverity(node.getStatus());
            return nodeDTO;
        }).collect(Collectors.toList());
        final NodeDTOList list = new NodeDTOList(statusEntities);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        return createResponse(list, offset, totalCount);
    }

    private static List<Object[]> convert(StatusSummary statusSummary) {
        return convert(statusSummary.getSeverityMap());
    }

    private static List<Object[]> convert(Map<OnmsSeverity, Long> input) {
        return input.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> new Object[]{e.getKey().getLabel(), e.getValue()})
                .collect(Collectors.toList());
    }

    private static SeverityFilter getSeverityFilter(UriInfo uriInfo) {
        final SeverityFilter severityFilter = new SeverityFilter();
        final List<String> severityFilterList = uriInfo.getQueryParameters().get("severityFilter");
        if (severityFilterList != null) {
            for (String eachSeverity : severityFilterList) {
                OnmsSeverity severity = getSeverity(eachSeverity);
                severityFilter.add(severity);
            }
        }
        return severityFilter;
    }

    private static OnmsSeverity getSeverity(String severityString) {
        if (!Strings.isNullOrEmpty(severityString)) {
            for (OnmsSeverity eachSeverity : OnmsSeverity.values()) {
                if (eachSeverity.getLabel().equalsIgnoreCase(severityString)) {
                    return eachSeverity;
                }
            }
        }
        return null;
    }

    private static Response createResponse(JaxbListWrapper list, int offset, int totalCount) {
        if (list.isEmpty()) {
            return Response.noContent().build();
        } else {
            // Make sure that offset is set to a numeric value when setting the Content-Range header
            return Response
                    .ok(list)
                    .header("Content-Range", String.format("items %d-%d/%d", offset, offset + list.size() - 1, totalCount))
                    .build();
        }
    }
}
