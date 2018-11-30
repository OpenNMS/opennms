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

public class OnmsTopologyEdge extends OnmsTopologyShared {

    public static OnmsTopologyEdge create(String id, OnmsTopologyPort source, OnmsTopologyPort target) throws OnmsTopologyException {
        if (source == null && target == null) {
            throw new OnmsTopologyException("source and target port null, cannot create edge");
        }
        if (source == null ) {
            throw new OnmsTopologyException("source port null, cannot create edge");
        }
        if (target == null ) {
            throw new OnmsTopologyException("target port null, cannot create edge");
        }
        
        if (source.getId().equals(target.getId())) {
            throw new OnmsTopologyException("target =0 source port, cannot create edge");
        }
        
        return new OnmsTopologyEdge(id, source, target);
    }
        
    private final OnmsTopologyPort m_source;
    private final OnmsTopologyPort m_target;

    private OnmsTopologyEdge(String id, OnmsTopologyPort source, OnmsTopologyPort target) {
        super(id, source,target);
        m_source = source;
        m_target = target;
    }

    public OnmsTopologyPort getSource() {
        return m_source;
    }

    public OnmsTopologyPort getTarget() {
        return m_target;
    }
    
}
