/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.topologies.service.api;

import java.util.Objects;

public class OnmsTopologyProtocol {

    private static final OnmsTopologyProtocol ALL_PROTOCOLS = create("ALL",OnmsProtocolLayer.NoLayer);

    public enum OnmsProtocolLayer {
        UserDefined(0),
        Layer7(1),
        Layer6(2),
        Layer5(3),
        Layer4(4),
        Layer3p4(5),
        Layer3(6),
        NetworkTopology(7),
        Layer2c5(8),
        Layer2(9),
        Layer1(10),
        NoLayer(11);
        
        private final int position;
        
        private OnmsProtocolLayer(int position) {
            this.position=position;
        }
        
        public int getPosition() {
            return position;
        }
        
    }
    public static OnmsTopologyProtocol create(String id, OnmsProtocolLayer layer) {
        Objects.requireNonNull(id, "id is null, cannot create protocol");
        Objects.requireNonNull(layer, "layer is null, cannot create protocol");
        return new OnmsTopologyProtocol(id.toUpperCase(),layer);
    }
    
    public static OnmsTopologyProtocol allProtocols() {
        return ALL_PROTOCOLS;
    }
    
    final private String m_id;
    final private OnmsProtocolLayer m_layer;
    private String m_name;
    private String m_source;

    private OnmsTopologyProtocol(String id, OnmsProtocolLayer layer) {
        m_id=id;
        m_layer=layer;
    }

    public String getId() {
        return m_id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_id == null) ? 0 : m_id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OnmsTopologyProtocol other = (OnmsTopologyProtocol) obj;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        return true;
    }

    public String getName() {
        return m_name;
    }

    public String getSource() {
        return m_source;
    }

    public void setName(String name) {
        m_name = name;
    }

    public void setSource(String source) {
        m_source = source;
    }

    public OnmsProtocolLayer getLayer() {
        return m_layer;
    }
    
    public OnmsTopologyProtocol clone() {
        OnmsTopologyProtocol protocol = create(m_id, m_layer);
        protocol.setName(m_name);
        protocol.setSource(m_source);
        return protocol;
    }

}
