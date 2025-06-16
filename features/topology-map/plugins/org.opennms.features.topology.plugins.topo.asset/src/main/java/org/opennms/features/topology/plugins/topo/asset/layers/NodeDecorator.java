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
package org.opennms.features.topology.plugins.topo.asset.layers;

import org.opennms.features.graphml.model.GraphMLNode;

/**
 * Decorator to set the fields of each {@link GraphMLNode} in a {@link Layer}.
 *
 * @author mvrueden
 *
 * @param <T> the type of the value each {@link org.opennms.netmgt.model.OnmsNode} is converted to.
 * @see ItemProvider
 */
public interface NodeDecorator<T> {
    void decorate(GraphMLNode graphMLNode, T value);

    /**
     * Returns the {@link GraphMLNode}'s id for the given <code>value</code>.
     *
     * DOES NOT CONSIDER HIERARCHY.
     *
     * @param value The <code>code</code>.
     * @return the {@link GraphMLNode}'s id for the given <code>value</code>.
     * @see IdGenerator
     */
    String getId(T value);
}
