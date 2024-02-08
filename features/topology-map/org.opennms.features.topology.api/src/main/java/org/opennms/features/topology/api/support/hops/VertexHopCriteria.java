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
package org.opennms.features.topology.api.support.hops;

import java.util.Set;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

public abstract class VertexHopCriteria extends Criteria {
    private String m_id = "";
    private String m_label;

    @Override
    public String toString() {
        return "Namespace:"+getNamespace()+", ID:"+getId()+", Label:"+getLabel();
    }

    //Adding explicit constructor because I found that this label must be set
    //for the focus list to have meaningful information in the focus list.
    public VertexHopCriteria(String label) {
        m_label = label;
    }

    public VertexHopCriteria(String id, String label) {
        m_id = id;
        m_label = label;
    }

    @Override
    public ElementType getType() {
        return ElementType.VERTEX;
    }

    public abstract Set<VertexRef> getVertices();

    public String getLabel() {
        return m_label;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public void setId(String id){
        m_id = id;
    }

    public String getId(){
        return m_id;
    }

    public boolean isEmpty() {
        Set<VertexRef> vertices = getVertices();
        if (vertices == null) {
            return false;
        }
        return vertices.isEmpty();
    }
}
