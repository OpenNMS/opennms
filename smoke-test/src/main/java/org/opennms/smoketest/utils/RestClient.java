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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.common.util.Base64Utility;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

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

    public QueryResponse getMeasurements(final QueryRequest request) {
        final WebTarget target = getTarget().path("measurements");
        return getBuilder(target).post(Entity.entity(request, MediaType.APPLICATION_XML), QueryResponse.class);
    }

    public OnmsNode getNode(String nodeCriteria) {
        final WebTarget target = getTarget().path("nodes").path(nodeCriteria);
        return getBuilder(target).get(OnmsNode.class);
    }

    private WebTarget getTarget() {
        final Client client = ClientBuilder.newClient();
        return client.target(String.format("http://%s:%d/opennms/rest", addr.getHostString(), addr.getPort()));
    }

    private Invocation.Builder getBuilder(final WebTarget target) {
        return target.request().header("Authorization", authorizationHeader);
    }
}
