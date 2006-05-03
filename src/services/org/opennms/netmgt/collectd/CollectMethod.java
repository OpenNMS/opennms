package org.opennms.netmgt.collectd;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.collectd.SnmpCollector.IfNumberTracker;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.utils.AlphaNumeric;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.EventProxyException;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.xml.event.Event;

public class CollectMethod {

    int execute(CollectionInterface iface, EventProxy eproxy, Map parameters) {
        try {
    		// Collect node and interface MIB data from the remote agent
    
    		SnmpNodeCollector nodeCollector = null;
    		// construct the nodeCollector
    		if (!iface.getNodeOidList().isEmpty()) {
    			nodeCollector = new SnmpNodeCollector(iface.getInetAddress(), iface.getNodeOidList());
    		}
    
    		IfNumberTracker ifNumber = null;
    		SnmpIfCollector ifCollector = null;
    		// construct the ifCollector
    		if (iface.hasInterfaceOids()) {
    			ifCollector = new SnmpIfCollector(iface.getInetAddress(), iface.getCombinedInterfaceOids());
    			ifNumber = new IfNumberTracker();
    		}
    
    		collectData(iface, ifNumber, nodeCollector, ifCollector);
    
            if (iface.hasInterfaceOids()) {
    			int savedIfCount = iface.getSavedIfCount();
    
    			int ifCount = ifNumber.getIfNumber();
    
    			iface.setSavedIfCount(ifCount);
    
    			log().debug(
    					"collect: nodeId: " + iface.getNodeId()
    							+ " interface: " + iface.getHostAddress()
    							+ " ifCount: " + ifCount + " savedIfCount: "
    							+ savedIfCount);
    
    			/*
    			 * If saved interface count differs from the newly retreived
    			 * interface count the following must occur: 1. generate
    			 * forceRescan event so Capsd will rescan the node, update the
    			 * database, and generate the appropriate events back to the
    			 * poller.
    			 */
    			if ((savedIfCount != -1) && (ifCount != savedIfCount)) {
    				if (!isForceRescanInProgress(
    						iface.getNodeId(),
    						iface.getHostAddress())) {
    					log()
    							.info(
    									"Number of interfaces on primary SNMP "
    											+ "interface "
    											+ iface.getHostAddress()
    											+ " has changed, generating 'ForceRescan' event.");
    					generateForceRescanEvent(iface.getHostAddress(),
    							iface.getNodeId(), eproxy);
    				}
    			}
    		}
    
    		// Update RRD with values retrieved in SNMP collection
    		boolean rrdError = updateRRDs(iface.getCollection(), iface,
    				nodeCollector, ifCollector, parameters, eproxy);
    
    		if (rrdError) {
    			log().warn(
    					"collect: RRD error during update for "
    							+ iface.getHostAddress());
    		}
    
    		// return the status of the collection
    		return ServiceCollector.COLLECTION_SUCCEEDED;
    	} catch (CollectionError e) {
    		return collectionError(e);
    	} catch (CollectionWarning e) {
    		return collectionWarning(e);
    	} catch (Throwable t) {
    		return unexpected(iface, t);
    	}
    }

    Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private void collectData(CollectionInterface iface, CollectionTracker ifNumber, SnmpNodeCollector nodeCollector, SnmpIfCollector ifCollector) throws CollectionWarning {
    	try {
    		InetAddress address = iface.getInetAddress();
    		List trackers = new ArrayList(3);
    
    		if (ifNumber != null) {
    			trackers.add(ifNumber);
    		}
    		if (nodeCollector != null) {
    			trackers.add(nodeCollector);
    		}
    		if (ifCollector != null) {
    			trackers.add(ifCollector);
    		}
    
    		SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance()
    				.getAgentConfig(address);
            agentConfig.setMaxVarsPerPdu(iface.getMaxVarsPerPdu());
    
    		// now collect the data
    		SnmpWalker walker = SnmpUtils.createWalker(agentConfig,
    				"SnmpCollectors for " + address.getHostAddress(),
    				(CollectionTracker[]) trackers
    						.toArray(new CollectionTracker[trackers.size()]));
    		walker.start();
    
            if (log().isDebugEnabled()) {
    			log().debug(
    					"collect: successfully instantiated "
    							+ "SnmpNodeCollector() for "
    							+ iface.getHostAddress());
    		}
    
    		// wait for collection to finish
    		walker.waitFor();
    
    		if (log().isDebugEnabled()) {
    			log().debug(
    					"collect: node SNMP query for address "
    							+ iface.getHostAddress() + " complete.");
    		}
    
    		// Was the node collection successful?
    		if (walker.failed()) {
    			// Log error and return COLLECTION_FAILED
    			throw new CollectionWarning("collect: collection failed for "
    					+ iface.getHostAddress());
    		}
    
    		iface.setMaxVarsPerPdu(walker.getMaxVarsPerPdu());
    	} catch (InterruptedException e) {
    		Thread.currentThread().interrupt();
    		throw new CollectionWarning("collect: Collection of node SNMP "
    				+ "data for interface " + iface.getHostAddress()
    				+ " interrupted!", e);
    	}
    }

    /**
     * This method is responsible for building an RRDTool style 'update' command
     * which is issued via the RRD JNI interface in order to push the latest
     * SNMP-collected values into the interface's RRD database.
     * 
     * @param collectionName
     *            SNMP data Collection name from 'datacollection-config.xml'
     * @param iface
     *            CollectionInterface object of the interface currently being
     *            polled
     * @param nodeCollector
     *            Node level MIB data collected via SNMP for the polled
     *            interface
     * @param ifCollector
     *            Interface level MIB data collected via SNMP for the polled
     *            interface
     * @param parms TODO
     * @param eproxy TODO
     * @throws CollectionError
     * @exception RuntimeException
     *                Thrown if the data source list for the interface is null.
     */
    private boolean updateRRDs(String collectionName, CollectionInterface iface, SnmpNodeCollector nodeCollector, SnmpIfCollector ifCollector, Map parms, EventProxy eproxy) throws CollectionError {
    	// Log4j category
    	InetAddress ipaddr = iface.getInetAddress();
    
    	// Retrieve SNMP storage attribute
    	String snmpStorage = iface.getSnmpStorage();
    
    	// Get primary interface index from NodeInfo object
    	int nodeId = iface.getNodeId();
    	int primaryIfIndex = iface.getIfIndex();
    
    	// Retrieve interface map attribute
    	Map ifMap = iface.getIfMap();
    	verifyIfMap(iface);
    
    	/*
    	 * Write relevant collected SNMP statistics to RRD database First the
    	 * node level RRD info will be updated. Secondly the interface level RRD
    	 * info will be updated.
    	 */
    	boolean rrdError = false;
    
        // Node data
    	if (nodeCollector != null) {
    		log().debug("updateRRDs: processing node-level collection...");
    
    		/*
    		 * Build path to node RRD repository. createRRD() will make the
    		 * appropriate directories if they do not already exist.
    		 */
    		String nodeRepository = getRrdPath() + File.separator
    				+ String.valueOf(nodeId);
    
    		SNMPCollectorEntry nodeEntry = nodeCollector.getEntry();
    
    		/*
    		 * Iterate over the node datasource list and issue RRD update
    		 * commands to update each datasource which has a corresponding
    		 * value in the collected SNMP data.
    		 */
    		Iterator iter = iface.getNodeDsList().iterator();
    		while (iter.hasNext()) {
    			DataSource ds = (DataSource) iter.next();
    
    			try {
    				String dsVal = getRRDValue(ds, nodeEntry);
    				if (dsVal == null) {
    					// Do nothing, no update is necessary
    					if (log().isDebugEnabled()) {
    						log().debug(
    								"updateRRDs: Skipping update, no "
    										+ "data retrieved for nodeId: "
    										+ nodeId + " datasource: "
    										+ ds.getName());
    					}
    				} else {
    					// createRRD(collectionName, ipaddr, nodeRepository,
    					// ds);
    					if (ds.performUpdate(collectionName, ipaddr
    							.getHostAddress(), nodeRepository,
    							ds.getName(), dsVal)) {
    						log().warn(
    								"updateRRDs: ds.performUpdate() "
    										+ "failed for node: " + nodeId
    										+ " datasource: " + ds.getName());
    						rrdError = true;
    					}
    				}
    			} catch (IllegalArgumentException e) {
    				log().warn("getRRDValue: " + e.getMessage());
    
    				// Set rrdError flag
    				rrdError = true;
    				log().warn(
    						"updateRRDs: call to getRRDValue() failed "
    								+ "for node: " + nodeId + " datasource: "
    								+ ds.getName());
    			}
    
    		} // end while(more datasources)
    	} // end if(nodeCollector != null)
    
    	// Interface-specific data
    	boolean forceRescan = false;
    	boolean rescanPending = false;
    	Map SnmpIfAliasMap = new HashMap();
    
    	if (ifCollector != null) {
    		String domain = ParameterMap.getKeyedString(parms, "domain",
    				"default");
    		String storeByNodeID = ParameterMap.getKeyedString(parms,
    				"storeByNodeID", "normal");
    		String storeByIfAlias = ParameterMap.getKeyedString(parms,
    				"storeByIfAlias", "false");
    		String storFlagOverride = ParameterMap.getKeyedString(parms,
    				"storFlagOverride", "false");
    		String ifAliasComment = ParameterMap.getKeyedString(parms,
    				"ifAliasComment", null);
    
    		if (log().isDebugEnabled() && storeByIfAlias.equals("true")) {
    			log()
    					.debug(
    							"domain:storeByNodeID:storeByIfAlias:"
    									+ "storFlagOverride:ifAliasComment = "
    									+ domain + ":" + storeByNodeID + ":"
    									+ storeByIfAlias + ":"
    									+ storFlagOverride + ":"
    									+ ifAliasComment);
    		}
    
    		/*
    		 * Retrieve list of SNMP collector entries generated for the remote
    		 * node's interfaces.
    		 */
    		List snmpCollectorEntries = ifCollector.getEntries();
    		if (snmpCollectorEntries == null
    				|| snmpCollectorEntries.size() == 0) {
    			log().warn(
    					"updateRRDs: No data retrieved for the interface "
    							+ ipaddr.getHostAddress());
    		}
    
    		// get the snmpIfAliases
    		if (isForceRescanInProgress(nodeId, ipaddr.getHostAddress())) {
    			rescanPending = true;
    		} else {
    			SnmpIfAliasMap = getIfAliasesFromDb(nodeId);
    		}
    
    		// Iterate over the SNMP collector entries
    		Iterator iter = snmpCollectorEntries.iterator();
    		while (iter.hasNext()) {
    			SNMPCollectorEntry ifEntry = (SNMPCollectorEntry) iter.next();
    
    			int ifIndex = ifEntry.getIfIndex().intValue();
    			String ifIdx = Integer.toString(ifIndex);
    
    			// get the ifAlias if one exists
    			String aliasVal = getRRDIfAlias(ifIdx, ifEntry);
    			if (aliasVal != null && !aliasVal.equals("")) {
    				aliasVal = aliasVal.trim();
    
    				/*
    				 * Check DB to see if ifAlias is current and flag a forced
    				 * rescan if not.
    				 */
    				if (!rescanPending) {
    					if (SnmpIfAliasMap.get(ifIdx) == null
    							|| !SnmpIfAliasMap.get(ifIdx).equals(aliasVal)) {
    						rescanPending = true;
    						forceRescan = true;
    						if (log().isDebugEnabled()) {
    							log().debug(
    									"Forcing rescan.  IfAlias " + aliasVal
    											+ " for index " + ifIdx
    											+ " does not match DB value: "
    											+ SnmpIfAliasMap.get(ifIdx));
    						}
    					}
    				}
    				if (ifAliasComment != null) {
    					int si = aliasVal.indexOf(ifAliasComment);
    					if (si > -1) {
    						aliasVal = aliasVal.substring(0, si).trim();
    					}
    				}
    				if (aliasVal != null && !aliasVal.equals("")) {
    					aliasVal = AlphaNumeric.parseAndReplaceExcept(aliasVal,
    							SnmpCollector.nonAnRepl, SnmpCollector.AnReplEx);
    				}
    			}
    
    			boolean override = true;
    			if (storFlagOverride.equals("false") || (aliasVal == null)
    					|| aliasVal.equals("")) {
    				override = false;
    			}
    			String byNode = storeByNodeID;
    
    			/*
    			 * Are we storing SNMP data for all interfaces or primary
    			 * interface only? If only storing for primary interface only
    			 * proceed if current ifIndex is equal to the ifIndex of the
    			 * primary SNMP interface.
    			 */
    			if (snmpStorage.equals(SnmpCollector.SNMP_STORAGE_PRIMARY)) {
    				if (ifIndex != primaryIfIndex) {
    					if (override) {
    						if (log().isDebugEnabled()) {
    							log()
    									.debug(
    											"updateRRDs: storFlagOverride "
    													+ "= true. Storing SNMP data for "
    													+ "non-primary interface "
    													+ ifIdx);
    						}
    					} else {
    						if (log().isDebugEnabled()) {
    							log()
    									.debug(
    											"updateRRDs: only storing "
    													+ "SNMP data for primary interface ("
    													+ primaryIfIndex
    													+ "), skipping ifIndex: "
    													+ ifIdx);
    						}
    						continue;
    					}
    					if (byNode.equals("normal")) {
    						byNode = "false";
    					}
    				}
    			}
    
    			/*
    			 * Use ifIndex to lookup the IfInfo object from the interface
    			 * map.
    			 */
    			IfInfo ifInfo = (IfInfo) ifMap.get(new Integer(ifIndex));
    			if (ifInfo == null) {
    				// no data needed for this interface
    				continue;
    			}
    
    			if (snmpStorage.equals(SnmpCollector.SNMP_STORAGE_SELECT)) {
    				if (ifInfo.getCollType().equals(CollectionType.NO_COLLECT)) {
    					if (override) {
    						if (log().isDebugEnabled()) {
    							log()
    									.debug(
    											"updateRRDs: storFlagOverride "
    													+ "= true. Storing SNMP data for "
    													+ "interface " + ifIdx
    													+ " with CollType = "
    													+ ifInfo.getCollType());
    						}
    					} else {
    						if (log().isDebugEnabled()) {
    							log()
    									.debug(
    											"updateRRDs: selectively storing "
    													+ "SNMP data for primary interface ("
    													+ primaryIfIndex
    													+ "), skipping ifIndex: "
    													+ ifIdx
    													+ " because collType = "
    													+ ifInfo.getCollType());
    						}
    						continue;
    					}
    					if (byNode.equals("normal")) {
    						byNode = "false";
    					}
    				}
    			}
    			if (byNode.equals("normal")) {
    				byNode = "true";
    			}
    
    			if (ifInfo.getDsList() == null) {
    				throw new RuntimeException("Data Source list not "
    						+ "available for primary IP addr "
    						+ ipaddr.getHostAddress() + " and ifIndex "
    						+ ifInfo.getIndex());
    			}
    
    			/*
    			 * Iterate over the interface datasource list and issue RRD
    			 * update commands to update each datasource which has a
    			 * corresponding value in the collected SNMP data.
    			 */
    			Iterator i = ifInfo.getDsList().iterator();
    			while (i.hasNext()) {
    				DataSource ds = (DataSource) i.next();
    
    				/*
    				 * Build path to interface RRD repository. createRRD() will
    				 * make the appropriate directories if they do not already
    				 * exist.
    				 */
    				String ifRepository = getRrdPath() + File.separator
    						+ String.valueOf(nodeId) + File.separator
    						+ ifInfo.getLabel();
    
    				try {
    					String dsVal = getRRDValue(ds, ifEntry);
    
    					// Build RRD update command
    					if (dsVal == null) {
    						// Do nothing, no update is necessary
    						if (log().isDebugEnabled()) {
    							log().debug(
    									"updateRRDs: Skipping update, "
    											+ "no data retrieved for "
    											+ "node/ifindex: " + nodeId
    											+ "/" + ifIndex
    											+ " datasource: "
    											+ ds.getName());
    						}
    					} else {
    						/*
    						 * Call createRRD() to create RRD if it doesn't
    						 * already exist.
    						 */
    						// createRRD(collectionName, ipaddr, ifRepository,
    						// ds);
    						if (byNode.equals("true")) {
    							if (ds.performUpdate(collectionName, ipaddr
    									.getHostAddress(), ifRepository, ds
    									.getName(), dsVal)) {
    								log()
    										.warn(
    												"updateRRDs: "
    														+ "ds.performUpdate() failed for "
    														+ "node/ifindex: "
    														+ nodeId + "/"
    														+ ifIndex
    														+ " datasource: "
    														+ ds.getName());
    								rrdError = true;
    							}
    						}
    						if (storeByIfAlias.equals("true")) {
    							if ((aliasVal != null) && !aliasVal.equals("")) {
    								ifRepository = getRrdPath() + File.separator
    										+ domain + File.separator
    										+ aliasVal;
    								if (ds.performUpdate(collectionName, ipaddr
    										.getHostAddress(), ifRepository, ds
    										.getName(), dsVal)) {
    									log()
    											.warn(
    													"updateRRDs: "
    															+ "ds.performUpdate() failed for "
    															+ "node/ifindex/domain/alias: "
    															+ nodeId
    															+ "/"
    															+ ifIndex
    															+ "/"
    															+ domain
    															+ "/"
    															+ aliasVal
    															+ " datasource: "
    															+ ds.getName());
    									rrdError = true;
    								}
    							}
    						}
    
    					}
    				} catch (IllegalArgumentException e) {
    					log().warn("buildRRDUpdateCmd: " + e.getMessage());
    
    					// Set rrdError flag
    					rrdError = true;
    					log().warn(
    							"updateRRDs: call to buildRRDUpdateCmd() "
    									+ "failed for node/ifindex: " + nodeId
    									+ "/" + ifIndex + " datasource: "
    									+ ds.getName());
    				}
    
    			} // end while(more datasources)
    		} // end while(more SNMP collector entries)
    	} // end if(ifCollector != null)
    
    	if (forceRescan) {
    		generateForceRescanEvent(ipaddr.getHostAddress(), iface.getNodeId(), eproxy);
    	}
    	return rrdError;
    }

    private String getRrdPath() {
        return DataCollectionConfigFactory.getInstance().getRrdPath();
    }

    private int collectionError(CollectionError e) {
        if (e.getCause() == null) {
    		log().error(e.getMessage());
    	} else {
    		log().error(e.getMessage(), e.getCause());
    	}
    	return ServiceCollector.COLLECTION_FAILED;
    }

    private int collectionWarning(CollectionWarning e) {
        if (e.getCause() == null) {
    		log().warn(e.getMessage());
    	} else {
    		log().warn(e.getMessage(), e.getCause());
    	}
    	return ServiceCollector.COLLECTION_FAILED;
    }

    private int unexpected(CollectionInterface iface, Throwable t) {
    	log().error(
    			"Unexpected error during node SNMP collection for "
    					+ iface.getHostAddress(), t);
    	return ServiceCollector.COLLECTION_FAILED;
    }

    /**
     * This method is responsible for determining if a forced rescan has been
     * started, but is not yet complete for the given nodeID
     * @param nodeID TODO
     * @param addr TODO
     * @param int
     *            nodeID the nodeID of the node being checked
     */
    private boolean isForceRescanInProgress(int nodeID, String addr) {
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

    private String determineLocalHostName() {
    	// Get local host name (used when generating threshold events)
    	try {
    		return InetAddress.getLocalHost().getHostName();
    	} catch (UnknownHostException e) {
    		log().warn("initialize: Unable to resolve local host name.", e);
    		return "unresolved.host";
    	}
    }

    /**
     * This method is responsible for building a Capsd forceRescan event object
     * and sending it out over the EventProxy.
     * 
     * @param ifAddress
     *            interface address to which this event pertains
     * @param nodeId TODO
     * @param eventProxy
     *            proxy over which an event may be sent to eventd
     */
    private void generateForceRescanEvent(String ifAddress, int nodeId, EventProxy eventProxy) {
    	// Log4j category
    	if (log().isDebugEnabled()) {
    		log().debug("generateForceRescanEvent: interface = " + ifAddress);
    	}
    
    	Event newEvent = createForceRescanEvent(ifAddress, nodeId);
    
    	// Send event via EventProxy
    	try {
    		eventProxy.send(newEvent);
    	} catch (EventProxyException e) {
    		log().error("generateForceRescanEvent: Unable to send "
    				+ "forceRescan event.", e);
    	}
    }

    private Event createForceRescanEvent(String ifAddress, int nodeId) {
        // create the event to be sent
        Event newEvent = new Event();

        newEvent.setUei(EventConstants.FORCE_RESCAN_EVENT_UEI);

        newEvent.setSource("SNMPServiceMonitor");

        newEvent.setInterface(ifAddress);

        newEvent.setService(SnmpCollector.SERVICE_NAME);

        if (determineLocalHostName() != null) {
            newEvent.setHost(determineLocalHostName());
        }

        newEvent.setTime(EventConstants.formatToString(new java.util.Date()));

        newEvent.setNodeid(nodeId);
        return newEvent;
    }

    private void verifyIfMap(CollectionInterface iface) throws CollectionError {
        Map ifMap = iface.getIfMap();
    	if (ifMap == null) {
    		throw new CollectionError("Interface map not available for "
    				+ "interface " + iface.getHostAddress());
    	}
    }

    /**
     * @param log
     * @param dsVal
     * @param ds
     * @param collectorEntry
     * @return
     * @throws Exception
     */
    private String getRRDValue(DataSource ds, SNMPCollectorEntry collectorEntry)
    		throws IllegalArgumentException {
    	Category log = log();
    
    	// Make sure we have an actual object id value.
    	if (ds.getOid() == null) {
    		return null;
    	}
    
    	String instance = null;
    	if (ds.getInstance().equals(MibObject.INSTANCE_IFINDEX)) {
    		instance = collectorEntry.getIfIndex().toString();
    	} else {
    		instance = ds.getInstance();
    	}
    
    	String fullOid = SnmpObjId.get(ds.getOid(), instance).toString();
    
    	SnmpValue snmpVar = collectorEntry.getValue(fullOid);
    	if (snmpVar == null) {
    		// No value retrieved matching this oid
    		return null;
    	}
    
    	if (log.isDebugEnabled()) {
    		log.debug("issueRRDUpdate: name:oid:value - " + ds.getName() + ":"
    				+ fullOid + ":" + snmpVar.toString());
    	}
    
    	return ds.getStorableValue(snmpVar);
    }

    /**
     * This method is responsible for retrieving an array of ifaliases indexed
     * by ifindex for the specified node
     * @param nodeID nodeID the nodeID of the node being checked
     */
    private Map getIfAliasesFromDb(int nodeID) {
    	java.sql.Connection dsConn = null;
    	Map ifAliasMap = new HashMap();
    	if (log().isDebugEnabled()) {
    		log().debug("building ifAliasMap for node " + nodeID);
    	}
    
    	try {
    		dsConn = DataSourceFactory.getInstance().getConnection();
    
    		PreparedStatement stmt = dsConn
    				.prepareStatement(SnmpCollector.SQL_GET_SNMPIFALIASES);
    		stmt.setInt(1, nodeID);
    		try {
    			ResultSet rs = stmt.executeQuery();
    			while (rs.next()) {
    				ifAliasMap.put(rs.getString(1), rs.getString(2));
    			}
    		} catch (SQLException e) {
    			throw e;
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
    	return ifAliasMap;
    }

    /**
     * @param oid
     * @param instance
     * @param collectorEntry
     * @return
     * @throws Exception
     */
    private String getRRDIfAlias(String instance, SNMPCollectorEntry collectorEntry) throws IllegalArgumentException {
    	Category log = log();
    	if (instance.equals("")) {
    		return null;
    	}
    
    	String fullOid = SnmpCollector.IFALIAS_OID + "." + instance;
    
    	String snmpVar = collectorEntry.getDisplayString(fullOid);
    
    	if (snmpVar == null || snmpVar.equals("")) {
    		// No value retrieved matching this oid
    		return null;
    	}
    
    	if (log.isDebugEnabled()) {
    		log.debug("getRRDIfAlias: ifAlias = " + snmpVar);
    	}
    	return snmpVar;
    }

}
