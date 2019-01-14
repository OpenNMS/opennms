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

import java.util.Arrays;
import java.util.List;

public class OnmsTopologySegment extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologySegment create(String id,OnmsTopologyPort...sources) throws OnmsTopologyException {
        if (id ==  null || sources == null || sources.length <= 1) {
            throw new OnmsTopologyException("Cannot create Shared");
        }
        return new OnmsTopologySegment(id,sources);
    }
        
    private final OnmsTopologyPort[] m_sources;

    protected OnmsTopologySegment(String id, OnmsTopologyPort...sources) {
        super(id);
        m_sources = sources;
    }

    public List<OnmsTopologyPort> getSources() {
        return Arrays.asList(m_sources);
    }
    
    public OnmsTopologyPort getPort(String id) {
        return Arrays.asList(m_sources).stream().filter(p -> id.equals(p.getId())).findAny().orElse(null);
    }

    public boolean hasPort(String id) {
        return (getPort(id) != null);
    }
    
}
