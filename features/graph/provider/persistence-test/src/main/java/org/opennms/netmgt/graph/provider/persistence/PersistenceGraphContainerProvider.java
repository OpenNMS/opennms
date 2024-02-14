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
package org.opennms.netmgt.graph.provider.persistence;

import java.util.Objects;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.persistence.GraphRepository;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;

/**
 * This {@link GraphContainerProvider} is an example on how to use the {@link GraphRepository} within a provider.
 *
 * @author mvrueden
 */
public class PersistenceGraphContainerProvider implements GraphContainerProvider {

    private static final String CONTAINER_ID = "persistence-example";

    private final GraphRepository graphRepository;

    public PersistenceGraphContainerProvider(GraphRepository graphRepository) {
        this.graphRepository = Objects.requireNonNull(graphRepository);
    }

    @Override
    public ImmutableGraphContainer loadGraphContainer() {
        final GenericGraphContainer genericGraphContainer = graphRepository.findContainerById(CONTAINER_ID);
        // It is not required to wrap the generic graph container with the domain view. However for
        // test purposes we do it here anyways.
        return new CustomGraphContainer(genericGraphContainer);
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        final GraphContainerInfo containerInfoById = graphRepository.findContainerInfoById(CONTAINER_ID);

        // As we provide a domain container above, we have to re-create each graphinfo to use the domain vertex type, instead of GenericVertex
        final DefaultGraphContainerInfo defaultGraphContainerInfo = new DefaultGraphContainerInfo(containerInfoById.getId());
        defaultGraphContainerInfo.setDescription(containerInfoById.getDescription());
        defaultGraphContainerInfo.setLabel(containerInfoById.getLabel());
        containerInfoById.getGraphInfos().forEach(gi -> {
            // Override vertex type
            final DefaultGraphInfo defaultGraphInfo = new DefaultGraphInfo(gi);
            defaultGraphContainerInfo.addGraphInfo(defaultGraphInfo);
        });
        return defaultGraphContainerInfo;
    }

    // This ensures that the container info is already present, even if nothing was persisted yet
    public void init() {
        if (graphRepository.findContainerInfoById(CONTAINER_ID) != null) {
            graphRepository.deleteContainer(CONTAINER_ID);
        }
        graphRepository.save(createContainerInfo());
    }

    protected static GraphContainerInfo createContainerInfo() {
        final DefaultGraphContainerInfo info = new DefaultGraphContainerInfo(CONTAINER_ID);
        info.setDescription("Example container which uses the GraphRepository for persistence");
        info.setLabel("Example Persistence Graph Container");

        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(CONTAINER_ID + ".graph");
        graphInfo.setDescription("The only graph of the container");
        graphInfo.setLabel("Graph");
        info.getGraphInfos().add(graphInfo);

        return info;
    }

}