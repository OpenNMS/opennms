/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.BridgeElement;

public class Bridge {
    final Integer m_id;
    Integer m_rootPort;
    boolean m_isRootBridge=false;
    List<BridgeElement> m_elements = new ArrayList<BridgeElement>();

    public Bridge(Integer id) {
        super();
        m_id = id;
    }

    public Integer getRootPort() {
        return m_rootPort;
    }

    public void setRootPort(Integer rootPort) {
        m_rootPort = rootPort;
    }

    public boolean isRootBridge() {
        return m_isRootBridge;
    }

    public void setRootBridge(boolean isRootBridge) {
        m_isRootBridge = isRootBridge;
    }

    public Integer getId() {
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
        Bridge other = (Bridge) obj;
        if (m_id == null) {
            if (other.m_id != null)
                return false;
        } else if (!m_id.equals(other.m_id))
            return false;
        return true;
    }
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("bridge:[");
        strbfr.append(m_id);
        strbfr.append("], isrootbridge:");
        strbfr.append(m_isRootBridge);
        strbfr.append(" designated port:");
        strbfr.append(m_rootPort);
        strbfr.append("]\n");
        return strbfr.toString();

    }

    public void clearBridgeElement() {
    	m_elements.clear();
    }
	public List<BridgeElement> getBridgeElements() {
		return m_elements;
	}
	
	public boolean addBridgeElement(BridgeElement element) {
		if (element.getNode().getId().intValue() == m_id.intValue()) {
			m_elements.add(element);
			return true;
		}
		return false;
	}

}
