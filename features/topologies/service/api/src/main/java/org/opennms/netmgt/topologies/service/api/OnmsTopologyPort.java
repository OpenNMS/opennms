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

public class OnmsTopologyPort extends OnmsTopologyAbstractRef implements OnmsTopologyRef {

    public static OnmsTopologyPort create(OnmsTopologyVertex vertex, Integer index) {
        if (vertex !=  null ) {
            return new OnmsTopologyPort(vertex.getId()+":"+index,vertex,index);
        }
        
        return null;
    }
        
    private final OnmsTopologyVertex m_vertex;
    private final Integer m_index;
    
    private String m_port;
    private String m_addr;
    private String m_speed;
    

    private OnmsTopologyPort(String id, OnmsTopologyVertex vertex, Integer index) {
        super(id);
        m_vertex = vertex;
        m_index = index;
    }


    public String getPort() {
        return m_port;
    }


    public void setPort(String port) {
        m_port = port;
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

    
     
}