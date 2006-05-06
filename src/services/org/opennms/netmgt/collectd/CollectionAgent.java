package org.opennms.netmgt.collectd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.xml.event.Event;

public class CollectionAgent extends IPv4NetworkInterface {

	private OnmsIpInterface m_iface;
    private String m_collectionName;
	private CollectionSet m_collectionSet;
    private int m_maxVarsPerPdu;
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
    
    List getCombinedInterfaceAttributes() {
        return m_collectionSet.getCombinedInterfaceAttributes();
    }

    List getNodeAttributeList() {
       return m_collectionSet.getAttributeList(); 
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

    /**
     * This method is responsible for determining if a forced rescan has been
     * started, but is not yet complete for the given nodeID
     * @param m_iface TODO
     * @param nodeID TODO
     * @param addr TODO
     * @param method TODO
     * @param int
     *            nodeID the nodeID of the node being checked
     */
    boolean isForceRescanInProgress() {
        int nodeID = getNodeId();
        String addr = getHostAddress();
    	java.sql.Connection dsConn = null;
    	boolean force = true;
    
        try {
    		dsConn = DataSourceFactory.getInstance().getConnection();
    
    		PreparedStatement stmt1 = dsConn
    				.prepareStatement(SnmpCollector.SQL_GET_LATEST_FORCED_RESCAN_EVENTID);
    		PreparedStatement stmt2 = dsConn
    				.prepareStatement(SnmpCollector.SQL_GET_LATEST_RESCAN_COMPLETED_EVENTID);
    		stmt1.setInt(1, nodeID);
    		stmt1.setString(2, addr);
    		stmt2.setInt(1, nodeID);
    		try {
    			// Issue database query
    			ResultSet rs1 = stmt1.executeQuery();
    			if (rs1.next()) {
    				int forcedRescanEventId = rs1.getInt(1);
    				try {
    					ResultSet rs2 = stmt2.executeQuery();
    					if (rs2.next()) {
    						if (rs2.getInt(1) > forcedRescanEventId) {
    							force = false;
    						} else {
    							if (log().isDebugEnabled()) {
    								log().debug("Rescan already pending on "
    										+ "node " + nodeID);
    							}
    						}
    					}
    				} catch (SQLException e) {
    					throw e;
    				} finally {
    					stmt2.close();
    				}
    			} else {
    				force = false;
    			}
    		} catch (SQLException e) {
    			throw e;
    		} finally {
    			stmt1.close();
    		}
    	} catch (SQLException e) {
    		log().error("Failed getting connection to the database.", e);
    		throw new UndeclaredThrowableException(e);
    	} finally {
    		// Done with the database so close the connection
    		try {
    			dsConn.close();
    		} catch (SQLException e) {
    			log()
    					.info("SQLException while closing database connection",
    							e);
    		}
    	}
    	return force;
    }

    String determineLocalHostName() {
    	// Get local host name (used when generating threshold events)
    	try {
    		return InetAddress.getLocalHost().getHostName();
    	} catch (UnknownHostException e) {
    		log().warn("initialize: Unable to resolve local host name.", e);
    		return "unresolved.host";
    	}
    }
    
    Event createForceRescanEvent() {
        // create the event to be sent
        Event newEvent = new Event();
        
        newEvent.setUei(EventConstants.FORCE_RESCAN_EVENT_UEI);
        
        newEvent.setSource("SNMPServiceMonitor");
        
        newEvent.setInterface(this.getHostAddress());
        
        newEvent.setService(SnmpCollector.SERVICE_NAME);
        
        newEvent.setHost(this.determineLocalHostName());
        
        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));
        
        newEvent.setNodeid(this.getNodeId());
        return newEvent;
    }

    /**
     * This method is responsible for building a Capsd forceRescan event object
     * and sending it out over the EventProxy.
     * @param ifAddress
     *            interface address to which this event pertains
     * @param nodeId TODO
     * @param eventProxy
     *            proxy over which an event may be sent to eventd
     */
    void sendForceRescanEvent(EventProxy eventProxy) {
    	Category log = log();
        // Log4j category
    	if (log.isDebugEnabled()) {
    		log.debug("generateForceRescanEvent: interface = " + getHostAddress());
    	}
    
    	Event newEvent = createForceRescanEvent();
    
    	// Send event via EventProxy
    	try {
            eventProxy.send(newEvent);
    	} catch (EventProxyException e) {
    		log.error("generateForceRescanEvent: Unable to send "
    				+ "forceRescan event.", e);
    	}
    }


}
