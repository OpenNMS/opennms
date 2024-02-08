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
package org.opennms.features.topology.api.topo;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLevelAwareVertex extends AbstractVertex implements LevelAware {

    private VertexRef m_parent;
    private List<VertexRef> m_children = new ArrayList<>();

    public AbstractLevelAwareVertex(String namespace, String id, String label) {
        super(namespace, id, label);
    }

    public final VertexRef getParent() {
        return m_parent;
    }

    public final void setParent(VertexRef parent) {
        if (this.equals(parent)) return;
        m_parent = parent;
    }

    public void addChildren(AbstractLevelAwareVertex vertex) {
        if (!m_children.contains(vertex)) {
            m_children.add(vertex);
            vertex.setParent(this);
        }
    }

    public List<VertexRef> getChildren() {
        return m_children;
    }
}
