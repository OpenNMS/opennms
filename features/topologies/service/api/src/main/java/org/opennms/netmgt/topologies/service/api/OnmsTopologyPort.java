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

import java.util.Objects;

public class OnmsTopologyPort extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologyPort create(String id, OnmsTopologyVertex vertex, Integer index) {
        Objects.requireNonNull(id, "Cannot create port, id is null");
        Objects.requireNonNull(vertex, "Cannot create port, vertex is null");

        if (index != null) {
            return new OnmsTopologyPort(id, vertex, index);
        }
        return new OnmsTopologyPort(id, vertex, -1);
    }

    private final OnmsTopologyVertex m_vertex;
    private final Integer m_index;
    private Integer m_ifindex;
    private String m_ifname;
    
    private String m_addr;
    private String m_speed;
    

    private OnmsTopologyPort(String id, OnmsTopologyVertex vertex, Integer index) {
        super(id);
        m_vertex = vertex;
        m_index = index;
    }

    public String getAddr() {
        return m_addr;
    }


    public void setAddr(String addr) {
        m_addr = addr;
    }


    public String getSpeed() {
        return m_speed;
    }


    public void setSpeed(String speed) {
        m_speed = speed;
    }


    public OnmsTopologyVertex getVertex() {
        return m_vertex;
    }


    public Integer getIndex() {
        return m_index;
    }


    public Integer getIfindex() {
        return m_ifindex;
    }


    public void setIfindex(Integer ifindex) {
        m_ifindex = ifindex;
    }


    public String getIfname() {
        return m_ifname;
    }


    public void setIfname(String ifname) {
        m_ifname = ifname;
    }

    @Override
    public void accept(TopologyVisitor v) {
        v.visit(this);
    }

}