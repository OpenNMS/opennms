/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.smoketest.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.common.util.Base64Utility;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.xml.event.Event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A ReST API client for OpenNMS.
 *
 * Uses CXF to perform automatic marshaling/unmarshaling of request and
 * response objects.
 *
 * @author jwhite
 */
public class RestClient {

    private static final String DEFAULT_USERNAME = "admin";

    private static final String DEFAULT_PASSWORD = "admin";

    private final InetSocketAddress addr;

    private final String authorizationHeader;

    public RestClient(String host, int port) {
        this(InetSocketAddress.createUnresolved(host, port));
    }

    public RestClient(InetSocketAddress addr) {
        this(addr, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public RestClient(InetSocketAddress addr, String username, String password) {
        this.addr = addr;
        authorizationHeader = "Basic " + Base64Utility.encode((username + ":" + password).getBytes());
    }

    public String getDisplayVersion() {
        final WebTarget target = getTarget().path("info");
        final String json = getBuilder(target).get(String.class);

        final ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(json);
            return actualObj.get("displayVersion").asText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addOrReplaceRequisition(Requisition requisition) {
        final WebTarget target = getTarget().path("requisitions");
        getBuilder(target).post(Entity.entity(requisition, MediaType.APPLICATION_XML));
    }

    public void importRequisition(final String foreignSource) {
        final WebTarget target = getTarget().path("requisitions").path(foreignSource).path("import");
        getBuilder(target).put(null);
    }

    public List<OnmsNode> getNodes() {
        GenericType<List<OnmsNode>> nodes = new GenericType<List<OnmsNode>>() {
        };
        final WebTarget target = getTarget().path("nodes");
        return getBuilder(target).get(nodes);
    }

    public List<OnmsMonitoredService> getServicesForANode(String nodeCriteria, String ipAddress) {
        GenericType<List<OnmsMonitoredService>> services = new GenericType<List<OnmsMonitoredService>>() {
        };
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress)
                .path("services");
        return getBuilder(target).get(services);
    }

    public QueryResponse getMeasurements(final QueryRequest request) {
        final WebTarget target = getTarget().path("measurements");
        return getBuilder(target).post(Entity.entity(request, MediaType.APPLICATION_XML), QueryResponse.class);
    }

    public OnmsNode getNode(String nodeCriteria) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria);
        return getBuilder(target).get(OnmsNode.class);
    }

    public Response getResponseForNode(String nodeCriteria) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria);
        return getBuilder(target).get();
    }

    public Response addNode(OnmsNode onmsNode) {
        final WebTarget target = getTarget().path("nodes");
        return getBuilder(target).post(Entity.entity(onmsNode, MediaType.APPLICATION_XML));
    }

    public Response addInterface(String nodeCriteria, OnmsIpInterface ipInterface) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces");
        return getBuilder(target).post(Entity.entity(ipInterface, MediaType.APPLICATION_XML));
    }

    public Response deleteInterface(String nodeCriteria, String ipAddress) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress);
        return getBuilder(target).delete();
    }

    public Response addService(String nodeCriteria, String ipAddress, OnmsMonitoredService service) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress)
                .path("services");
        return getBuilder(target).post(Entity.entity(service, MediaType.APPLICATION_XML));
    }

    public OnmsMonitoredService getService(String nodeCriteria, String ipAddress, String service) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress)
                .path("services").path(service);
        return getBuilder(target).get(OnmsMonitoredService.class);
    }

    public Response getResponseForService(String nodeCriteria, String ipAddress, String service) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress)
                .path("services").path(service);
        return getBuilder(target).get();
    }

    public Response deleteService(String nodeCriteria, String ipAddress, String service) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress)
                .path("services").path(service);
        return getBuilder(target).delete();
    }

    public OnmsIpInterface getInterface(String nodeCriteria, String ipAddress) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress);
        return getBuilder(target).get(OnmsIpInterface.class);
    }

    public Response getResponseForInterface(String nodeCriteria, String ipAddress) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria).path("ipinterfaces").path(ipAddress);
        return getBuilder(target).get();
    }

    public OnmsMinion getMinion(String id) {
        final WebTarget target = getTarget().path("minions").path(id);
        return getBuilder(target).accept(MediaType.APPLICATION_XML).get(OnmsMinion.class);
    }

    public List<OnmsMinion> getAllMinions() {
        GenericType<List<OnmsMinion>> minions = new GenericType<List<OnmsMinion>>() {
        };
        final WebTarget target = getTargetV2().path("minions");
        return getBuilder(target).accept(MediaType.APPLICATION_XML).get(minions);
    }

    public Response addMinion(OnmsMinion minion) {
        final WebTarget target = getTargetV2().path("minions");
        return getBuilder(target).post(Entity.entity(minion, MediaType.APPLICATION_XML));
    }

    public Response deleteMinion(String id) {
        final WebTarget target = getTargetV2().path("minions").path(id);
        return getBuilder(target).delete();
    }

    public void sendEvent(Event event) {
        sendEvent(event, true);
    }

    public void sendEvent(Event event, boolean clearDates) {
        if (clearDates) {
            // Clear dates to avoid problems with date marshaling/unmarshaling
            event.setCreationTime(null);
            event.setTime(null);
        }
        final WebTarget target = getTarget().path("events");
        final Response response = getBuilder(target).post(Entity.entity(event, MediaType.APPLICATION_XML));
        if (!Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            throw new RuntimeException(String.format("Request failed with: %s:\n%s",
                    response.getStatusInfo().getReasonPhrase(), response.hasEntity() ? response.readEntity(String.class) : ""));
        }
    }

    public List<OnmsEvent> getEvents() {
        GenericType<List<OnmsEvent>> events = new GenericType<List<OnmsEvent>>() {
        };
        final WebTarget target = getTarget().path("events");
        return getBuilder(target).get(events);
    }

    public List<OnmsEvent> getAllEvents() {
        GenericType<List<OnmsEvent>> events = new GenericType<List<OnmsEvent>>() {
        };
        final WebTarget target = getTarget().path("events").queryParam("limit", 0);
        return getBuilder(target).get(events);
    }

    public Long getFlowCount(long start, long end) {
        final WebTarget target = getTarget().path("flows").path("count")
                .queryParam("start", start)
                .queryParam("end", end);
        return getBuilder(target).get(Long.class);
    }

    private WebTarget getTarget() {
        final Client client = ClientBuilder.newClient();
        return client.target(String.format("http://%s:%d/opennms/rest", addr.getHostString(), addr.getPort()));
    }

    private WebTarget getTargetV2() {
        final Client client = ClientBuilder.newClient();
        return client.target(String.format("http://%s:%d/opennms/api/v2", addr.getHostString(), addr.getPort()));
    }

    private Invocation.Builder getBuilder(final WebTarget target) {
        return target.request().header("Authorization", authorizationHeader);
    }
}
