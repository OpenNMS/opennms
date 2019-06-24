/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.topologies.service.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OnmsTopologyEdge extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologyEdge create(String id, OnmsTopologyPort source, OnmsTopologyPort target) {
        Objects.requireNonNull(source, "source port null, cannot create edge");
        Objects.requireNonNull(target, "target port null, cannot create edge");

        if (source.getId().equals(target.getId())) {
            throw new IllegalArgumentException("target equals source port, cannot create edge");
        }
        
        return new OnmsTopologyEdge(id, source, target);
    }
        
    private final OnmsTopologyPort m_source;
    private final OnmsTopologyPort m_target;

    private OnmsTopologyEdge(String id, OnmsTopologyPort source, OnmsTopologyPort target) {
        super(id);
        m_source = source;
        m_target = target;
    }

    public OnmsTopologyPort getSource() {
        return m_source;
    }

    public OnmsTopologyPort getTarget() {
        return m_target;
    }

    public OnmsTopologyPort getPort(String id) {
        return getPorts().stream().filter(p -> id.equals(p.getId())).findAny().orElse(null);
    }
     
    public boolean hasPort(String id) {
           return (getPort(id) != null);
    }
    
    public List<OnmsTopologyPort> getPorts() {
        List<OnmsTopologyPort>ports = new ArrayList<>();
        ports.add(m_source);
        ports.add(m_target);
        return ports;
    }

    @Override
    public void accept(TopologyVisitor v) {
        v.visit(this);
    }
}
