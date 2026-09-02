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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.support.CreateIfNecessaryTemplate;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsMetaDataList;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.RedirectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

/**
 * Basic Web Service using REST for {@link OnmsIpInterface} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class NodeMonitoredServiceRestService extends AbstractNodeDependentRestService<OnmsMonitoredService,OnmsMonitoredService,Integer,String> {

    private static final Logger LOG = LoggerFactory.getLogger(NodeMonitoredServiceRestService.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private MonitoredServicesComponent m_component;

    @Autowired
    private MonitoredServiceDao m_dao;

    @Override
    protected MonitoredServiceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoredService> getDaoClass() {
        return OnmsMonitoredService.class;
    }

    @Override
    protected Class<OnmsMonitoredService> getQueryBeanClass() {
        return OnmsMonitoredService.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());

        // 1st level JOINs
        builder.alias("ipInterface", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias("ipInterface.node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias("ipInterface.snmpInterface", Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        builder.alias("node.assetRecord", Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        builder.alias("node.categories", Aliases.category.toString(), JoinType.LEFT_JOIN);
        builder.alias("node.location", Aliases.location.toString(), JoinType.LEFT_JOIN);

        builder.orderBy("id");

        updateCriteria(uriInfo, builder);

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMonitoredService> createListWrapper(Collection<OnmsMonitoredService> list) {
        return new OnmsMonitoredServiceList(list);
    }

    @Override
    protected Response doCreate(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService service) {
        final OnmsIpInterface iface = getInterface(uriInfo);
        if (iface == null) {
            throw getException(Status.BAD_REQUEST, "IP interface was not found");
        } else if (service == null) {
            throw getException(Status.BAD_REQUEST, "Service object cannot be null");
        } else if (service.getServiceType() == null || service.getServiceType().getName() == null) {
            throw getException(Status.BAD_REQUEST, "Service type names cannot be null");
        }
        service.setServiceType(getServiceType(service.getServiceName()));
        service.setIpInterface(iface);
        iface.addMonitoredService(service);
        getDao().save(service);

        final Event e = EventUtils.createNodeGainedServiceEvent("ReST", iface.getNode().getId(), iface.getIpAddress(), service.getServiceName(), iface.getNode().getLabel(),
                                                                iface.getNode().getLabelSource(), iface.getNode().getSysName(), iface.getNode().getSysDescription());
        sendEvent(e);
        ApplicationEventUtil.getApplicationChangedEvents(service.getApplications()).forEach(this::sendEvent);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, service.getServiceName())).build();
    }

    @Override
    protected void updateCriteria(final UriInfo uriInfo, final CriteriaBuilder builder) {
        super.updateCriteria(uriInfo, builder);
        List<PathSegment> segments = uriInfo.getPathSegments(true);
        final String ipAddress =  segments.get(3).getPath(); // /nodes/{criteria}/ipinterfaces/{ipAddress}
        builder.eq("ipInterface.ipAddress", ipAddress);
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService targetObject, MultivaluedMapImpl params) {
        final String previousStatus = targetObject.getStatus();
        final Set<OnmsApplication> applicationsOriginal = new HashSet<>(); // unfortunately applications set is not immutable, let's make a copy.
        if(targetObject.getApplications() != null) {
            applicationsOriginal.addAll(targetObject.getApplications());
        }
        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);

        Set<OnmsApplication> changedApplications = Sets.symmetricDifference(applicationsOriginal, targetObject.getApplications());
        ApplicationEventUtil.getApplicationChangedEvents(changedApplications).forEach(this::sendEvent);

        boolean changed = m_component.hasStatusChanged(previousStatus, targetObject);
        return changed ? Response.noContent().build() : Response.notModified().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService svc) {
        svc.getIpInterface().getMonitoredServices().remove(svc);
        getDao().delete(svc);
        final Event e = EventUtils.createDeleteServiceEvent("ReST", svc.getNodeId(), svc.getIpAddress().getHostAddress(), svc.getServiceName(), -1L);
        sendEvent(e);
        ApplicationEventUtil.getApplicationChangedEvents(svc.getApplications()).forEach(this::sendEvent);
    }

    @Override
    protected OnmsMonitoredService doGet(UriInfo uriInfo, String serviceName) {
        final OnmsIpInterface iface = getInterface(uriInfo);
        return iface == null ? null : iface.getMonitoredServiceByServiceType(serviceName);
    }

    private OnmsServiceType getServiceType(final String serviceName) {
        final OnmsServiceType serviceType = new CreateIfNecessaryTemplate<OnmsServiceType, ServiceTypeDao>(m_transactionManager, m_serviceTypeDao) {
            @Override
            protected OnmsServiceType query() {
                return m_dao.findByName(serviceName);
            }
            @Override
            protected OnmsServiceType doInsert() {
                LOG.info("getServiceType: creating service type {}", serviceName);
                final OnmsServiceType s = new OnmsServiceType(serviceName);
                m_dao.saveOrUpdate(s);
                return s;
            }
        }.execute();
        return serviceType;
    }

    private OnmsIpInterface getInterface(final UriInfo uriInfo) {
        final OnmsNode node = getNode(uriInfo);
        final String ipAddress =  uriInfo.getPathSegments(true).get(3).getPath();
        return node == null ? null : node.getIpInterfaceByIpAddress(ipAddress);
    }

    protected OnmsMonitoredService getService(final UriInfo uriInfo, final String serviceName) {
        final var iface = getInterface(uriInfo);
        return iface == null? null : iface.getMonitoredServiceByServiceType(serviceName);
    }

    // The generic collection and item operations below are inherited from AbstractDaoRestServiceWithDTO.
    // They are overridden here only so that each concrete path carries its own OpenAPI documentation;
    // the bodies delegate unchanged.
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get the monitored services of an IP interface",
            description = """
        Return the monitored services of the IP interface named in the path. The criteria are restricted
        to that node and address, so `_s` narrows within the interface. `limit` defaults to 10.

        The criteria join the owning node's categories without a `distinct`, so a node carrying more
        than one surveillance category yields the same service once per category. `totalCount` and
        `GET .../services/count` are inflated by the same factor.

        `status` holds the single-letter poller status and `statusLong` its label. `lastGood` and
        `lastFail` are epoch milliseconds in JSON, not the `string/date-time` the derived schema
        shows.

        Example query: `_s=status==A&orderBy=id`.""",
            operationId = "NodeMonitoredServiceRestServiceGETServices",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`. A value that is neither is answered with 500.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching monitored services.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMonitoredServiceList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "service": [
                        {
                          "id": 23618,
                          "ipInterfaceId": 23615,
                          "serviceType": {"id": 6, "name": "ICMP"},
                          "status": "A",
                          "statusLong": "Managed",
                          "down": false,
                          "source": null,
                          "qualifier": null,
                          "notify": null,
                          "lastGood": null,
                          "lastFail": null
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMonitoredServiceList.class),
                                    examples = @ExampleObject(value = """
                    <services count="1" offset="0" totalCount="1">
                      <service down="false" status="A" statusLong="Managed" id="23618">
                        <ipInterfaceId>23615</ipInterfaceId>
                        <serviceType id="6"><name>ICMP</name></serviceType>
                      </service>
                    </services>"""))
                    }),
            @ApiResponse(responseCode = "204", description = "The interface has no matching service. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response get(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @GET
    @Path("count")
    @Produces({MediaType.TEXT_PLAIN})
    @Operation(
            summary = "Count the monitored services of an IP interface",
            description = """
        Return the number of the interface's services matching `_s` as a bare decimal string. The count
        carries the same category-join inflation as the collection: a node with two categories reports
        each service twice.

        Example query: `_s=status==A`.""",
            operationId = "NodeMonitoredServiceRestServiceGETServiceCount",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Number of matching services.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "2"))),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response getCount(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @GET
    @Path("properties")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get monitored service search properties",
            description = """
        This resource declares no search property set, so the operation answers 204 with no body. FIQL
        expressions still work on this resource; they are validated against the entity by Hibernate
        rather than against a property list.""",
            operationId = "NodeMonitoredServiceRestServiceGETServiceSearchProperties",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponse(responseCode = "204", description = "No search properties are declared for this resource. No body is returned.")
    @Override
    public Response getProperties(
            @Parameter(in = ParameterIn.QUERY, name = "q",
                    description = "Case-insensitive substring matched against the property `name`, not its id. Has no effect while the property set is empty.", example = "Status")
            @QueryParam("q") final String query) {
        return super.getProperties(query);
    }

    @GET
    @Path("properties/{propertyId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Get values of a monitored service search property",
            description = """
        Answered with 404 for every `propertyId`, because this resource declares no search property
        set.""",
            operationId = "NodeMonitoredServiceRestServiceGETServiceSearchPropertyValues",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponse(responseCode = "404", description = "No search property has that id. No body is returned.")
    @Override
    public Response getPropertyValues(
            @Parameter(in = ParameterIn.PATH, name = "propertyId", description = "Property id.", example = "status")
            @PathParam("propertyId") final String propertyId,
            @Parameter(in = ParameterIn.QUERY, name = "q", description = "Substring the value must contain.", example = "A")
            @QueryParam("q") final String query,
            @Parameter(in = ParameterIn.QUERY, name = "limit", description = "Maximum number of values to return.", example = "10")
            @QueryParam("limit") final Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get a monitored service",
            description = """
        Return one monitored service of the interface, addressed by service name rather than by its
        database id. The name is matched exactly.""",
            operationId = "NodeMonitoredServiceRestServiceGETServiceByName",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The monitored service.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMonitoredService.class),
                                    examples = @ExampleObject(value = """
                    {
                      "id": 23618,
                      "ipInterfaceId": 23615,
                      "serviceType": {"id": 6, "name": "ICMP"},
                      "status": "A",
                      "statusLong": "Managed",
                      "down": false,
                      "source": null,
                      "qualifier": null,
                      "notify": null,
                      "lastGood": null,
                      "lastFail": null
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMonitoredService.class),
                                    examples = @ExampleObject(value = """
                    <service down="false" status="A" statusLong="Managed" id="23618">
                      <ipInterfaceId>23615</ipInterfaceId>
                      <serviceType id="6"><name>ICMP</name></serviceType>
                    </service>"""))
                    }),
            @ApiResponse(responseCode = "404", description = "The interface has no service with that name. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response get(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "IP address of the owning interface, then the service name. The two path templates are both named `id`.",
                    example = "ICMP")
            @PathParam("id") final String id) {
        return super.get(uriInfo, id);
    }

    @POST
    @Path("{id}")
    @Operation(
            summary = "Rejected: create a monitored service at a caller-chosen path",
            description = "Always answered with 404, whether or not the service exists.",
            operationId = "NodeMonitoredServiceRestServicePOSTServiceSpecific",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria", required = true,
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id", required = true,
                            description = "IP address of the owning interface, then the service name. The two path templates are both named `id`, and neither value is read: every request to this path is answered with 404.",
                            example = "ICMP")
            })
    @ApiResponse(responseCode = "404", description = "Not supported. No body is returned.")
    @Override
    public Response createSpecific() {
        return super.createSpecific();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Operation(
            summary = "Add a monitored service to an IP interface",
            description = """
        Attach a monitored service to the interface and send a `nodeGainedService` event, plus an
        application-changed event for each application named in the body. `serviceType.name` is
        required, and a service type that does not yet exist is created. `status` in the body is applied
        as given; omitting it stores the service with no status, so an immediate read-back can show
        `status` and `statusLong` as `null`. The new service's URI is returned in the `Location`
        header.""",
            operationId = "NodeMonitoredServiceRestServicePOSTService",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @RequestBody(required = true, description = "The monitored service to add.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMonitoredService.class),
                            examples = @ExampleObject(value = """
                    {"serviceType": {"name": "ICMP"}}""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsMonitoredService.class),
                            examples = @ExampleObject(value = """
                    <service><serviceType><name>ICMP</name></serviceType></service>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The service was added. `Location` carries its URI.",
                    headers = @Header(name = "Location", description = "URI of the created monitored service.",
                            schema = @Schema(type = "string", example = "http://localhost:8980/opennms/api/v2/nodes/257/ipinterfaces/192.0.2.31/services/ICMP"))),
            @ApiResponse(responseCode = "400", description = "The interface was not found, or the body carried no service type name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "no service type", value = "Service type names cannot be null"),
                                    @ExampleObject(name = "no interface", value = "IP interface was not found")
                            })),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response create(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, final OnmsMonitoredService object) {
        return super.create(securityContext, uriInfo, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Update properties of several monitored services",
            description = """
        Apply the form parameters as bean properties to every service of the interface matching `_s`.
        The default `limit` of 10 applies to the selection. A change to `status` sends the matching
        service-status event, and a change to `applications` sends an application-changed event. The
        whole call runs in one transaction, so a per-service failure aborts the batch.

        Example query: `_s=status==N&limit=100`.""",
            operationId = "NodeMonitoredServiceRestServicePUTServices",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @RequestBody(required = true, description = "Monitored service bean properties to set, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "status=A")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "All selected services were updated."),
            @ApiResponse(responseCode = "404", description = "No service matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response updateMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext, final MultivaluedMapImpl params) {
        return super.updateMany(securityContext, uriInfo, searchContext, params);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{id}")
    @Operation(
            summary = "Not implemented: replace a monitored service from a document",
            description = """
        Answered with 501. This variant binds `{id}` as an integer while the collection addresses
        services by name, so a name in the path is answered with 404 before the handler runs.""",
            operationId = "NodeMonitoredServiceRestServicePUTServiceDocument",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(description = "Ignored.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OnmsMonitoredService.class),
                            examples = @ExampleObject(value = "{\"status\": \"A\"}")),
                    @Content(mediaType = MediaType.APPLICATION_XML, schema = @Schema(implementation = OnmsMonitoredService.class),
                            examples = @ExampleObject(value = "<service status=\"A\"/>"))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "`{id}` is not an integer, which is the case for every service name."),
            @ApiResponse(responseCode = "501", description = "Replacing a monitored service from a document is not implemented. No body is returned.")
    })
    @Override
    public Response update(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "Bound as an integer here, unlike every other operation on this path.", example = "23618")
            @PathParam("id") final Integer id,
            final OnmsMonitoredService object) {
        return super.update(securityContext, uriInfo, id, object);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{id}")
    @Operation(
            summary = "Update properties of a monitored service",
            description = """
        Apply the form parameters as bean properties to one service of the interface. Setting `status`
        to `A` resumes polling and `F` forces the service out of service. A body that leaves the status
        unchanged is answered with 304. A change to `applications` sends an application-changed
        event.""",
            operationId = "NodeMonitoredServiceRestServicePUTServiceProperties",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @RequestBody(required = true, description = "Monitored service bean properties to set, form-encoded.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "object"),
                    examples = @ExampleObject(value = "status=F")))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The service status changed."),
            @ApiResponse(responseCode = "304", description = "The service was written but its status is unchanged. No body is returned."),
            @ApiResponse(responseCode = "404", description = "The interface has no service with that name. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response updateProperties(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "IP address of the owning interface, then the service name.", example = "ICMP")
            @PathParam("id") final String id,
            final MultivaluedMapImpl params) {
        return super.updateProperties(securityContext, uriInfo, id, params);
    }

    @DELETE
    @Operation(
            summary = "Delete several monitored services",
            description = """
        Delete every service of the interface matching `_s` and send a `deleteService` event for each,
        plus an application-changed event per affected application. The default `limit` of 10 applies to
        the selection.

        Example query: `_s=status==N&limit=100`.""",
            operationId = "NodeMonitoredServiceRestServiceDELETEServices",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The selected services were deleted."),
            @ApiResponse(responseCode = "404", description = "No service matched. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment could not be parsed, or the FIQL expression could not be parsed or resolved.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Error parsing FIQL search")))
    })
    @Override
    public Response deleteMany(@Context final SecurityContext securityContext, @Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        return super.deleteMany(securityContext, uriInfo, searchContext);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete a monitored service",
            description = """
        Delete one service of the interface and send a `deleteService` event, plus an
        application-changed event per application the service belonged to. The service type itself is
        left in place even when no service references it any more.""",
            operationId = "NodeMonitoredServiceRestServiceDELETEServiceByName",
            parameters = @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The service was deleted."),
            @ApiResponse(responseCode = "404", description = "The interface has no service with that name. No body is returned."),
            @ApiResponse(responseCode = "500", description = "The node path segment is neither a number nor `foreignSource:foreignId`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    @Override
    public Response delete(
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "id",
                    description = "IP address of the owning interface, then the service name.", example = "ICMP")
            @PathParam("id") final String id) {
        return super.delete(securityContext, uriInfo, id);
    }

    @GET
    @Path("{serviceName}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get all metadata of a monitored service",
            description = """
        Return every metadata entry attached to the service, across all contexts. An empty result is
        still a 200; `totalCount` and `count` are then `null` rather than `0`. A service name that does
        not exist on the interface is answered with 500 rather than 400.""",
            operationId = "NodeMonitoredServiceRestServiceGetMetaDataByService",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metadata entries of the service.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = @ExampleObject(value = """
                    {
                      "totalCount": 1,
                      "count": 1,
                      "offset": 0,
                      "metaData": [{"context": "X-ApiDoc", "key": "sla", "value": "gold"}]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMetaDataList.class),
                                    examples = @ExampleObject(value = """
                    <meta-data-list count="1" offset="0" totalCount="1">
                      <meta-data><context>X-ApiDoc</context><key>sla</key><value>gold</value></meta-data>
                    </meta-data-list>"""))
                    }),
            @ApiResponse(responseCode = "500", description = "The interface has no service with that name, or the node path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.model.OnmsMonitoredService.getMetaData()\" because \"service\" is null")))
    })
    public OnmsMetaDataList getMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "serviceName",
                    description = "Service name.", example = "ICMP")
            @PathParam("serviceName") String serviceName) {
        final OnmsMonitoredService service = getService(uriInfo, serviceName);

        if (serviceName == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find service " + serviceName);
        }

        return new OnmsMetaDataList(service.getMetaData());
    }

    @GET
    @Path("{serviceName}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get metadata of a monitored service in one context",
            description = """
        Return the service's metadata entries whose context matches exactly, case-sensitively. A context
        that holds no entries and a context that does not exist both give a 200 with an empty list.""",
            operationId = "NodeMonitoredServiceRestServiceGetMetaDataByServiceAndContext",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metadata entries in that context, possibly empty.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaDataList.class),
                            examples = {
                                    @ExampleObject(name = "match", value = """
                    {"totalCount": 1, "count": 1, "offset": 0, "metaData": [{"context": "X-ApiDoc", "key": "sla", "value": "gold"}]}"""),
                                    @ExampleObject(name = "no match", value = """
                    {"totalCount": null, "count": null, "offset": 0, "metaData": []}""")
                            })),
            @ApiResponse(responseCode = "500", description = "The interface has no service with that name, or the node path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public OnmsMetaDataList getMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "serviceName",
                    description = "Service name.", example = "ICMP")
            @PathParam("serviceName") String serviceName,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context to filter on.", example = "X-ApiDoc")
            @PathParam("context") String context) {
        final OnmsMonitoredService service = getService(uriInfo, serviceName);

        if (serviceName == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find service " + serviceName);
        }

        return new OnmsMetaDataList(service.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()))
                .collect(Collectors.toList()));
    }

    @GET
    @Path("{serviceName}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get one metadata entry of a monitored service",
            description = """
        Return the service's metadata entries matching both context and key. At most one entry can
        match, but the response is still the list envelope.""",
            operationId = "NodeMonitoredServiceRestServiceGetMetaDataByServiceAndContextAndKey",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The matching entry, or an empty list.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaDataList.class),
                            examples = @ExampleObject(value = """
                    {"totalCount": 1, "count": 1, "offset": 0, "metaData": [{"context": "X-ApiDoc", "key": "sla", "value": "gold"}]}"""))),
            @ApiResponse(responseCode = "500", description = "The interface has no service with that name, or the node path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public OnmsMetaDataList getMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "serviceName",
                    description = "Service name.", example = "ICMP")
            @PathParam("serviceName") String serviceName,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context.", example = "X-ApiDoc")
            @PathParam("context") String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key within the context.", example = "sla")
            @PathParam("key") String key) {
        final OnmsMonitoredService service = getService(uriInfo, serviceName);

        if (serviceName == null) {
            throw getException(Status.BAD_REQUEST, "getMetaData: Can't find service " + serviceName);
        }

        return new OnmsMetaDataList(service.getMetaData().stream()
                .filter(e -> context.equals(e.getContext()) && key.equals(e.getKey()))
                .collect(Collectors.toList()));
    }

    @DELETE
    @Path("{serviceName}/metadata/{context}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete a metadata context of a monitored service",
            description = """
        Remove every metadata entry of the service in the given context. Deleting a context that holds
        no entries is also a 204. The context is checked before the service is looked up, so a non-`X-`
        context is answered with 403 even for a service that does not exist.""",
            operationId = "NodeMonitoredServiceRestServiceDELETEMetaDataByServiceAndContext",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The context was removed."),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`. Only user-defined contexts may be written through this API.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The interface has no service with that name, or the node path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response deleteMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "serviceName",
                    description = "Service name.", example = "ICMP")
            @PathParam("serviceName") final String serviceName,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context to remove. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsMonitoredService service = getService(uriInfo, serviceName);

            if (serviceName == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find service " + serviceName);
            }
            service.removeMetaData(context);
            getDao().update(service);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{serviceName}/metadata/{context}/{key}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Delete one metadata entry of a monitored service",
            description = """
        Remove a single metadata entry of the service. Deleting a key that does not exist is also a
        204.""",
            operationId = "NodeMonitoredServiceRestServiceDELETEMetaDataByServiceAndContextAndKey",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was removed."),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The interface has no service with that name, or the node path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response deleteMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "serviceName",
                    description = "Service name.", example = "ICMP")
            @PathParam("serviceName") final String serviceName,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key to remove.", example = "sla")
            @PathParam("key") final String key) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsMonitoredService service = getService(uriInfo, serviceName);

            if (serviceName == null) {
                throw getException(Status.BAD_REQUEST, "deleteMetaData: Can't find service " + serviceName);
            }
            service.removeMetaData(context, key);
            getDao().update(service);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @POST
    @Path("{serviceName}/metadata")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Add or replace a metadata entry of a monitored service",
            description = """
        Set one metadata entry on the service from the request body. An existing entry with the same
        context and key is overwritten. No `@Consumes` is declared, so both JSON and XML bodies are
        accepted; the XML root element is `meta-data`.""",
            operationId = "NodeMonitoredServiceRestServicePOSTMetaDataByService",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @RequestBody(required = true, description = "The metadata entry to set. The context must start with `X-`.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMetaData.class),
                            examples = @ExampleObject(value = """
                    {"context": "X-ApiDoc", "key": "sla", "value": "gold"}""")),
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsMetaData.class),
                            examples = @ExampleObject(value = """
                    <meta-data><context>X-ApiDoc</context><key>sla</key><value>gold</value></meta-data>"""))
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was stored."),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The interface has no service with that name, the node path segment could not be parsed, or the body could not be deserialised.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response postMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "serviceName",
                    description = "Service name.", example = "ICMP")
            @PathParam("serviceName") final String serviceName,
            final OnmsMetaData entity) {
        checkUserDefinedMetadataContext(entity.getContext());

        writeLock();
        try {
            final OnmsMonitoredService service = getService(uriInfo, serviceName);

            if (serviceName == null) {
                throw getException(Status.BAD_REQUEST, "postMetaData: Can't find service " + serviceName);
            }
            service.addMetaData(entity.getContext(), entity.getKey(), entity.getValue());
            getDao().update(service);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{serviceName}/metadata/{context}/{key}/{value}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Set a metadata entry of a monitored service from the path",
            description = """
        Set one metadata entry on the service with context, key and value all taken from the path. An
        existing entry with the same context and key is overwritten. A value containing `/` has to be
        percent-encoded, and an empty value cannot be expressed this way.""",
            operationId = "NodeMonitoredServiceRestServicePUTMetaDataByService",
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "nodeCriteria",
                            description = "Node database id, or `foreignSource:foreignId`.", example = "257"),
                    @Parameter(in = ParameterIn.PATH, name = "id",
                            description = "IP address of the owning interface.", example = "192.0.2.31")
            })
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry was stored."),
            @ApiResponse(responseCode = "403", description = "The context does not start with `X-`.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Only metadata in contexts starting with 'X-' can be modified"))),
            @ApiResponse(responseCode = "500", description = "The interface has no service with that name, or the node path segment could not be parsed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "For input string: \"notanumber\"")))
    })
    public Response putMetaData(
            @Context final UriInfo uriInfo,
            @Parameter(in = ParameterIn.PATH, name = "serviceName",
                    description = "Service name.", example = "ICMP")
            @PathParam("serviceName") final String serviceName,
            @Parameter(in = ParameterIn.PATH, name = "context",
                    description = "Metadata context. Must start with `X-`.", example = "X-ApiDoc")
            @PathParam("context") final String context,
            @Parameter(in = ParameterIn.PATH, name = "key",
                    description = "Metadata key.", example = "sla")
            @PathParam("key") final String key,
            @Parameter(in = ParameterIn.PATH, name = "value",
                    description = "Metadata value.", example = "gold")
            @PathParam("value") final String value) {
        checkUserDefinedMetadataContext(context);

        writeLock();
        try {
            final OnmsMonitoredService service = getService(uriInfo, serviceName);

            if (serviceName == null) {
                throw getException(Status.BAD_REQUEST, "putMetaData: Can't find service " + serviceName);
            }
            service.addMetaData(context, key, value);
            getDao().update(service);
            return Response.noContent().build();
        } finally {
            writeUnlock();
        }
    }
}
