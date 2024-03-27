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

import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainGraph;

public class CustomGraph extends AbstractDomainGraph<CustomVertex, CustomEdge> {
    public CustomGraph(GenericGraph genericGraph) {
        super(genericGraph);
    }

    @Override
    protected CustomGraph convert(GenericGraph graph) {
        return new CustomGraph(graph);
    }

    @Override
    protected CustomVertex convert(GenericVertex vertex) {
        return new CustomVertex(vertex);
    }

    @Override
    protected CustomEdge convert(GenericEdge edge) {
        return new CustomEdge(edge);
    }
}
