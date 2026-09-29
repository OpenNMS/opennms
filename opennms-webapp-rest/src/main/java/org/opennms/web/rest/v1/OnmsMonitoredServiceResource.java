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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.support.CreateIfNecessaryTemplate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoredServiceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.api.RestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>OnmsMonitoredServiceResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@Component("onmsMonitoredServiceResource")
@Transactional
public class OnmsMonitoredServiceResource extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(OnmsMonitoredServiceResource.class);

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;
    
    @Autowired
    private MonitoredServiceDao m_serviceDao;
    
    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;
    
    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    /**
     * <p>getServices</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredServiceList} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "List the monitored services on one IP interface",
            description = """
                    Returns every monitored service on the interface. The whole set is returned; this endpoint
                    takes no paging or filter parameters, so `offset` is always 0 and `count` equals `totalCount`.

                    `status` is the stored one-character poller state and `statusLong` its label: `A` Managed,
                    `N` Not Monitored, `U` Unmanaged, `D` Deleted, `F` Forced Unmanaged, `R` Rescan to Resume,
                    `S` Rescan to Suspend, `X` Remotely Monitored.""",
            operationId = "listInterfaceServices"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The interface's monitored services.",
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
                          "source": null,
                          "qualifier": null,
                          "status": "A",
                          "down": false,
                          "notify": null,
                          "lastGood": null,
                          "lastFail": null,
                          "serviceType": { "id": 1, "name": "ICMP" },
                          "statusLong": "Managed",
                          "ipInterfaceId": 23601,
                          "id": 23616
                        }
                      ]
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMonitoredServiceList.class),
                                    examples = @ExampleObject(value = """
                    <services count="1" offset="0" totalCount="1">
                      <service down="false" status="A" statusLong="Managed" id="23616">
                        <ipInterfaceId>23601</ipInterfaceId>
                        <serviceType id="1">
                          <name>ICMP</name>
                        </serviceType>
                      </service>
                    </services>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or the node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found."),
                                    @ExampleObject(name = "unknownInterface", value = "IP Interface 192.0.2.99 was not found on node 258.")
                            }))
    })
    public OnmsMonitoredServiceList getServices(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                                @PathParam("nodeCriteria") String nodeCriteria,
                                                @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                                                @PathParam("ipAddress") String ipAddress) {
        OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddress);
        if (iface == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
        }
        return new OnmsMonitoredServiceList(iface.getMonitoredServices());
    }

    /**
     * <p>getService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("{service}")
    @Operation(
            summary = "Get one monitored service by service name",
            description = """
                    Returns the monitored service with the given service name on the interface. The path segment
                    is the service *name*, such as `ICMP` or `HTTP`, not the numeric service id used by
                    `/ifservices/{id}`. Matching is exact and case-sensitive.""",
            operationId = "getInterfaceService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The monitored service.",
                    content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = OnmsMonitoredService.class),
                                    examples = @ExampleObject(value = """
                    {
                      "source": null,
                      "qualifier": null,
                      "status": "A",
                      "down": false,
                      "notify": null,
                      "lastGood": null,
                      "lastFail": null,
                      "serviceType": { "id": 1, "name": "ICMP" },
                      "statusLong": "Managed",
                      "ipInterfaceId": 23601,
                      "id": 23616
                    }""")),
                            @Content(mediaType = MediaType.APPLICATION_XML,
                                    schema = @Schema(implementation = OnmsMonitoredService.class),
                                    examples = @ExampleObject(value = """
                    <service down="false" status="A" statusLong="Managed" id="23616">
                      <ipInterfaceId>23601</ipInterfaceId>
                      <serviceType id="1">
                        <name>ICMP</name>
                      </serviceType>
                    </service>"""))
                    }),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or the node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "IP Interface 192.0.2.99 was not found on node 258."))),
            @ApiResponse(responseCode = "404", description = "The interface carries no service with that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Monitored Service NOSUCH was not found on IP Interface 192.0.2.10 and node 258.")))
    })
    public OnmsMonitoredService getService(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                           @PathParam("nodeCriteria") String nodeCriteria,
                                           @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                                           @PathParam("ipAddress") String ipAddress,
                                           @Parameter(description = "Service name, as it appears in `serviceType.name`.", example = "ICMP")
                                           @PathParam("service") String service) {
        final OnmsNode node = m_nodeDao.get(nodeCriteria);
        if (node == null) {
            throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
        }
        final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddress);
        if (iface == null) {
            throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
        }
        final OnmsMonitoredService svc = iface.getMonitoredServiceByServiceType(service);
        if (svc == null) {
            throw getException(Status.NOT_FOUND, "Monitored Service {} was not found on IP Interface {} and node {}.", service, ipAddress, nodeCriteria);
        }
        return svc;
    }
    
    /**
     * <p>addService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param service a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
            summary = "Add a monitored service to an IP interface",
            description = """
                    Attaches a monitored service to the interface and publishes `nodeGainedService`.

                    The service type is looked up by `serviceType.name` and created if it does not exist. A
                    service type created this way stays in the `service` table after the monitored service is
                    deleted.

                    Both XML and JSON bodies are accepted; a form-encoded body is rejected with 415. In XML
                    `status` is an attribute on `service` and `serviceType` is a nested element; in JSON both are
                    plain fields. `serviceType` is required: a body without it fails with 500, not 400.

                    The `status` sent in the body is stored, and pollerd can change it immediately afterwards.""",
            operationId = "addInterfaceService"
    )
    @RequestBody(
            required = true,
            description = "The service to add. `serviceType.name` is required.",
            content = {
                    @Content(mediaType = MediaType.APPLICATION_XML,
                            schema = @Schema(implementation = OnmsMonitoredService.class),
                            examples = @ExampleObject(value = """
                    <service status="A">
                      <serviceType>
                        <name>ICMP</name>
                      </serviceType>
                    </service>""")),
                    @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = OnmsMonitoredService.class),
                            examples = @ExampleObject(value = """
                    {
                      "status": "A",
                      "serviceType": { "name": "ICMP" }
                    }"""))
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created. `Location` points at the service, keyed by name."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or the node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = {
                                    @ExampleObject(name = "unknownNode", value = "Node 999999 was not found."),
                                    @ExampleObject(name = "unknownInterface", value = "IP Interface 192.0.2.99 was not found on node 258.")
                            })),
            @ApiResponse(responseCode = "415", description = "The body was form-encoded. Send XML or JSON."),
            @ApiResponse(responseCode = "500", description = "The body carried no `serviceType`, or publishing `nodeGainedService` failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot invoke \"org.opennms.netmgt.model.OnmsServiceType.getName()\" because the return value of \"org.opennms.netmgt.model.OnmsMonitoredService.getServiceType()\" is null")))
    })
    public Response addService(@Context final UriInfo uriInfo,
                               @Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                               @PathParam("nodeCriteria") final String nodeCriteria,
                               @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                               @PathParam("ipAddress") final String ipAddress, final OnmsMonitoredService service) {
        writeLock();
        
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            final OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
            if (intf == null) throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
            if (service == null) throw getException(Status.BAD_REQUEST, "Service object cannot be null");
            if (service.getServiceName() == null) throw getException(Status.BAD_REQUEST, "Service must have a name");

            final OnmsServiceType serviceType = new CreateIfNecessaryTemplate<OnmsServiceType, ServiceTypeDao>(m_transactionManager, m_serviceTypeDao) {
                @Override
                protected OnmsServiceType query() {
                    return m_dao.findByName(service.getServiceName());
                }

                @Override
                protected OnmsServiceType doInsert() {
                    LOG.info("addService: creating service type {}", service.getServiceName());
                    final OnmsServiceType s = new OnmsServiceType(service.getServiceName());
                    m_dao.saveOrUpdate(s);
                    return s;
                }
            }.execute();

            service.setServiceType(serviceType);
            service.setIpInterface(intf);
            LOG.debug("addService: adding service {}", service);
            m_serviceDao.save(service);
            
            Event e = EventUtils.createNodeGainedServiceEvent("ReST", node.getId(), intf.getIpAddress(), 
                    service.getServiceName(), node.getLabel(), node.getLabelSource(), node.getSysName(), node.getSysDescription());
            sendEvent(e);

            return Response.created(getRedirectUri(uriInfo, service.getServiceName())).build();
        } finally {
            writeUnlock();
        }
    }
    
    /**
     * <p>updateService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param params a {@link org.opennms.web.rest.support.MultivaluedMapImpl} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("{service}")
    @Operation(
            summary = "Update one monitored service",
            description = """
                    Applies form-encoded fields to one monitored service. Keys are bean property names on
                    `OnmsMonitoredService`; `id`, `dbId`, `nodeId`, `authorizedGroups`, `foreignSource`,
                    `foreignId` and `type` are protected and dropped with a log warning, and unresolvable keys are
                    ignored, so a request naming only such keys comes back 304.

                    `status` is handled specially. `S`, and a move from `A` to `F`, store `F` and publish
                    `suspendPollingService`. `R`, and a move from `F` to `A`, store `A` and publish
                    `resumePollingService`. So `R` and `S` are request codes that are never themselves stored.""",
            operationId = "updateInterfaceService"
    )
    @RequestBody(
            required = true,
            description = "Form-encoded service properties to set.",
            content = @Content(mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(type = "string"),
                    examples = {
                            @ExampleObject(name = "suspend", summary = "Stop polling the service",
                                    value = "status=F"),
                            @ExampleObject(name = "resume", summary = "Resume polling the service",
                                    value = "status=A")
                    })
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "At least one field was written. No body."),
            @ApiResponse(responseCode = "304", description = "No key in the body resolved to a writable, unprotected property."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, the node has no interface with that address, or the interface carries no service with that name.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Monitored Service NOSUCH was not found on IP Interface 192.0.2.10 and node 258."))),
            @ApiResponse(responseCode = "500", description = "Publishing the suspend or resume event failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot send event uei.opennms.org/internal/poller/suspendPollingService : connection refused")))
    })
    public Response updateService(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                  @PathParam("nodeCriteria") String nodeCriteria,
                                  @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                                  @PathParam("ipAddress") String ipAddress,
                                  @Parameter(description = "Service name, as it appears in `serviceType.name`.", example = "ICMP")
                                  @PathParam("service") String serviceName, MultivaluedMapImpl params) {
        writeLock();
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
            if (intf == null) throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
            OnmsMonitoredService service = intf.getMonitoredServiceByServiceType(serviceName);
            if (service == null) throw getException(Status.BAD_REQUEST, "Monitored Service {} was not found on IP Interface {} and node {}.", serviceName, ipAddress, nodeCriteria);
    
            LOG.debug("updateService: updating service {}", service);
            boolean modified = false;
            BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(service);
            for(String key : params.keySet()) {
                if (RestUtils.isProtectedProperty(key)) {
                    LOG.warn("Ignoring attempt to set protected property '{}'", key);
                    continue;
                }
                if (wrapper.isWritableProperty(key)) {
                    String stringValue = params.getFirst(key);
                    Object value = wrapper.convertIfNecessary(stringValue, (Class<?>)wrapper.getPropertyType(key));
                    if (key.equals("status")) {
                        if ("S".equals(value) || ("A".equals(service.getStatus()) && "F".equals(value))) {
                            LOG.debug("updateService: suspending polling for service {} on node with IP {}", service.getServiceName(), service.getIpAddress().getHostAddress());
                            value = "F";
                            sendEvent(EventConstants.SUSPEND_POLLING_SERVICE_EVENT_UEI, service);
                        }
                        if ("R".equals(value) || ("F".equals(service.getStatus()) && "A".equals(value))) {
                            LOG.debug("updateService: resuming polling for service {} on node with IP {}", service.getServiceName(), service.getIpAddress().getHostAddress());
                            value = "A";
                            sendEvent(EventConstants.RESUME_POLLING_SERVICE_EVENT_UEI, service);
                        }
                    }
                    wrapper.setPropertyValue(key, value);
                    modified = true;
                }
            }
            if (modified) {
                LOG.debug("updateSservice: service {} updated", service);
                m_serviceDao.saveOrUpdate(service);
                return Response.noContent().build();
            }
            return Response.notModified().build();
        } finally {
            writeUnlock();
        }
    }

    /**
     * <p>deleteService</p>
     *
     * @param nodeCriteria a {@link java.lang.String} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link javax.ws.rs.core.Response} object.
     */
    @DELETE
    @Path("{service}")
    @Operation(
            summary = "Delete one monitored service",
            description = """
                    Publishes `deleteService` and returns immediately. The deletion is carried out asynchronously,
                    so the service is normally still readable for a moment after the 202.

                    The service type row itself is left in place, so a service name introduced by a POST here
                    stays defined after its last monitored service is gone.""",
            operationId = "deleteInterfaceService"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "The delete request was published. No body. Completion is not confirmed."),
            @ApiResponse(responseCode = "400", description = "No node matches `nodeCriteria`, or the node has no interface with that address.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "IP Interface 192.0.2.99 was not found on node 258."))),
            @ApiResponse(responseCode = "409", description = "The interface carries no service with that name. This path reports the missing service as a conflict, not a 404.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Monitored Service NOSUCH was not found on IP Interface 192.0.2.10 and node 258."))),
            @ApiResponse(responseCode = "500", description = "Publishing `deleteService` failed.",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Cannot send event uei.opennms.org/nodes/deleteService : connection refused")))
    })
    public Response deleteService(@Parameter(description = "Node identifier: either the database node id or `foreignSource:foreignId`. Both forms are accepted.", example = "Router-Requisition:node1")
                                  @PathParam("nodeCriteria") final String nodeCriteria,
                                  @Parameter(description = "Literal IPv4 or IPv6 address of the interface.", example = "192.0.2.10")
                                  @PathParam("ipAddress") final String ipAddress,
                                  @Parameter(description = "Service name, as it appears in `serviceType.name`.", example = "ICMP")
                                  @PathParam("service") final String serviceName) {
        writeLock();
        
        try {
            OnmsNode node = m_nodeDao.get(nodeCriteria);
            if (node == null) throw getException(Status.BAD_REQUEST, "Node {} was not found.", nodeCriteria);
            OnmsIpInterface intf = node.getIpInterfaceByIpAddress(ipAddress);
            if (intf == null) throw getException(Status.BAD_REQUEST, "IP Interface {} was not found on node {}.", ipAddress, nodeCriteria);
            OnmsMonitoredService service = intf.getMonitoredServiceByServiceType(serviceName);
            if (service == null) throw getException(Status.CONFLICT, "Monitored Service {} was not found on IP Interface {} and node {}.", serviceName, ipAddress, nodeCriteria);
            LOG.debug("deleteService: deleting service {} from node {}", serviceName, nodeCriteria);

            Event e = EventUtils.createDeleteServiceEvent("OpenNMS.REST", node.getId(), ipAddress, serviceName, -1L);
            sendEvent(e);

            return Response.accepted().build();
        } finally {
            writeUnlock();
        }
    }

    private void sendEvent(String eventUEI, OnmsMonitoredService dbObj) {
        final EventBuilder bldr = new EventBuilder(eventUEI, "ReST");
        bldr.setNodeid(dbObj.getNodeId());
        bldr.setInterface(dbObj.getIpAddress());
        bldr.setService(dbObj.getServiceName());
        sendEvent(bldr.getEvent());
    }

    private void sendEvent(Event event) {
        try {
            m_eventProxy.send(event);
        } catch (final EventProxyException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}", event.getUei(), e.getMessage());
        }
    }

}
