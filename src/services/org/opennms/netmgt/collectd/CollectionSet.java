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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

public class CollectionSet {
	
	private CollectionAgent m_agent;
	private String m_collectionName;
	
	private Map m_ifMap = new TreeMap();
	
	public CollectionSet(CollectionAgent agent, String collectionName) {
		m_agent = agent;
		m_collectionName = collectionName;
		addSnmpInterfacesToCollectionSet();
	}
	
	public Map getIfMap() {
		return m_ifMap;
	}
	public void setIfMap(Map ifMap) {
		m_ifMap = ifMap;
	}
	public NodeInfo getNodeInfo() {
		return new NodeInfo(m_agent, m_collectionName);
	}

	void addIfInfo(IfInfo ifInfo) {
		getIfMap().put(new Integer(ifInfo.getIndex()), ifInfo);
	}

	boolean hasDataToCollect() {
        if (!getNodeInfo().getAttributeList().isEmpty()) return true;
        return hasInterfaceDataTo‚ollect();
	}

    boolean hasInterfaceDataTo‚ollect() {
        Iterator iter = getIfMap().values().iterator();
        while (iter.hasNext()) {
            IfInfo ifInfo = (IfInfo) iter.next();
            if (!ifInfo.getAttributeList().isEmpty()) {
                return true;
            }
        }

        return false;
    }

	public String getCollectionName() {
		return m_collectionName;
	}
	
	public CollectionAgent getCollectionAgent() {
		return m_agent;
	}

	void addSnmpInterface(OnmsSnmpInterface snmpIface) {
		addIfInfo(new IfInfo(m_agent, m_collectionName, snmpIface));
	}

	void logInitializeSnmpIface(OnmsSnmpInterface snmpIface) {
		if (log().isDebugEnabled()) {
			log()
			.debug(
					"initialize: snmpifindex = " + snmpIface.getIfIndex().intValue()
					+ ", snmpifname = " + snmpIface.getIfName()
					+ ", snmpifdescr = " + snmpIface.getIfDescr()
					+ ", snmpphysaddr = -"+ snmpIface.getPhysAddr() + "-");
		}
		
		if (log().isDebugEnabled()) {
			log().debug("initialize: ifLabel = '" + snmpIface.computeLabelForRRD() + "'");
		}
	
	
	}

	private Category log() {
		return ThreadCategory.getInstance(getClass());
	}

	void addSnmpInterfacesToCollectionSet() {
		CollectionAgent agent = getCollectionAgent();
		OnmsNode node = agent.getNode();
	
		Set snmpIfs = node.getSnmpInterfaces();
		
		for (Iterator it = snmpIfs.iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIface = (OnmsSnmpInterface) it.next();
			logInitializeSnmpIface(snmpIface);
			addSnmpInterface(snmpIface);
			
		}
		
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

    List getCombinedInterfaceAttributes() {
        Set attributes = new LinkedHashSet();
        for (Iterator it = getIfMap().values().iterator(); it.hasNext();) {
            IfInfo ifInfo = (IfInfo) it.next();
            attributes.addAll(ifInfo.getAttributeList());
        }
        return new ArrayList(attributes);
    }

}
