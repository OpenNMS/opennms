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
package org.opennms.web.rest.v2.bsm;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import javax.ws.rs.core.UriInfo;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.edge.ApplicationEdge;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.web.rest.api.ResourceLocationFactory;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceListDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceRequestDTO;
import org.opennms.web.rest.v2.bsm.model.BusinessServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.MapFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.ReduceFunctionDTO;
import org.opennms.web.rest.v2.bsm.model.edge.AbstractEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ApplicationEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ApplicationEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ApplicationResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ChildEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.EdgeRequestDTOVisitor;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.IpServiceResponseDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeRequestDTO;
import org.opennms.web.rest.v2.bsm.model.edge.ReductionKeyEdgeResponseDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionMetaDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionMetaListDTO;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionType;
import org.opennms.web.rest.v2.bsm.model.meta.FunctionsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@Path("business-services")
@Transactional
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
@Tag(name = "BusinessServices", description = """
        Business Service Monitoring API.

        A business service carries a reduce function and a set of edges. Each edge points at an IP
        service, an application, a reduction key, or another business service, and carries a map function
        and a weight. The reduce function turns the mapped severities of the edges into the service's own
        operational status.

        Bodies are JAXB beans serialized through the codehaus Jackson provider, so JSON field names come
        from the `@XmlElement` names: `reduce-function`, `map-function`, `ip-service-edges`,
        `child-edges`, `reduction-key-edges`, `ip-service-id`, `child-id`, `reduction-key`,
        `friendly-name`. `attributes` is not a plain map: it serializes as
        `{"attribute":[{"key":"...","value":"..."}]}`, and sending a plain map fails with a 500.

        `POST /business-services` and `PUT /business-services/{id}` apply the child, IP-service and
        reduction-key edges in the body. Application edges are read back on the response but are not
        applied from a request body, and there is no per-edge endpoint for them.

        A `{id}` or `{edgeId}` that does not exist is a 404 whose body is a `text/plain` entity
        description such as `BusinessServiceEntity with id 42`. Requests that would not change anything
        answer 304.""")
// The response DTOs carrying a custom @JsonSerialize (BusinessServiceListDTO,
// BusinessServiceResponseDTO, AbstractEdgeResponseDTO) are documented by example only:
// swagger-core 2.1.12 introspects custom serializers through BeanDescription.findJsonValueMethod(),
// which Jackson dropped after 2.12, so referencing them as a schema fails document generation.
public class BusinessServiceRestService {

    private static final String BUSINESS_SERVICE_REQUEST_EXAMPLE = """
            {
              "name": "Storefront",
              "attributes": { "attribute": [ { "key": "dc", "value": "RDU" } ] },
              "reduce-function": { "type": "Threshold", "properties": { "threshold": "0.26" } },
              "ip-service-edges": [
                {
                  "ip-service-id": 1017,
                  "friendly-name": "web front end",
                  "map-function": { "type": "Identity", "properties": {} },
                  "weight": 1
                }
              ],
              "reduction-key-edges": [
                {
                  "reduction-key": "uei.opennms.org/nodes/nodeDown::1",
                  "friendly-name": "core switch down",
                  "map-function": { "type": "Increase", "properties": {} },
                  "weight": 2
                }
              ],
              "child-edges": [
                {
                  "child-id": 23641,
                  "map-function": { "type": "Identity", "properties": {} },
                  "weight": 1
                }
              ]
            }""";

    @Autowired
    private BusinessServiceManager businessServiceManager;

    protected BusinessServiceManager getManager() {
        return businessServiceManager;
    }

    @GET
    @Operation(
            summary = "List business services",
            description = """
        Return a link per business service. The list carries resource locations, not the services
        themselves; follow each one to `GET /business-services/{id}` for the detail.""",
            operationId = "listBusinessServices"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business service locations returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(value = """
                    {"business-services":["/api/v2/business-services/23641","/api/v2/business-services/23663"]}"""))),
            @ApiResponse(responseCode = "204", description = "No business service is defined.")
    })
    public Response list() {
        List<BusinessService> services = getManager().getAllBusinessServices();
        if (services == null || services.isEmpty()) {
            return Response.noContent().build();
        }
        BusinessServiceListDTO serviceList = new BusinessServiceListDTO(services);
        return Response.ok(serviceList).build();
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get a business service",
            description = """
        Return one business service with its reduce function, its edges grouped by kind, its parent
        services and its current operational status. Each edge carries the reduction keys it listens on,
        which for an IP-service edge are derived from the node, interface and service.""",
            operationId = "getBusinessServiceById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business service returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(value = """
                    {
                      "location": "/api/v2/business-services/23663",
                      "id": 23663,
                      "name": "Storefront",
                      "attributes": { "attribute": [ { "key": "dc", "value": "RDU" } ] },
                      "ip-service-edges": [
                        {
                          "id": 23667,
                          "location": "/api/v2/business-services/23663/edges/23667",
                          "weight": 1,
                          "operational-status": "INDETERMINATE",
                          "map-function": { "type": "Identity", "properties": {} },
                          "reduction-keys": [
                            "uei.opennms.org/nodes/nodeDown::2",
                            "uei.opennms.org/nodes/nodeLostService::2:127.0.0.1:HTTP-8080",
                            "uei.opennms.org/nodes/interfaceDown::2:127.0.0.1"
                          ],
                          "ip-service": {
                            "location": "/rest/ifservices/1017",
                            "id": 1017,
                            "service-name": "HTTP-8080",
                            "node-label": "loopback-001",
                            "ip-address": "/127.0.0.1"
                          },
                          "friendly-name": "web front end"
                        }
                      ],
                      "reduction-key-edges": [],
                      "child-edges": [],
                      "application-edges": [],
                      "parent-services": [],
                      "reduce-function": { "type": "HighestSeverity", "properties": {} },
                      "operational-status": "INDETERMINATE"
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No business service carries that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "BusinessServiceEntity with id 42")))
    })
    public Response getById(
            @Parameter(description = "Business service id.", example = "23663", required = true)
            @PathParam("id") Long id) {
        BusinessService service = getManager().getBusinessServiceById(id);

        final BusinessServiceResponseDTO response = new BusinessServiceResponseDTO();
        response.setId(service.getId());
        response.setName(service.getName());
        response.setAttributes(service.getAttributes());
        response.setLocation(ResourceLocationFactory.createBusinessServiceLocation(service.getId().toString()));
        response.setParentServices(service.getParentServices().stream().map(BusinessService::getId).collect(Collectors.toSet()));
        response.setOperationalStatus(service.getOperationalStatus());
        response.setReduceFunction(transform(service.getReduceFunction()));

        service.getEdges().forEach(eachEdge -> eachEdge.accept(new EdgeVisitor<Void>() {
            @Override
            public Void visit(IpServiceEdge edge) {
                response.getIpServices().add(transform(edge));
                return null;
            }

            @Override
            public Void visit(ReductionKeyEdge edge) {
                response.getReductionKeys().add(transform(edge));
                return null;
            }

            @Override
            public Void visit(ChildEdge edge) {
                response.getChildren().add(transform(edge));
                return null;
            }

            @Override
            public Void visit(ApplicationEdge edge) {
                response.getApplications().add(transform(edge));
                return null;
            }
        }));
        return Response.ok(response).build();
    }

    @POST
    @Operation(
            summary = "Create a business service",
            description = """
        Create a business service from the supplied name, attributes, reduce function and edges. The
        `Location` header of the 201 points at the new service. Referenced ids have to exist: an unknown
        `child-id`, `ip-service-id` or `application-id` is reported as a 404 naming the missing entity.
        A `child-edges` entry that would make the graph cyclic fails with a 500.
        Application edges in the body are not applied.""",
            operationId = "createBusinessService"
    )
    @RequestBody(
            required = true,
            description = "Business service to create.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = BusinessServiceRequestDTO.class),
                    examples = @ExampleObject(value = BUSINESS_SERVICE_REQUEST_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Business service created. No response body; `Location` points at the new service."),
            @ApiResponse(responseCode = "404", description = "A referenced child service, IP service or application does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "OnmsMonitoredService with id 99999999"))),
            @ApiResponse(responseCode = "500", description = "The edges would form a loop, or `attributes` was sent as a plain map instead of the `{\"attribute\":[...]}` wrapper.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service will form a loop")))
    })
    public Response create(@Context final UriInfo uriInfo, final BusinessServiceRequestDTO request) {
        final BusinessService service = getManager().createBusinessService();
        service.setName(request.getName());
        service.setAttributes(request.getAttributes());
        service.setReduceFunction(transform(request.getReduceFunction()));

        request.getEdges().forEach(eachEdge -> eachEdge.accept(new EdgeRequestDTOVisitor() {
            @Override
            public void visit(IpServiceEdgeRequestDTO ipEdge) {
                service.addIpServiceEdge(
                        getManager().getIpServiceById(ipEdge.getIpServiceId()),
                        transform(ipEdge.getMapFunction()),
                        ipEdge.getWeight(),
                        ipEdge.getFriendlyName());
            }

            @Override
            public void visit(ChildEdgeRequestDTO childEdge) {
                service.addChildEdge(
                        getManager().getBusinessServiceById(childEdge.getChildId()),
                        transform(childEdge.getMapFunction()),
                        childEdge.getWeight());
            }

            @Override
            public void visit(ReductionKeyEdgeRequestDTO rkEdge) {
                service.addReductionKeyEdge(
                        rkEdge.getReductionKey(),
                        transform(rkEdge.getMapFunction()),
                        rkEdge.getWeight(),
                        rkEdge.getFriendlyName());
            }

            @Override
            public void visit(ApplicationEdgeRequestDTO rkEdge) {
                service.addApplicationEdge(
                        getManager().getApplicationById(rkEdge.getApplicationId()),
                        transform(rkEdge.getMapFunction()),
                        rkEdge.getWeight());
            }
        }));
        getManager().saveBusinessService(service);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, service.getId())).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete a business service",
            description = """
        Delete one business service along with its edges. Edges in other services that pointed at it as a
        child are removed with it. A successful response carries no body.""",
            operationId = "deleteBusinessService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Business service deleted. No response body."),
            @ApiResponse(responseCode = "404", description = "No business service carries that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "BusinessServiceEntity with id 42")))
    })
    public Response delete(
            @Parameter(description = "Business service id.", example = "23663", required = true)
            @PathParam("id") Long id) {
        final BusinessService service = getManager().getBusinessServiceById(id);
        getManager().deleteBusinessService(service);

        return Response.ok().build();
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Replace a business service",
            description = """
        Replace a business service in full. The four edge collections are cleared before the body's edges
        are applied, so an edge left out of the body is deleted and an edge that survives is recreated
        with a new edge id. Application edges in the body are not applied.
        A successful response carries no body.""",
            operationId = "updateBusinessService"
    )
    @RequestBody(
            required = true,
            description = "Replacement business service.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = BusinessServiceRequestDTO.class),
                    examples = @ExampleObject(value = BUSINESS_SERVICE_REQUEST_EXAMPLE))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Business service replaced. No response body."),
            @ApiResponse(responseCode = "404", description = "The business service, or a referenced child service, IP service or application, does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "BusinessServiceEntity with id 42"))),
            @ApiResponse(responseCode = "500", description = "The edges would form a loop, or `attributes` was sent as a plain map instead of the `{\"attribute\":[...]}` wrapper.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service will form a loop")))
    })
    public Response update(
            @Parameter(description = "Business service id.", example = "23663", required = true)
            @PathParam("id") final Long id, final BusinessServiceRequestDTO request) {
        final BusinessService service = getManager().getBusinessServiceById(id);
        service.setName(request.getName());
        service.setAttributes(request.getAttributes());
        service.setReduceFunction(transform(request.getReduceFunction()));
        service.setReductionKeyEdges(Sets.newHashSet());
        service.setIpServiceEdges(Sets.newHashSet());
        service.setApplicationEdges(Sets.newHashSet());
        service.setChildEdges(Sets.newHashSet());

        request.getEdges().forEach(eachEdge -> eachEdge.accept(new EdgeRequestDTOVisitor() {

            @Override
            public void visit(IpServiceEdgeRequestDTO ipEdge) {
                getManager().addIpServiceEdge(service,
                        getManager().getIpServiceById(ipEdge.getIpServiceId()),
                        transform(ipEdge.getMapFunction()),
                        ipEdge.getWeight(),
                        ipEdge.getFriendlyName());
            }

            @Override
            public void visit(ChildEdgeRequestDTO childEdge) {
                getManager().addChildEdge(
                        service,
                        getManager().getBusinessServiceById(childEdge.getChildId()),
                        transform(childEdge.getMapFunction()),
                        childEdge.getWeight());
            }

            @Override
            public void visit(ReductionKeyEdgeRequestDTO rkEdge) {
                getManager().addReductionKeyEdge(
                    service,
                    rkEdge.getReductionKey(),
                    transform(rkEdge.getMapFunction()),
                    rkEdge.getWeight(),
                    rkEdge.getFriendlyName());
            }

            @Override
            public void visit(ApplicationEdgeRequestDTO applicationEdge) {
                getManager().addApplicationEdge(
                        service,
                        getManager().getApplicationById(applicationEdge.getApplicationId()),
                        transform(applicationEdge.getMapFunction()),
                        applicationEdge.getWeight());
            }
        }));
        getManager().saveBusinessService(service);

        return Response.noContent().build();
    }

    @GET
    @Path("/edges/{edgeId}")
    @Operation(
            summary = "Get an edge",
            description = """
        Return one edge by id, regardless of which business service owns it. The response shape depends
        on the edge kind: an IP-service edge carries `ip-service`, a child edge carries `child-id`, a
        reduction-key edge carries `reduction-key`, an application edge carries `application`.""",
            operationId = "getBusinessServiceEdgeById"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(value = """
                    {
                      "id": 23667,
                      "location": "/api/v2/business-services/23663/edges/23667",
                      "weight": 1,
                      "operational-status": "INDETERMINATE",
                      "map-function": { "type": "Identity", "properties": {} },
                      "reduction-keys": [ "uei.opennms.org/nodes/nodeDown::2" ],
                      "ip-service": {
                        "location": "/rest/ifservices/1017",
                        "id": 1017,
                        "service-name": "HTTP-8080",
                        "node-label": "loopback-001",
                        "ip-address": "/127.0.0.1"
                      },
                      "friendly-name": "web front end"
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No edge carries that id.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "BusinessServiceEdgeEntity with id 42")))
    })
    public Response getEdgeById(
            @Parameter(description = "Edge id.", example = "23667", required = true)
            @PathParam("edgeId") final Long edgeId) {
        Edge edge = getManager().getEdgeById(edgeId);
        AbstractEdgeResponseDTO edgeDTO = transform(edge);
        return Response.ok().entity(edgeDTO).build();
    }

    @POST
    @Path("{id}/ip-service-edge")
    @Operation(
            summary = "Add an IP service edge",
            description = """
        Add one IP-service edge to an existing business service, leaving its other edges alone. An edge
        that already matches the request adds nothing and answers 304. A successful response carries no
        body; read the new edge id back from `GET /business-services/{id}`.""",
            operationId = "addBusinessServiceIpServiceEdge"
    )
    @RequestBody(
            required = true,
            description = "IP service edge to add.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = IpServiceEdgeRequestDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "ip-service-id": 1017,
                      "friendly-name": "web front end",
                      "map-function": { "type": "Identity", "properties": {} },
                      "weight": 1
                    }"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge added. No response body."),
            @ApiResponse(responseCode = "304", description = "An identical edge is already present; nothing changed."),
            @ApiResponse(responseCode = "404", description = "The business service or the IP service does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "OnmsMonitoredService with id 99999999")))
    })
    // Add IpService
    public Response addIpServiceEdge(
            @Parameter(description = "Business service id the edge is added to.", example = "23663", required = true)
            @PathParam("id") final Long serviceId,
                            final IpServiceEdgeRequestDTO edgeRequest) {
        final BusinessService businessService = getManager().getBusinessServiceById(serviceId);
        final IpService ipService = getManager().getIpServiceById(edgeRequest.getIpServiceId());
        boolean changed = getManager().addIpServiceEdge(businessService, ipService, transform(edgeRequest.getMapFunction()), edgeRequest.getWeight(), edgeRequest.getFriendlyName());
        if (!changed) {
            return Response.notModified().build();
        }
        businessService.save();
        return Response.ok().build();
    }

    @POST
    @Path("{id}/reduction-key-edge")
    @Operation(
            summary = "Add a reduction key edge",
            description = """
        Add one reduction-key edge to an existing business service, leaving its other edges alone. The
        reduction key is matched against alarms as an opaque string and is not validated here. An edge
        that already matches the request adds nothing and answers 304. A successful response carries no
        body.""",
            operationId = "addBusinessServiceReductionKeyEdge"
    )
    @RequestBody(
            required = true,
            description = "Reduction key edge to add.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ReductionKeyEdgeRequestDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "reduction-key": "uei.opennms.org/nodes/nodeDown::1",
                      "friendly-name": "core switch down",
                      "map-function": { "type": "Increase", "properties": {} },
                      "weight": 2
                    }"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge added. No response body."),
            @ApiResponse(responseCode = "304", description = "An identical edge is already present; nothing changed."),
            @ApiResponse(responseCode = "404", description = "The business service does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "BusinessServiceEntity with id 42")))
    })
    // Add Reduction Key
    public Response addReductionKeyEdge(
            @Parameter(description = "Business service id the edge is added to.", example = "23663", required = true)
            @PathParam("id") final Long serviceId,
                            final ReductionKeyEdgeRequestDTO edgeRequest) {
        final BusinessService businessService = getManager().getBusinessServiceById(serviceId);
        boolean changed = getManager().addReductionKeyEdge(businessService, edgeRequest.getReductionKey(), transform(edgeRequest.getMapFunction()), edgeRequest.getWeight(), edgeRequest.getFriendlyName());
        if (!changed) {
            return Response.notModified().build();
        }
        businessService.save();
        return Response.ok().build();
    }

    @POST
    @Path("{id}/child-edge")
    @Operation(
            summary = "Add a child service edge",
            description = """
        Add one child-service edge to an existing business service, leaving its other edges alone. An
        edge that already matches the request adds nothing and answers 304. An edge that would make the
        graph cyclic, including a service pointing at itself, fails with a 500. A successful response
        carries no body.""",
            operationId = "addBusinessServiceChildEdge"
    )
    @RequestBody(
            required = true,
            description = "Child service edge to add.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ChildEdgeRequestDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "child-id": 23641,
                      "map-function": { "type": "Identity", "properties": {} },
                      "weight": 1
                    }"""))
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge added. No response body."),
            @ApiResponse(responseCode = "304", description = "An identical edge is already present; nothing changed."),
            @ApiResponse(responseCode = "404", description = "The parent or the child business service does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "BusinessServiceEntity with id 42"))),
            @ApiResponse(responseCode = "500", description = "The edge would form a loop.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Service will form a loop")))
    })
    // Add Child Service
    public Response addChildServiceEdge(
            @Parameter(description = "Parent business service id the edge is added to.", example = "23663", required = true)
            @PathParam("id") final Long serviceId,
                            final ChildEdgeRequestDTO edgeRequest) {
        final BusinessService parentService = getManager().getBusinessServiceById(serviceId);
        final BusinessService childService = getManager().getBusinessServiceById(edgeRequest.getChildId());
        boolean changed = getManager().addChildEdge(parentService, childService, transform(edgeRequest.getMapFunction()), edgeRequest.getWeight());
        if (!changed) {
            return Response.notModified().build();
        }
        parentService.save();
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}/edges/{edgeId}")
    @Operation(
            summary = "Remove an edge from a business service",
            description = """
        Delete one edge from the given business service. An edge that exists but belongs to a different
        service is left alone and answers 304; an edge id that does not exist at all is a 404. A
        successful response carries no body.""",
            operationId = "removeBusinessServiceEdge"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Edge removed. No response body."),
            @ApiResponse(responseCode = "304", description = "The edge does not belong to that business service; nothing changed."),
            @ApiResponse(responseCode = "404", description = "The business service or the edge does not exist.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "BusinessServiceEdgeEntity with id 42")))
    })
    public Response removeEdge(
            @Parameter(description = "Business service id owning the edge.", example = "23663", required = true)
            @PathParam("id") final Long serviceId,
            @Parameter(description = "Edge id to remove.", example = "23667", required = true)
            @PathParam("edgeId") final Long edgeId) {
        final BusinessService service = getManager().getBusinessServiceById(serviceId);
        final Edge edge = getManager().getEdgeById(edgeId);
        boolean changed = getManager().deleteEdge(service, edge);
        if (!changed) {
            return Response.notModified().build();
        }
        service.save();
        return Response.ok().build();
    }

    @POST
    @Path("daemon/reload")
    @Operation(
            summary = "Reload the BSM daemon",
            description = """
        Ask the BSM daemon to rebuild its in-memory graph from the database and recompute every
        operational status. Use it after editing business services outside this API. The call returns as
        soon as the reload is triggered and carries no body.""",
            operationId = "reloadBusinessServiceDaemon"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reload triggered. No response body.")
    })
    public Response reload() {
        getManager().triggerDaemonReload();
        return Response.ok().build();
    }

    @GET
    @Path("functions/map")
    @Operation(
            summary = "List map functions",
            description = """
        Return every available map function with its parameters. A map function transforms the severity
        an edge reports before the reduce function sees it. Use the `name` as the `type` of a
        `map-function` object, and the `parameter` keys as its `properties`.""",
            operationId = "listBusinessServiceMapFunctions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map function metadata returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = FunctionMetaListDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "function": [
                        { "type": "MapFunction", "name": "Identity", "description": "Use the status as is", "parameter": [] },
                        { "type": "MapFunction", "name": "Increase", "description": "Increase the status by one level", "parameter": [] },
                        { "type": "MapFunction", "name": "Ignore", "description": "Ignores the status", "parameter": [] },
                        {
                          "type": "MapFunction", "name": "SetTo", "description": "Sets the status to a defined value",
                          "parameter": [ { "key": "status", "type": "status", "description": "The status value to set the status to", "required": true } ]
                        },
                        { "type": "MapFunction", "name": "Decrease", "description": "Decreases the status by one level", "parameter": [] }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "204", description = "No map function is registered.")
    })
    public Response listMapFunctions() {
       return createFunctionMetaListDTO(new FunctionsManager().getMapFunctions(), FunctionType.MapFunction);
    }

    @GET
    @Path("functions/map/{name}")
    @Operation(
            summary = "Get one map function's metadata",
            description = "Return the parameters of one map function. The name is matched exactly and is case-sensitive.",
            operationId = "getBusinessServiceMapFunctionMetaData"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Map function metadata returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = FunctionMetaDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "type": "MapFunction",
                      "name": "SetTo",
                      "description": "Sets the status to a defined value",
                      "parameter": [ { "key": "status", "type": "status", "description": "The status value to set the status to", "required": true } ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No map function carries that name.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No map function with name 'NoSuch' found.")))
    })
    public Response getMapFunctionMetaData(
            @Parameter(description = "Map function name, case-sensitive.", example = "Identity", required = true,
                    schema = @Schema(allowableValues = {"Identity", "Increase", "Decrease", "Ignore", "SetTo"}))
            @PathParam("name") final String name) {
        FunctionMetaDTO metaData = new FunctionsManager().getMapFunctionMetaData(name);
        if (metaData == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No map function with name '" + name + "' found.").build();
        }
        return Response.ok().entity(metaData).build();
    }

    @GET
    @Path("functions/reduce")
    @Operation(
            summary = "List reduce functions",
            description = """
        Return every available reduce function with its parameters. A reduce function turns the mapped
        severities of a service's edges into the service's own operational status. Use the `name` as the
        `type` of a `reduce-function` object, and the `parameter` keys as its `properties`.""",
            operationId = "listBusinessServiceReduceFunctions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reduce function metadata returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = FunctionMetaListDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "function": [
                        { "type": "ReduceFunction", "name": "HighestSeverity", "description": "Uses the value of the highest severity", "parameter": [] },
                        {
                          "type": "ReduceFunction", "name": "HighestSeverityAbove",
                          "description": "Uses the highest severity greater than the given threshold severity",
                          "parameter": [ { "key": "threshold", "type": "status", "description": "The status value to use as threshold", "required": true } ]
                        },
                        {
                          "type": "ReduceFunction", "name": "ExponentialPropagation",
                          "description": "Propagate severities using a given base number",
                          "parameter": [ { "key": "base", "type": "double", "description": "The base used to calculate the required elements for propagation", "required": true } ]
                        },
                        {
                          "type": "ReduceFunction", "name": "Threshold",
                          "description": "Uses the highest severity found more often than the given threshold.",
                          "parameter": [ { "key": "threshold", "type": "float", "description": "The Threshold to use", "required": true } ]
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "204", description = "No reduce function is registered.")
    })
    public Response listReduceFunctions() {
        return createFunctionMetaListDTO(new FunctionsManager().getReduceFunctions(), FunctionType.ReduceFunction);
    }

    @GET
    @Path("functions/reduce/{name}")
    @Operation(
            summary = "Get one reduce function's metadata",
            description = "Return the parameters of one reduce function. The name is matched exactly and is case-sensitive.",
            operationId = "getBusinessServiceReduceFunctionMetaData"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reduce function metadata returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = FunctionMetaDTO.class),
                            examples = @ExampleObject(value = """
                    {
                      "type": "ReduceFunction",
                      "name": "Threshold",
                      "description": "Uses the highest severity found more often than the given threshold.",
                      "parameter": [ { "key": "threshold", "type": "float", "description": "The Threshold to use", "required": true } ]
                    }"""))),
            @ApiResponse(responseCode = "404", description = "No reduce function carries that name.",
                    content = @Content(schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "No reduce function with name 'NoSuch' found.")))
    })
    public Response getReduceFunctionMetaData(
            @Parameter(description = "Reduce function name, case-sensitive.", example = "HighestSeverity", required = true,
                    schema = @Schema(allowableValues = {"HighestSeverity", "HighestSeverityAbove", "ExponentialPropagation", "Threshold"}))
            @PathParam("name") final String name) {
        FunctionMetaDTO metaData = new FunctionsManager().getReduceFunctionMetaData(name);
        if (metaData == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No reduce function with name '" + name + "' found.").build();
        }
        return Response.ok().entity(metaData).build();
    }

    private Response createFunctionMetaListDTO(List<Class<?>> functions, FunctionType functionType) {
        if (functions == null || functions.isEmpty()) {
            return Response.noContent().build();
        }
        List<FunctionMetaDTO> functionList = functions.stream().map(functionMetaData -> new FunctionMetaDTO(functionMetaData, functionType)).collect(Collectors.toList());
        return Response.ok().entity(new FunctionMetaListDTO(functionList)).build();
    }

    private IpServiceResponseDTO transform(IpService input) {
        IpServiceResponseDTO response = new IpServiceResponseDTO();
        response.setId(input.getId());
        response.setNodeLabel(input.getNodeLabel());
        response.setServiceName(input.getServiceName());
        response.setIpAddress(input.getIpAddress());
        response.setLocation(ResourceLocationFactory.createIpServiceLocation(String.valueOf(input.getId())));
        return response;
    }

    private ApplicationResponseDTO transform(Application input) {
        ApplicationResponseDTO response = new ApplicationResponseDTO();
        response.setId(input.getId());
        response.setApplicationName(input.getApplicationName());
        return response;
    }

    private AbstractEdgeResponseDTO transform(Edge edge) {
        Objects.requireNonNull(edge);
        return edge.accept(new EdgeVisitor<AbstractEdgeResponseDTO>() {

            @Override
            public AbstractEdgeResponseDTO visit(IpServiceEdge edge) {
                return transform(edge);
            }

            @Override
            public AbstractEdgeResponseDTO visit(ReductionKeyEdge edge) {
                return transform(edge);
            }

            @Override
            public AbstractEdgeResponseDTO visit(ChildEdge edge) {
                return transform(edge);
            }

            @Override
            public AbstractEdgeResponseDTO visit(ApplicationEdge edge) {
                return transform(edge);
            }
        });
    }

    private ApplicationEdgeResponseDTO transform(ApplicationEdge edge) {
        final ApplicationEdgeResponseDTO response = new ApplicationEdgeResponseDTO();
        response.setId(edge.getId());
        response.setOperationalStatus(edge.getOperationalStatus());
        response.setReductionKeys(edge.getReductionKeys());
        response.setMapFunction(transform(edge.getMapFunction()));
        response.setWeight(edge.getWeight());
        response.setApplication(transform(edge.getApplication()));
        return response;
    }

    private IpServiceEdgeResponseDTO transform(IpServiceEdge edge) {
        final IpServiceEdgeResponseDTO response = new IpServiceEdgeResponseDTO();
        response.setId(edge.getId());
        response.setOperationalStatus(edge.getOperationalStatus());
        response.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(edge.getSource().getId(), edge.getId()));
        response.setReductionKeys(edge.getReductionKeys());
        response.setMapFunction(transform(edge.getMapFunction()));
        response.setWeight(edge.getWeight());
        response.setIpService(transform(edge.getIpService()));
        response.setFriendlyName(edge.getFriendlyName());
        return response;
    }

    private ChildEdgeResponseDTO transform(ChildEdge edge) {
        final ChildEdgeResponseDTO response = new ChildEdgeResponseDTO();
        response.setId(edge.getId());
        response.setChildId(edge.getChild().getId());
        response.setOperationalStatus(edge.getOperationalStatus());
        response.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(edge.getSource().getId(), edge.getId()));
        response.setReductionKeys(edge.getReductionKeys());
        response.setMapFunction(transform(edge.getMapFunction()));
        response.setWeight(edge.getWeight());
        return response;
    }

    private ReductionKeyEdgeResponseDTO transform(ReductionKeyEdge edge) {
        final ReductionKeyEdgeResponseDTO response = new ReductionKeyEdgeResponseDTO();
        response.setId(edge.getId());
        response.setOperationalStatus(edge.getOperationalStatus());
        response.setReductionKey(edge.getReductionKey());
        response.setLocation(ResourceLocationFactory.createBusinessServiceEdgeLocation(edge.getSource().getId(), edge.getId()));
        response.setReductionKeys(edge.getReductionKeys());
        response.setMapFunction(transform(edge.getMapFunction()));
        response.setWeight(edge.getWeight());
        response.setFriendlyName(edge.getFriendlyName());
        return response;
    }

    private MapFunction transform(MapFunctionDTO input) {
        return new FunctionsManager().getMapFunction(input);
    }

    private MapFunctionDTO transform(MapFunction input) {
       return new FunctionsManager().getMapFunctionDTO(input);
    }

    private ReduceFunctionDTO transform(ReductionFunction input) {
       return new FunctionsManager().getReduceFunctionDTO(input);
    }

    private ReductionFunction transform(ReduceFunctionDTO input) {
        return new FunctionsManager().getReduceFunction(input);
    }
}
