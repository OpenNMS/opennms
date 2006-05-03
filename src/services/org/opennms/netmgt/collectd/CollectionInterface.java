package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.poller.IPv4NetworkInterface;

public class CollectionInterface extends IPv4NetworkInterface {

	private OnmsIpInterface m_iface;
    private String m_collectionName;
	private CollectionSet m_collectionSet;
    private int m_maxVarsPerPdu;
    private int m_ifCount = -1;

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
    
    String getCollection() {
        return m_collectionName;
    }
    
    public void setCollection(String collectionName) {
        m_collectionName = collectionName;
    }

	InetAddress getInetAddress() {
	
		if (getType() != CollectionInterface.TYPE_IPV4)
			throw new RuntimeException("Unsupported interface type, "
					+ "only TYPE_IPV4 currently supported");
	
		return (InetAddress) getAddress();
	}

	String getSnmpStorage() {
        return m_collectionSet.getStorageFlag();
	}

	public void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
		if (log().isDebugEnabled()) {
			log().debug("maxVarsPerPdu=" + maxVarsPerPdu);
		}
	}

	String getHostAddress() {
		return getInetAddress().getHostAddress();
	}

	public void setSavedIfCount(int ifCount) {
        m_ifCount = ifCount;
	}

	int getSavedIfCount() {
        return m_ifCount;
	}

	boolean hasInterfaceOids() {
		return m_collectionSet.hasInterfaceOids();
	}

	Map getIfMap() {
		return m_collectionSet.getIfMap();
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
        return m_collectionSet.getCombinedInterfaceOids();
	}

    List getNodeDsList() {
        return m_collectionSet.getDsList();
	}

    List getNodeOidList() throws CollectionError {
        return m_collectionSet.getOidList();
	}

    public int getMaxVarsPerPdu() {
        if (m_maxVarsPerPdu == -1)
            m_maxVarsPerPdu = m_collectionSet.getMaxVarsPerPdu();
        return m_maxVarsPerPdu;
    }

    private void logCompletion() {
    	
    	if (log().isDebugEnabled()) {
    		log().debug(
    				"initialize: initialization completed: nodeid = " + getNodeId()
    				+ ", address = " + getHostAddress()
    				+ ", primaryIfIndex = " + getIfIndex()
    				+ ", isSnmpPrimary = " + getCollectionType()
    				+ ", sysoid = " + getSysObjectId()
    				);
    	}
    
    }

    private void verifyCollectionIsNecessary() {
        m_collectionSet.verifyCollectionIsNecessary(this);
    }

    private void validateSysObjId() {
    	if (getSysObjectId() == null) {
    		throw new RuntimeException("System Object ID for interface "
    				+ getHostAddress()
    				+ " does not exist in the database.");
    	}
    }

    private void logCollectionParms() {
    	if (log().isDebugEnabled()) {
    		log().debug(
    				"initialize: db retrieval info: nodeid = " + getNodeId()
    				+ ", address = " + getHostAddress()
    				+ ", primaryIfIndex = " + getIfIndex()
    				+ ", isSnmpPrimary = " + getCollectionType()
    				+ ", sysoid = " + getSysObjectId()
    				);
    	}
    }

    private void validateIsSnmpPrimary() {
    	if (!CollectionType.PRIMARY.equals(getCollectionType())) {
    		throw new RuntimeException("Interface "
    				+ getHostAddress()
    				+ " is not the primary SNMP interface for nodeid "
    				+ getNodeId());
    	}
    }

    private void validatePrimaryIfIndex() {
    	if (getIfIndex() == -1) {
    		// allow this for nodes without ipAddrTables
    		// throw new RuntimeException("Unable to retrieve ifIndex for
    		// interface " + ipAddr.getHostAddress());
    		if (log().isDebugEnabled()) {
    			log().debug(
    					"initialize: db retrieval info: node " + getNodeId()
    					+ " does not have a legitimate "
    					+ "primaryIfIndex.  Assume node does not "
    					+ "supply ipAddrTable and continue...");
    		}
    	}
    }

    private void validateAgent() {
        logCollectionParms();
        validateIsSnmpPrimary();
        validatePrimaryIfIndex();
        validateSysObjId();
        verifyCollectionIsNecessary();
        logCompletion();
    }

    private void createCollectionSet() {
        m_collectionSet = new CollectionSet(this, getCollection());
    }

    public void initialize() {
        createCollectionSet();
    	validateAgent();
    }
    
    public String toString() {
        return getHostAddress();
    }

}
