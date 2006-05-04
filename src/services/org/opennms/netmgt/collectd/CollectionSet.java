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
	
	private CollectionInterface m_collectionInterface;
	private String m_collectionName;
	
	private Map m_ifMap = new TreeMap();
	
	public CollectionSet(CollectionInterface collectionInterface, String collectionName) {
		m_collectionInterface = collectionInterface;
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
		return new NodeInfo(m_collectionInterface, m_collectionName);
	}

	void addIfInfo(IfInfo ifInfo) {
		getIfMap().put(new Integer(ifInfo.getIndex()), ifInfo);
	}

	boolean hasDataToCollect() {
		boolean hasInterfaceOids = false;
		if (getNodeInfo().getAttributeList().isEmpty()) {
			hasInterfaceOids = false;
			Iterator iter = getIfMap().values().iterator();
			while (iter.hasNext() && !hasInterfaceOids) {
				IfInfo ifInfo = (IfInfo) iter.next();
				if (!ifInfo.getOidList().isEmpty()) {
					hasInterfaceOids = true;
				}
			}
		}
		return hasInterfaceOids;
	}

	public String getCollectionName() {
		return m_collectionName;
	}
	
	public CollectionInterface getCollectionInterface() {
		return m_collectionInterface;
	}

	void addSnmpInterface(OnmsSnmpInterface snmpIface) {
		addIfInfo(new IfInfo(m_collectionInterface, m_collectionName, snmpIface));
	}

	boolean hasInterfaceOids() {
		
		Map ifMap = getIfMap();
		if (ifMap == null) return false;
		
		Iterator iter = ifMap.values().iterator();
		while (iter.hasNext()) {
			IfInfo ifInfo = (IfInfo) iter.next();
			if (ifInfo.getType() < 1) {
				continue;
			}
			if (!ifInfo.getOidList().isEmpty()) {
				return true;
			}
		}
		return false;
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
		CollectionInterface collectionInterface = getCollectionInterface();
		OnmsNode node = collectionInterface.getNode();
	
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
    	int maxVarsPerPdu = DataCollectionConfigFactory.getInstance()
    			.getMaxVarsPerPdu(m_collectionName);
    	if (maxVarsPerPdu == -1) {
            if (log().isEnabledFor(Priority.WARN)) {
    			log().warn(
    					"initialize: Configuration error, failed to "
    							+ "retrieve max vars per pdu from collection: "
    							+ m_collectionName);
    		}
    		maxVarsPerPdu = SnmpCollector.DEFAULT_MAX_VARS_PER_PDU;
    	} else if (maxVarsPerPdu == 0) {
    		/*
    		 * Special case, zero indicates "no limit" on number of vars in a
    		 * single PDU...so set maxVarsPerPdu to maximum integer value:
    		 * Integer.MAX_VALUE. This is a lot easier than building in special
    		 * logic to handle a value of zero. Doubt anyone will attempt to
    		 * collect over 2 billion oids.
    		 */
    		maxVarsPerPdu = Integer.MAX_VALUE;
    	}
    	return maxVarsPerPdu;
    }

    void verifyCollectionIsNecessary(CollectionInterface iface) {
        /*
    	 * Verify that there is something to collect from this primary SMP
    	 * interface. If no node objects and no interface objects then throw
    	 * exception
    	 */
    	if (!hasDataToCollect()) {
            throw new RuntimeException("collection '" + getCollectionName()
    				+ "' defines nothing to collect for "
    				+ iface);
    	}
    }

    List getAttributeList() {
        return getNodeInfo().getAttributeList();
    }

    List getCombinedInterfaceOids() {
        Map ifMap = getIfMap();
        List allOids = new ArrayList();
    
        // Iterate over all the interface's in the interface map
        //
        if (ifMap != null) {
            Iterator i = ifMap.values().iterator();
            while (i.hasNext()) {
                IfInfo ifInfo = (IfInfo) i.next();
                List ifOidList = ifInfo.getOidList();
    
                // Add unique interface oid's to the list
                //
                Iterator j = ifOidList.iterator();
                while (j.hasNext()) {
                    MibObject oid = (MibObject) j.next();
                    if (!allOids.contains(oid))
                        allOids.add(oid);
                }
            }
        }
    	return allOids;
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
