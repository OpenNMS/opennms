/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.rest.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.flows.rest.FlowRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class FlowRestServiceImpl implements FlowRestService {

    private static final String ELASTIC_SEARCH_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static final Logger LOG = LoggerFactory.getLogger(FlowRestServiceImpl.class);

    private FlowRepository flowRepository;

    public FlowRestServiceImpl(FlowRepository flowRepository) {
        this.flowRepository = Objects.requireNonNull(flowRepository);
    }
    
    // TODO MVR This is duplicated from ClientFactory, should be merged or removed at some point
    private static final Gson gson =  new GsonBuilder()
            .setDateFormat(ELASTIC_SEARCH_DATE_FORMAT)
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    @Override
    public Response proxySearch(String query) {
        try {
            final String result = flowRepository.rawQuery(query);
            return Response.status(200)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(result)
                    .build();
        } catch (FlowException e) {
            LOG.error("Error while proxy search flows: {}", e.getMessage(), e);
            return Response.status(500)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @Override
    public Response getFlows() {
        return getFlows("");
    }

    @Override
    public Response getFlows(String query) {
        try {
            final List<NetflowDocument> documents = flowRepository.findAll(query);
            final String json = gson.toJson(documents);
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } catch (FlowException ex) {
            LOG.error("Error while fetching flows from repository", ex);
            return Response.status(500)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Error while fetching flows from repository: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public Response saveFlows(String input) {
        try {
            final JsonElement jsonElement = gson.fromJson(input, JsonElement.class);
            if (jsonElement.isJsonArray()) {
                final Type listType = new TypeToken<ArrayList<NetflowDocument>>() {
                }.getType();
                List<NetflowDocument> netflowDocuments = gson.fromJson(jsonElement, listType);
                flowRepository.save(netflowDocuments);
            } else {
                List<NetflowDocument> documents = new ArrayList<>();
                documents.add(gson.fromJson(jsonElement, NetflowDocument.class));
                flowRepository.save(documents);
            }
            return Response.accepted().build();
        } catch (FlowException e) {
            LOG.error("Error while persisting flow(s)", e);
            return Response.status(500)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("Error while persisting flow(s): " + e.getMessage())
                    .build();
        }
    }
}
