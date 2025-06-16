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
package org.opennms.netmgt.graph.api.persistence;

import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;

/**
 * The {@link GraphRepository} allows persisting any given {@link ImmutableGraphContainer} or {@link GraphContainerInfo}.
 *
 * Please ensure that the implementing side knows how to persist the property values accordingly (e.g. custom types)
 */
public interface GraphRepository {

    void save(ImmutableGraphContainer graphContainer);

    void save(GraphContainerInfo containerInfo);

    GenericGraphContainer findContainerById(String containerId);

    GraphContainerInfo findContainerInfoById(String containerId);

    void deleteContainer(String containerId);

}
