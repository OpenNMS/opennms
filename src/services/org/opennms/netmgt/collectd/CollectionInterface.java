package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.poller.IPv4NetworkInterface;

public class CollectionInterface extends IPv4NetworkInterface {

	private OnmsIpInterface m_iface;
	private CollectionSet m_collectionSet;

	public CollectionInterface(OnmsIpInterface iface) {
		super(iface.getInetAddress());
		m_iface = iface;
	}
	
	public OnmsIpInterface getIpInterface() {
		return m_iface;
	}
	
	public OnmsNode getNode() {
		return m_iface.getNode();
	}

	InetAddress getInetAddress() {
	
		if (getType() != CollectionInterface.TYPE_IPV4)
			throw new RuntimeException("Unsupported interface type, "
					+ "only TYPE_IPV4 currently supported");
	
	
	
		InetAddress ipaddr = (InetAddress) getAddress();
		return ipaddr;
	}

	String getSnmpStorage() {
		String snmpStorage = (String) getAttribute(SnmpCollector.SNMP_STORAGE_KEY);
		return snmpStorage;
	}

	void setMaxVarsPerPdu(int maxVarsPerPdu) {
		// Add max vars per pdu value as an attribute of the interface
		setAttribute(SnmpCollector.MAX_VARS_PER_PDU_STORAGE_KEY, new Integer(
				maxVarsPerPdu));
		if (log().isDebugEnabled()) {
			log().debug("maxVarsPerPdu=" + maxVarsPerPdu);
		}
	}

	void setStorageFlag(String storageFlag) {
		setAttribute(SnmpCollector.SNMP_STORAGE_KEY, storageFlag);
		if (log().isDebugEnabled()) {
			log().debug("initialize: SNMP storage flag: '" + storageFlag + "'");
		}
	}

	String getHostAddress() {
		return getInetAddress().getHostAddress();
	}

	void saveIfCount(int ifCount) {
		/*
		 * Add the interface count to the interface's attributes for retrieval
		 * during poll()
		 */
		setAttribute(SnmpCollector.INTERFACE_COUNT_KEY, new Integer(ifCount));
	}

	int getSavedIfCount() {
		int savedIfCount = -1;
		Integer tmp = (Integer) getAttribute(SnmpCollector.INTERFACE_COUNT_KEY);
		if (tmp != null) {
			savedIfCount = tmp.intValue();
		}
		return savedIfCount;
	}

	boolean hasInterfaceOids() {
		return m_collectionSet.hasInterfaceOids();
	}

	Map getIfMap() {
		return getCollectionSet().getIfMap();
	}

	public void setCollectionSet(CollectionSet collectionSet) {
		m_collectionSet = collectionSet;
	}
	
	public CollectionSet getCollectionSet() {
		return m_collectionSet;
	}
	
	NodeInfo getNodeInfo() {
		return m_collectionSet.getNodeInfo();
	}

	int getNodeId() {
		return getIpInterface().getNode().getId() == null ? -1 : getIpInterface().getNode().getId().intValue();
	}

	int getIfIndex() {
		return (getIpInterface().getIfIndex() == null ? -1 : getIpInterface().getIfIndex().intValue());
	}

	String getSysObjectId() {
		return getIpInterface().getNode().getSysObjectId();
	}

	CollectionType getCollectionType() {
		return getIpInterface().getIsSnmpPrimary();
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

	List getNodeDsList() {
		return getNodeInfo().getDsList();
	}

	List getNodeOidList() throws CollectionError {
		return getNodeInfo().getOidList();
	}

}
