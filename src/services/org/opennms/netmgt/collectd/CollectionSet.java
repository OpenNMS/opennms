//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class CollectionSet {
	
	private CollectionAgent m_agent;
    private NodeResourceType m_nodeResourceType;
    private IfResourceType m_ifResourceType;
    private OnmsSnmpCollection m_snmpCollection;
	
	public CollectionSet(CollectionAgent agent, OnmsSnmpCollection snmpCollection) {
		m_agent = agent;
        m_snmpCollection = snmpCollection;
        m_nodeResourceType = new NodeResourceType(m_agent, snmpCollection);
        m_ifResourceType = new IfResourceType(m_agent, snmpCollection);
	}
	
	public NodeInfo getNodeInfo() {
        return m_nodeResourceType.getNodeInfo();
	}

	boolean hasDataToCollect() {
        return (m_nodeResourceType.hasDataToCollect() || m_ifResourceType.hasDataToCollect());
	}
    
    boolean hasInterfaceDataToCollect() {
        return m_ifResourceType.hasDataToCollect();
    }

	public CollectionAgent getCollectionAgent() {
		return m_agent;
	}

	Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public String getStorageFlag() {
        return m_snmpCollection.getStorageFlag();
    }

    int getMaxVarsPerPdu() {
        return m_snmpCollection.getMaxVarsPerPdu();
    }

    void verifyCollectionIsNecessary(CollectionAgent agent) {
        /*
    	 * Verify that there is something to collect from this primary SMP
    	 * interface. If no node objects and no interface objects then throw
    	 * exception
    	 */
    	if (!hasDataToCollect()) {
            throw new RuntimeException("collection '" + this
                    + "' defines nothing to collect for " + agent);
    	}
    }

    Collection getAttributeList() {
        return getNodeInfo().getAttributeList();
    }

    /**
     * @deprecated Use {@link org.opennms.netmgt.collectd.IfResourceType#getCombinedInterfaceAttributes()} instead
     */
    List getCombinedInterfaceAttributes() {
        return m_ifResourceType.getCombinedInterfaceAttributes();
    }

    /**
     * @deprecated Use {@link org.opennms.netmgt.collectd.IfResourceType#getIfInfos()} instead
     */
    public Collection getIfInfos() {
        return m_ifResourceType.getIfInfos();
    }

    public IfInfo getIfInfo(int ifIndex) {
        return m_ifResourceType.getIfInfo(ifIndex);
    }


}
