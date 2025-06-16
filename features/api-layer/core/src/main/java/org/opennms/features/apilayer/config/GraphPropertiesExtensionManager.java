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
package org.opennms.features.apilayer.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.datacollection.graphs.GraphPropertiesExtension;
import org.opennms.integration.api.v1.config.datacollection.graphs.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphs;

public class GraphPropertiesExtensionManager extends ConfigExtensionManager<GraphPropertiesExtension, PrefabGraphs>{


    public GraphPropertiesExtensionManager() {
        super(PrefabGraphs.class, new PrefabGraphs());
    }

    @Override
    protected PrefabGraphs getConfigForExtensions(Set<GraphPropertiesExtension> extensions) {
        List<org.opennms.netmgt.model.PrefabGraph> graphList = extensions.stream().flatMap(extension -> extension.getPrefabGraphs().stream())
                .map(GraphPropertiesExtensionManager::toPrefabGraphs)
                .collect(Collectors.toList());
        PrefabGraphs prefabGraphs = new PrefabGraphs();
        prefabGraphs.setPrefabGraphs(graphList);
        return prefabGraphs;
    }

    @Override
    protected void triggerReload() {
        // PropertiesGraphDao doesn't need reload.
    }

    private static org.opennms.netmgt.model.PrefabGraph toPrefabGraphs(PrefabGraph graph) {
        return new org.opennms.netmgt.model.PrefabGraph(graph.getName(), graph.getTitle(), graph.getColumns(), graph.getCommand(),
                graph.getExternalValues(), graph.getPropertiesValues(), 0, graph.getTypes(), graph.getDescription(),
                graph.getGraphWidth(), graph.getGraphHeight(), graph.getSupress());
    }
}
