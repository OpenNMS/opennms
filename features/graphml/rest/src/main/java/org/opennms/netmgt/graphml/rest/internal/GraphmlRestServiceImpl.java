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

package org.opennms.netmgt.graphml.rest.internal;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.graphdrawing.graphml.GraphmlType;
import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.features.graphml.service.GraphmlRepository;
import org.opennms.netmgt.graphml.rest.GraphmlRestService;

public class GraphmlRestServiceImpl implements GraphmlRestService {

    private final GraphmlRepository graphmlRepository;

    public GraphmlRestServiceImpl(GraphmlRepository graphmlRepository) {
        this.graphmlRepository = Objects.requireNonNull(graphmlRepository);
    }

    @Override
    public Response createGraph(String graphname, GraphmlType graphmlType) throws IOException {
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
    }

    @Override
    public Response deleteGraph(String graphname) throws IOException {
        if (!graphmlRepository.exists(graphname)) {
            throw new NoSuchElementException("No GraphML file found with name  " + graphname);
        }
        graphmlRepository.delete(graphname);
        return Response.ok().build();
    }

    @Override
    public Response getGraph(String graphname) throws IOException {
        final GraphmlType byName = graphmlRepository.findByName(graphname);
        return Response.ok(byName).build();
    }

}