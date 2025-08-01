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
package org.opennms.netmgt.graph.api.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public interface GraphService {

    List<GraphContainerInfo> getGraphContainerInfos();

    GraphContainerInfo getGraphContainerInfo(String containerId);

    GraphContainerInfo getGraphContainerInfoByNamespace(String namespace);

    GraphInfo getGraphInfo(String graphNamespace);

    GenericGraphContainer getGraphContainer(String containerId);

    GenericGraph getGraph(String containerId, String graphNamespace);

    default GenericGraph getGraph(String namespace) {
        final GraphContainerInfo graphContainerInfo = getGraphContainerInfoByNamespace(namespace);
        if (graphContainerInfo != null) {
            final GenericGraph graph = getGraphContainer(graphContainerInfo.getId()).getGraph(namespace);
            return graph;
        }
        throw new NoSuchElementException("Could not find a Graph with namespace '" + namespace + "'.");
    }
}
