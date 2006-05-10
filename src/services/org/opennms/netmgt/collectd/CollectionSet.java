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
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;

public class CollectionSet {
	
	private CollectionAgent m_agent;
	private String m_collectionName;
    private NodeResourceDef m_nodeResourceDef;
    IfResourceDef m_ifResourceDef;
	
	public CollectionSet(CollectionAgent agent, String collectionName) {
		m_agent = agent;
		m_collectionName = collectionName;
        m_nodeResourceDef = new NodeResourceDef(m_agent, m_collectionName);
        m_ifResourceDef = new IfResourceDef(m_agent, m_collectionName);
	}
	
	public NodeInfo getNodeInfo() {
        return m_nodeResourceDef.getNodeInfo();
	}

	boolean hasDataToCollect() {
        return (m_nodeResourceDef.hasDataToCollect() || m_ifResourceDef.hasDataToCollect());
	}

    public String getCollectionName() {
		return m_collectionName;
	}
	
	public CollectionAgent getCollectionAgent() {
		return m_agent;
	}

	Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	public String getStorageFlag() {
        String collectionName = m_collectionName;
    	String storageFlag = DataCollectionConfigFactory.getInstance()
    			.getSnmpStorageFlag(collectionName);
    	if (storageFlag == null) {
            if (log().isEnabledFor(Priority.WARN)) {
    			log().warn(
    					"initialize: Configuration error, failed to "
    							+ "retrieve SNMP storage flag for collection: "
    							+ collectionName);
    		}
    		storageFlag = SnmpCollector.SNMP_STORAGE_PRIMARY;
    	}
    	return storageFlag;
    }

    int getMaxVarsPerPdu() {
    	// Retrieve configured value for max number of vars per PDU
    	int maxVarsPerPdu = DataCollectionConfigFactory.getInstance().getMaxVarsPerPdu(m_collectionName);
    	if (maxVarsPerPdu == -1) {
            if (log().isEnabledFor(Priority.WARN)) {
    			log().warn(
    					"initialize: Configuration error, failed to "
    							+ "retrieve max vars per pdu from collection: "
    							+ m_collectionName);
    		}
    		maxVarsPerPdu = SnmpCollector.DEFAULT_MAX_VARS_PER_PDU;
    	} 
        return maxVarsPerPdu;
    }

    void verifyCollectionIsNecessary(CollectionAgent agent) {
        /*
    	 * Verify that there is something to collect from this primary SMP
    	 * interface. If no node objects and no interface objects then throw
    	 * exception
    	 */
    	if (!hasDataToCollect()) {
            throw new RuntimeException("collection '" + getCollectionName()
    				+ "' defines nothing to collect for "
    				+ agent);
    	}
    }

    List getAttributeList() {
        return getNodeInfo().getAttributeList();
    }

    /**
     * @deprecated Use {@link org.opennms.netmgt.collectd.IfResourceDef#getCombinedInterfaceAttributes()} instead
     */
    List getCombinedInterfaceAttributes() {
        return m_ifResourceDef.getCombinedInterfaceAttributes();
    }

    /**
     * @deprecated Use {@link org.opennms.netmgt.collectd.IfResourceDef#getIfInfos()} instead
     */
    public Collection getIfInfos() {
        return m_ifResourceDef.getIfInfos();
    }

}
