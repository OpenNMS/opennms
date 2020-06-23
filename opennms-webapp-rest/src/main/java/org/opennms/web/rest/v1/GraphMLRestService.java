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

package org.opennms.web.rest.v1;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.graphdrawing.graphml.GraphmlType;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.spring.BeanUtils;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.graphml.service.GraphmlRepository;
import org.springframework.stereotype.Component;

@Component
@Path("graphml")
public class GraphMLRestService {

    private ServiceRegistry serviceRegistry;

    @POST
    @Path("{graph-name}")
    public Response createGraph(@PathParam("graph-name") String graphname, GraphmlType graphmlType) throws IOException {
       return runWithGraphmlRepositoryBlock(graphmlRepository -> {
           // Verify that it does not already exist
           if (graphmlRepository.exists(graphname)) {
               return Response.status(500).entity("Graph with name " + graphname + " already exists").build();
           }

           try {
               // Convert to the OpenNMS GraphML representation to apply additional validation
               GraphML convertedGraphML = GraphMLReader.convert(graphmlType);
               String label = convertedGraphML.getProperty("label", graphname);
               graphmlRepository.save(graphname, label, graphmlType);
               return Response.status(Response.Status.CREATED).build();
           } catch (InvalidGraphException ex) {
               return Response.status(500).entity(ex.getMessage()).build();
           }
       });
    }

    @DELETE
    @Path("{graph-name}")
    public Response deleteGraph(@PathParam("graph-name") String graphname) throws IOException {
       return runWithGraphmlRepositoryBlock(graphmlRepository -> {
           if (!graphmlRepository.exists(graphname)) {
               throw new NoSuchElementException("No GraphML file found with name  " + graphname);
           }
           graphmlRepository.delete(graphname);
           return Response.ok().build();
       });
    }

    @GET
    @Path("{graph-name}")
    public Response getGraph(@PathParam("graph-name") String graphname) throws IOException {
        return runWithGraphmlRepositoryBlock(graphmlRepository -> {
            GraphmlType byName = graphmlRepository.findByName(graphname);
            return Response.ok(byName).build();
        });
    }

    private ServiceRegistry getServiceRegistry() {
        if (serviceRegistry == null) {
            serviceRegistry = BeanUtils.getBean("soaContext", "serviceRegistry", ServiceRegistry.class);
            Objects.requireNonNull(serviceRegistry);
        }
        return serviceRegistry;
    }

    private interface Block {
        Response invoke(GraphmlRepository graphmlRepository) throws IOException;
    }

    /**
     * Helper to grap the GraphmlRepository from the ServiceRegistry before continuuing.
     * If the GraphmlRepository could not be found, a 503 (SERVICE_UNAVAILABLE) is returned, otherwise
     * the <code>block</code> is executed and that Response returned
     *
     * @param block The block to execute
     */
    private Response runWithGraphmlRepositoryBlock(Block block) throws IOException {
        Objects.requireNonNull(block);

        // Get Service
        final GraphmlRepository graphmlRepository = getServiceRegistry().findProvider(GraphmlRepository.class);
        if (graphmlRepository == null) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("No service registered to handle your query. This is a temporary issue. Please try again later.")
                    .build();
        }

        // Invoke actual logic
        Response response = block.invoke(graphmlRepository);
        Objects.requireNonNull(response);
        return response;
    }
}