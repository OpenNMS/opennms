/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
