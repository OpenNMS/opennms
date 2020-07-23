/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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
