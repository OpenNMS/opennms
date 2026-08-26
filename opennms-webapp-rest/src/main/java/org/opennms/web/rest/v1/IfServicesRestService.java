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
package org.opennms.web.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceDetail;
import org.opennms.netmgt.model.OnmsMonitoredServiceDetailList;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Managing Monitored Services (control the polling state of monitored services).
 * 
 * Examples:
 *
 * curl -u admin:admin "http://localhost:8980/opennms/rest/ifservices?node.label=onms-prd-01"
 * curl -u admin:admin "http://localhost:8980/opennms/rest/ifservices?ipInterface.ipAddress=192.168.32.140"
 * curl -u admin:admin "http://localhost:8980/opennms/rest/ifservices?category.name=Production"
 * 
 * curl -X PUT "status=F" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?node.label=onms-prd-01"
 * curl -X PUT "status=A" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?ipInterface.ipAddress=192.168.32.140"
 * curl -X PUT "status=F" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?category.name=Production"
 * curl -X PUT "status=F&services=ICMP,HTTP" -u admin:admin "http://localhost:8980/opennms/rest/ifservices?category.name=Production"
 * 
 * Possible values for status:
 * A (Managed)
 * F (Forced Unmanaged)
 * R (Rescan to Resume, for compatibility purposes)
 * S (Rescan to Suspend, for compatibility purposes)
 * 
 * The optional parameter services is designed to specify the list of affected services as CSV.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@Component("ifServicesRestService")
@Path("ifservices")
@Tag(name = "Ifservices", description = "Ifservices API")
@Transactional
public class IfServicesRestService extends OnmsRestService {

    private static final Logger LOG = LoggerFactory.getLogger(IfServicesRestService.class);

    @Autowired
    private MonitoredServiceDao m_serviceDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Search monitored services",
            description = """
                    Returns a flat list of monitored services across all nodes, each with its polling status.

                    Every query parameter that is not one of the paging parameters below is read as a property
                    restriction on `OnmsMonitoredService`. Joins are pre-registered for `ipInterface`,
                    `ipInterface.node` as `node`, `ipInterface.node.categories` as `category`,
                    `ipInterface.snmpInterface` as `snmpInterface` and `serviceType`, so restrictions such as
                    `node.label`, `ipInterface.ipAddress`, `category.name` and `serviceType.name` all resolve.
                    A value of `null` or `notnull` becomes an is-null / is-not-null test instead of an equality
                    test. Unknown property names are logged and ignored rather than rejected.

                    Results are ordered by `id`. `totalCount` counts all matches, `count` the returned page.""",
            operationId = "searchIfServices",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "limit",
                            description = "Maximum rows to return. 0 disables the limit.",
                            schema = @Schema(type = "integer", defaultValue = "10"), example = "25"),
                    @Parameter(in = ParameterIn.QUERY, name = "offset",
                            description = "Zero-based index of the first row to return.",
                            schema = @Schema(type = "integer"), example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "orderBy",
                            description = "Property to sort on, replacing the default `id` ordering.",
                            schema = @Schema(type = "string"), example = "serviceType.name"),
                    @Parameter(in = ParameterIn.QUERY, name = "order",
                            description = "Sort direction for `orderBy`. Anything other than `desc` is read as ascending.",
                            schema = @Schema(type = "string", allowableValues = {"asc", "desc"}), example = "asc"),
                    @Parameter(in = ParameterIn.QUERY, name = "comparator",
                            description = "Comparison applied to every property restriction in the query.",
                            schema = @Schema(type = "string", defaultValue = "eq",
                                    allowableValues = {"eq", "ne", "gt", "lt", "ge", "le", "like", "ilike", "contains", "iplike"}),
                            example = "ilike"),
                    @Parameter(in = ParameterIn.QUERY, name = "match",
                            description = "Whether the property restrictions are combined with AND or OR.",
                            schema = @Schema(type = "string", defaultValue = "all", allowableValues = {"all", "any"}),
                            example = "any"),
                    @Parameter(in = ParameterIn.QUERY, name = "node.label",
                            description = "Restrict to services on nodes with this label. One of the generic property restrictions.",
                            schema = @Schema(type = "string"), example = "core-sw-01"),
                    @Parameter(in = ParameterIn.QUERY, name = "ipInterface.ipAddress",
                            description = "Restrict to services on this IP address.",
                            schema = @Schema(type = "string"), example = "192.0.2.10"),
                    @Parameter(in = ParameterIn.QUERY, name = "category.name",
                            description = "Restrict to services on nodes in this surveillance category.",
                            schema = @Schema(type = "string"), example = "Production"),
                    @Parameter(in = ParameterIn.QUERY, name = "serviceType.name",
                            description = "Restrict to services of this type.",
                            schema = @Schema(type = "string"), example = "ICMP")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matching monitored services.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMonitoredServiceDetailList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 2,
                      "count": 2,
                      "offset": 0,
                      "monitored-service": [
                        {
                          "id": "23616",
                          "status": "Managed",
                          "serviceName": "ICMP",
                          "statusCode": "A",
                          "ipAddress": "192.0.2.10",
                          "ipInterfaceId": 23601,
                          "isMonitored": true,
                          "node": "core-sw-01",
                          "isDown": false
                        },
                        {
                          "id": "23619",
                          "status": "Forced Unmanaged",
                          "serviceName": "SNMP",
                          "statusCode": "F",
                          "ipAddress": "192.0.2.10",
                          "ipInterfaceId": 23601,
                          "isMonitored": false,
                          "node": "core-sw-01",
                          "isDown": false
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMonitoredServiceDetailList.class),
                                    examples = @ExampleObject(value = """
                    <monitored-services count="1" offset="0" totalCount="1">
                      <monitored-service isDown="false" id="23616" ipInterfaceId="23601" isMonitored="true" statusCode="A">
                        <ipAddress>192.0.2.10</ipAddress>
                        <node>core-sw-01</node>
                        <serviceName>ICMP</serviceName>
                        <status>Managed</status>
                      </monitored-service>
                    </monitored-services>"""))
                    })
    })
    public OnmsMonitoredServiceDetailList getServices(@Context final UriInfo uriInfo) {
        final Criteria c = getCriteria(uriInfo.getQueryParameters());
        final OnmsMonitoredServiceDetailList servicesList = new OnmsMonitoredServiceDetailList();
        final List<OnmsMonitoredService> services = m_serviceDao.findMatching(c);
        for (OnmsMonitoredService svc : services) {
            servicesList.add(new OnmsMonitoredServiceDetail(svc));
        }
        c.setLimit(null);
        c.setOffset(null);
        c.setOrders(new ArrayList<Order>());
        servicesList.setTotalCount(m_serviceDao.countMatching(c));
        return servicesList;
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get one monitored service by database id",
            description = """
                    Looks the monitored service up by its `ifservices` primary key, which is the `id` reported by
                    the search endpoint. This is the numeric service id, not a node id and not a service name.

                    The representation follows the request `Accept` header and defaults to XML when none is
                    sent.""",
            operationId = "getIfServiceById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The monitored service.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMonitoredServiceDetail.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": "23616",
                      "status": "Managed",
                      "serviceName": "ICMP",
                      "statusCode": "A",
                      "ipAddress": "192.0.2.10",
                      "ipInterfaceId": 23601,
                      "isMonitored": true,
                      "node": "core-sw-01",
                      "isDown": false
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMonitoredServiceDetail.class),
                                    examples = @ExampleObject(value = """
                    <monitored-service isDown="false" id="23616" ipInterfaceId="23601" isMonitored="true" statusCode="A">
                      <ipAddress>192.0.2.10</ipAddress>
                      <node>core-sw-01</node>
                      <serviceName>ICMP</serviceName>
                      <status>Managed</status>
                    </monitored-service>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "No monitored service has that id. Also returned when `id` is not an integer, since the path then matches no method. The body is empty.")
    })
    public Response getServiceById(@Parameter(description = "Primary key of the monitored service, as reported by GET /ifservices.", example = "23616")
                                   @PathParam("id") Integer monitoredServiceId) {
        OnmsMonitoredService monitoredService = m_serviceDao.get(monitoredServiceId);
        if (monitoredService == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok().entity(new OnmsMonitoredServiceDetail(monitoredService)).build();
    }

    @PUT
    @Operation(
            summary = "Set the polling status of every matching monitored service",
            description = """
                    Bulk update of the poller status of the services selected by the query string. The selection
                    works exactly as it does for GET /ifservices, and `limit` and `offset` are cleared before the
                    update, so every match is affected, not just the first page.

                    With no query parameters the selection is every monitored service in the system.

                    `status=S` and a transition from `A` to `F` store `F` and send both
                    `serviceUnmanaged` and `suspendPollingService`. `status=R` and a transition from `F` to `A`
                    store `A` and send `resumePollingService`.""",
            operationId = "updateIfServices",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "node.label",
                            description = "Restrict the update to services on nodes with this label. Any `OnmsMonitoredService` property path accepted by GET /ifservices works here too.",
                            schema = @Schema(type = "string"), example = "core-sw-01"),
                    @Parameter(in = ParameterIn.QUERY, name = "ipInterface.ipAddress",
                            description = "Restrict the update to services on this IP address.",
                            schema = @Schema(type = "string"), example = "192.0.2.10"),
                    @Parameter(in = ParameterIn.QUERY, name = "category.name",
                            description = "Restrict the update to services on nodes in this surveillance category.",
                            schema = @Schema(type = "string"), example = "Production")
            }
    )
    @RequestBody(
            required = true,
            description = """
                    Form-encoded body. `status` is required. `services` optionally narrows the update to a
                    comma-separated list of service names within the selection; names that match nothing simply
                    leave the selection untouched.

                    Any content type is accepted, but only a form-encoded body deserializes; a JSON body fails
                    with HTTP 500.""",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "forceUnmanaged", summary = "Force every selected service unmanaged",
                                    value = "status=F"),
                            @ExampleObject(name = "resumeTwoServices", summary = "Resume polling for ICMP and HTTP only",
                                    value = "status=R&services=ICMP,HTTP")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one service was updated. No body."),
            @ApiResponse(responseCode = "304", description = "The selection was non-empty but `services` excluded every member of it, so nothing was written."),
            @ApiResponse(responseCode = "400", description = "`status` is missing or not one of A, F, R, S, or the query string selected no service.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "badStatus", value = "Parameter status must be specified. Possible values: A (Managed), F (Forced Unmanaged), R (Rescan to Resume), S (Rescan to Suspend)"),
                                    @ExampleObject(name = "noMatch", value = "Can't find any service matching the provided criteria: {node.label=[nosuchnode]}.")
                            })),
            @ApiResponse(responseCode = "500", description = "Sending event to the event bus failed, or the body was not form-encoded.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Can not deserialize instance of java.util.ArrayList out of VALUE_STRING token")))
    })
    public Response updateServices(@Context final UriInfo uriInfo, final MultivaluedMapImpl params) {
        final String status = params.getFirst("status");
        if (status == null || !status.matches("(A|R|S|F)")) {
            throw getException(Status.BAD_REQUEST, "Parameter status must be specified. Possible values: A (Managed), F (Forced Unmanaged), R (Rescan to Resume), S (Rescan to Suspend)");
        }
        final String services_csv = params.getFirst("services");
        final List<String> serviceList = new ArrayList<>();
        if (services_csv != null) {
            for (String s : services_csv.split(",")) {
                serviceList.add(s);
            }
        }
        final Criteria c = getCriteria(uriInfo.getQueryParameters());
        c.setLimit(null);
        c.setOffset(null);
        final OnmsMonitoredServiceList services = new OnmsMonitoredServiceList(m_serviceDao.findMatching(c));
        if (services.isEmpty()) {
            throw getException(Status.BAD_REQUEST, "Can't find any service matching the provided criteria: {}.", uriInfo.getQueryParameters().toString());
        }
        boolean modified = false;
        for (OnmsMonitoredService svc : services) {
            boolean proceed = false;
            if (serviceList.isEmpty()) {
                proceed = true;
            } else {
                if (serviceList.contains(svc.getServiceName())) {
                    proceed = true;
                }
            }
            if (proceed) {
                modified = true;
                final String currentStatus = svc.getStatus();
                svc.setStatus(status);
                m_serviceDao.update(svc);
                if ("S".equals(status) || ("A".equals(currentStatus) && "F".equals(status))) {
                    LOG.debug("updateServices: suspending polling for service {} on node with IP {}", svc.getServiceName(), svc.getIpAddress().getHostAddress());
                    sendEvent(EventConstants.SERVICE_UNMANAGED_EVENT_UEI, svc); // TODO ManageNodeServlet is sending this.
                    sendEvent(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, svc);
                }
                if ("R".equals(status) || ("F".equals(currentStatus) && "A".equals(status))) {
                    LOG.debug("updateServices: resuming polling for service {} on node with IP {}", svc.getServiceName(), svc.getIpAddress().getHostAddress());
                    sendEvent(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, svc);
                }
            }
        }
        return modified ? Response.noContent().build() : Response.notModified().build();
    }

    private static Criteria getCriteria(final MultivaluedMap<String, String> params) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoredService.class);
        builder.alias("ipInterface.snmpInterface", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterface", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.node", "node", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.node.categories", "category", JoinType.LEFT_JOIN);
        builder.alias("serviceType", "serviceType", JoinType.LEFT_JOIN);
        builder.orderBy("id");
        applyQueryFilters(params, builder);

        return builder.toCriteria();
    }

    private void sendEvent(String eventUEI, OnmsMonitoredService dbObj) {
        final EventBuilder bldr = new EventBuilder(eventUEI, "ReST");
        bldr.setNodeid(dbObj.getNodeId());
        bldr.setInterface(dbObj.getIpAddress());
        bldr.setService(dbObj.getServiceName());
        try {
            m_eventProxy.send(bldr.getEvent());
        } catch (EventProxyException ex) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", eventUEI, ex.getMessage());
        }
    }

}
