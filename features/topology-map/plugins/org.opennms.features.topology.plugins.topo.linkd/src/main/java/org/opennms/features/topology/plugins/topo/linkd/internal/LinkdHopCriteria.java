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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.VertexRef;

/**
 * 
 * @author <a href=mailto:thedesloge@opennms.org>Donald Desloge</a>
 * @author <a href=mailto:seth@opennms.org>Seth Leger</a>
 *
 */
public class LinkdHopCriteria extends VertexHopCriteria {
    
    public synchronized static VertexHopCriteria createCriteria(String nodeId, String nodeLabel, LinkdTopologyFactory linkdTopologyFactory) {
        return new LinkdHopCriteria(nodeId, nodeLabel, linkdTopologyFactory);
    }

    private final String m_nodeId;
    private final LinkdTopologyFactory m_linkdTopologyFactory;

    private LinkdHopCriteria(String nodeId, String nodeLabel, LinkdTopologyFactory linkdTopologyFactory) {
        super(nodeId,nodeLabel);
        m_nodeId = nodeId;
        m_linkdTopologyFactory = linkdTopologyFactory;
    }
    
    @Override
    public String getNamespace() {
        return m_linkdTopologyFactory.getActiveNamespace();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_nodeId == null) ? 0 : m_nodeId.hashCode());
        result = prime * result
                + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;

        if (obj instanceof LinkdHopCriteria) {
            LinkdHopCriteria ref = (LinkdHopCriteria)obj;
            return ref.m_nodeId.equals(m_nodeId) && ref.getNamespace().equals(getNamespace());
        }
        
        return false;
    }

    @Override
    public Set<VertexRef> getVertices() {
	Set<VertexRef> vertices = new TreeSet<>(new RefComparator());
        vertices.add(new DefaultVertexRef(getNamespace(), m_nodeId, getLabel()));
        return vertices;
    }
	
}
