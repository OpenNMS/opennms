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