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
package org.opennms.netmgt.graph.provider.graphml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphmlGraphContainerProvider implements GraphContainerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GraphmlGraphContainerProvider.class);
    private GenericGraphContainer graphContainer;

    public GraphmlGraphContainerProvider(String location) throws IOException, InvalidGraphException {
        if (!new File(location).exists()) {
            throw new FileNotFoundException(location);
        }
        try (InputStream input = new FileInputStream(location)) {
            final GraphML graphML = GraphMLReader.read(input);
            this.graphContainer = new GraphmlToGraphConverter().convert(graphML);
        }

        // Verify that the container Id matches the graph's name.
        // This is not critical but should be by convention
        // Determine graph file name
        final String filename = Paths.get(location).getFileName().toString();
        final String graphName = filename.substring(0, filename.lastIndexOf("."));
        if (!graphContainer.getId().equals(graphName)) {
            LOG.warn("The GraphML file name and the container id do not match but should. GraphML file name: '{}', container id: '{}'.", graphName, graphContainer.getId());
        }
    }

    @Override
    public ImmutableGraphContainer loadGraphContainer() {
        // The container does not support manual file changes.
        // Any change must be Pushed again through the rest-service.
        // Therefore we always return the same instance after the container has been loaded.
        return graphContainer;
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        // As this is static content, the container info is already part of the graph, no extra setup required
        // The content is already in memory, so no further conversion is performed.
        return graphContainer;
    }

}
