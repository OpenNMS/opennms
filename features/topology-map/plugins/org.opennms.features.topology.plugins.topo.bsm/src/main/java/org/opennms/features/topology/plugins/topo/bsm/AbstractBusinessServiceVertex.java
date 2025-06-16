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
package org.opennms.features.topology.plugins.topo.bsm;

import java.util.Set;

import org.opennms.features.topology.api.topo.AbstractLevelAwareVertex;
import org.opennms.features.topology.api.topo.LevelAware;

public abstract class AbstractBusinessServiceVertex extends AbstractLevelAwareVertex implements LevelAware {

    enum Type {
        BusinessService,
        IpService,
        ReductionKey,
        Application
    }

    private final int level;

    /**
     * Creates a new {@link AbstractBusinessServiceVertex}.
     *  @param id the unique id of this vertex. Must be unique overall the namespace.
     * @param label a human readable label
     * @param level the level of the vertex in the Business Service Hierarchy. The root element is level 0.
     */
    protected AbstractBusinessServiceVertex(String id, String label, int level) {
        super(BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE, id, label);
        this.level = level;
        setIconKey(null);
        setLocked(false);
        setSelected(false);
    }

    @Override
    public int getLevel() {
        return level;
    }

    public abstract boolean isLeaf();

    public abstract Type getType();

    public abstract Set<String> getReductionKeys();

    public abstract <T> T accept(BusinessServiceVertexVisitor<T> visitor);
}
