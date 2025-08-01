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
package org.opennms.netmgt.graph.service;

import java.util.Objects;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer.GenericGraphContainerBuilder;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.api.service.GraphProvider;

public class SingleGraphContainerProvider implements GraphContainerProvider {

    private final GraphContainerInfo containerInfo;
    private final GraphProvider graphProvider;

    public SingleGraphContainerProvider(GraphProvider graphProvider, GraphContainerInfo containerInfo) {
        this.graphProvider = Objects.requireNonNull(graphProvider);
        this.containerInfo = Objects.requireNonNull(containerInfo);
    }

    @Override
    public ImmutableGraphContainer loadGraphContainer() {
        final GenericGraphContainerBuilder containerBuilder = GenericGraphContainer.builder().applyContainerInfo(getContainerInfo());
        final GenericGraph graph = graphProvider.loadGraph().asGenericGraph();
        if (graph != null) {
            containerBuilder.addGraph(graph);
        }
        return containerBuilder.build();
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        final DefaultGraphContainerInfo containerInfo = new DefaultGraphContainerInfo(this.containerInfo.getId());
        containerInfo.setDescription(this.containerInfo.getDescription());
        containerInfo.setLabel(this.containerInfo.getLabel());
        containerInfo.getGraphInfos().add(graphProvider.getGraphInfo());
        return containerInfo;
    }
}
