/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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
