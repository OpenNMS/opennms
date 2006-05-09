package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.collectd.SnmpCollector.IfNumberTracker;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

public class CollectionAgent extends IPv4NetworkInterface {

    // the interface of the Agent
	private OnmsIpInterface m_iface;
    
    // collection trackers
    private SnmpNodeCollector m_nodeCollector;
    private IfNumberTracker m_ifNumber;
    private SnmpIfCollector m_ifCollector;

    // miscellaneous junk?
    private String m_collectionName;
	private CollectionSet m_collectionSet;
    private int m_maxVarsPerPdu = 0;
    private int m_ifCount = -1;

	public CollectionAgent(OnmsIpInterface iface) {
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
	
		if (getType() != CollectionAgent.TYPE_IPV4)
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

    List getCombinedInterfaceAttributes() {
        return m_collectionSet.getCombinedInterfaceAttributes();
    }

    List getNodeAttributeList() {
       return m_collectionSet.getAttributeList(); 
    }

    public int getMaxVarsPerPdu() {
        
        if (m_maxVarsPerPdu < 1) {
            m_maxVarsPerPdu = m_collectionSet.getMaxVarsPerPdu();
            log().info("using maxVarsPerPdu from dataCollectionConfig");
        }
        
        if (m_maxVarsPerPdu < 1) {
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(getInetAddress());
            m_maxVarsPerPdu = agentConfig.getMaxVarsPerPdu();
            log().info("using maxVarsPerPdu from snmpconfig");
        }
        
        if (m_maxVarsPerPdu < 1) {
            log().warn("MaxVarsPerPdu CANNOT BE LESS THAN 1.  Using 10");
            return 10;
        }

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

    public SnmpAgentConfig getAgentConfig() {
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(getInetAddress());
        agentConfig.setMaxVarsPerPdu(getMaxVarsPerPdu());
        return agentConfig;
    }

    public SnmpIfCollector getIfCollector() {
        if (m_ifCollector == null)
            m_ifCollector = createIfCollector();
        return m_ifCollector;
    }

    public IfNumberTracker getIfNumber() {
        if (m_ifNumber == null)
            m_ifNumber = createIfNumberTracker();
        return m_ifNumber;
    }

    public SnmpNodeCollector getNodeCollector() {
        if (m_nodeCollector == null)
            m_nodeCollector = createNodeCollector();
        return m_nodeCollector;
    }

    private SnmpNodeCollector createNodeCollector() {
        SnmpNodeCollector nodeCollector = null;
        if (!getNodeAttributeList().isEmpty()) {
        	nodeCollector = new SnmpNodeCollector(getInetAddress(), getNodeAttributeList());
        }
        return nodeCollector;
    }

    private IfNumberTracker createIfNumberTracker() {
        IfNumberTracker ifNumber = null;
        if (hasInterfaceDataToCollect()) {
            ifNumber = new IfNumberTracker();
        }
        return ifNumber;
    }

    private SnmpIfCollector createIfCollector() {
        SnmpIfCollector ifCollector = null;
        // construct the ifCollector
        if (hasInterfaceDataToCollect()) {
        	ifCollector = new SnmpIfCollector(getInetAddress(), getCombinedInterfaceAttributes());
        }
        return ifCollector;
    }

    boolean hasInterfaceDataToCollect() {
        return m_collectionSet.hasInterfaceDataToCollect();
    }

    CollectionTracker getCollectionTracker() {
        List trackers = new ArrayList(3);
       
        if (getIfNumber() != null) {
        	trackers.add(getIfNumber());
        }
        if (getNodeCollector() != null) {
        	trackers.add(getNodeCollector());
        }
        if (getIfCollector() != null) {
        	trackers.add(getIfCollector());
        }
       
        return new AggregateTracker(trackers);
    }

    SnmpWalker createWalker() {
        return SnmpUtils.createWalker(getAgentConfig(), "SnmpCollectors for " + getHostAddress(), getCollectionTracker());
    }

    void saveMaxVarsPerPdu(SnmpWalker walker) {
        setMaxVarsPerPdu(walker.getMaxVarsPerPdu());
    }

    void logStartedWalker() {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"collect: successfully instantiated "
        					+ "SnmpNodeCollector() for "
        					+ getHostAddress());
        }
    }

    void logFinishedWalker() {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"collect: node SNMP query for address "
        					+ getHostAddress() + " complete.");
        }
    }

    void verifySuccessfulWalk(SnmpWalker walker) throws CollectionWarning {
        if (walker.failed()) {
        	// Log error and return COLLECTION_FAILED
        	throw new CollectionWarning("collect: collection failed for "
        			+ getHostAddress());
        }
    }

    void warnOfInterruption(InterruptedException e) throws CollectionWarning {
        Thread.currentThread().interrupt();
        throw new CollectionWarning("collect: Collection of node SNMP "
        		+ "data for interface " + getHostAddress()
        		+ " interrupted!", e);
    }

    void collect() throws CollectionWarning {
    	try {
    
            // now collect the data
    		SnmpWalker walker = createWalker();
    		walker.start();
    
            logStartedWalker();
    
    		// wait for collection to finish
    		walker.waitFor();
    
    		logFinishedWalker();
    
    		// Was the collection successful?
    		verifySuccessfulWalk(walker);
    
    		saveMaxVarsPerPdu(walker);
            
    	} catch (InterruptedException e) {
    		warnOfInterruption(e);
    	}
    }

    void logIfCounts() {
        log().debug("collect: nodeId: " + getNodeId()
        				+ " interface: " + getHostAddress()
        				+ " ifCount: " + getIfNumber().getIfNumber() 
                       + " savedIfCount: " + getSavedIfCount());
    }

    boolean ifCountHasChanged() {
        return (getSavedIfCount() != -1) && (getIfNumber().getIfNumber() != getSavedIfCount());
    }

    IfInfo getIfInfo(int ifIndex) {
        return (IfInfo) getIfMap().get(new Integer(ifIndex));
    }



}
