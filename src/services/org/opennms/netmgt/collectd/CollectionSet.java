package org.opennms.netmgt.collectd;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
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
		if (getNodeInfo().getOidList().isEmpty()) {
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

	void logInitializeSnmpIface(Initializer x, OnmsSnmpInterface snmpIface) {
		logInitializeSnmpIface(snmpIface);
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

}
