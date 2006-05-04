package org.opennms.netmgt.collectd;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.utils.AlphaNumeric;
import org.opennms.netmgt.utils.EventProxy;
import org.opennms.netmgt.utils.ParameterMap;

public class UpdateRRDs {

    private CollectionInterface m_iface;
    private SnmpNodeCollector m_nodeCollector;
    private SnmpIfCollector m_IfCollector;
    private Map m_parameters;
    private EventProxy m_eventProxy;
    private boolean m_forceRescan;
    private boolean m_rescanPending;

    void execute(CollectionInterface collectionInterface, SnmpNodeCollector snc, SnmpIfCollector sIfC, Map parms, EventProxy eproxy) {
        m_iface = collectionInterface;
        m_nodeCollector = snc;
        m_IfCollector = sIfC;
        m_parameters = parms;
        m_eventProxy = eproxy;
        
        /*
    	 * Write relevant collected SNMP statistics to RRD database First the
    	 * node level RRD info will be updated. Secondly the interface level RRD
    	 * info will be updated.
    	 */
    
        // Node data
        storeNodeData();
    
    	m_forceRescan = false;
        m_rescanPending = false;

        if (m_IfCollector != null) {
            
    		logIfAliasConfig();
    
            // get the snmpIfAliases
            if (m_iface.isForceRescanInProgress()) {
                m_rescanPending = true;
            }
    
    		/*
    		 * Retrieve list of SNMP collector entries generated for the remote
    		 * node's interfaces.
    		 */
    		List snmpCollectorEntries = m_IfCollector.getEntries();
            if (snmpCollectorEntries.size() == 0) {
    			log().warn(
    					"updateRRDs: No data retrieved for the interface "
    							+ m_iface.getInetAddress().getHostAddress());
    		}
    
    		// Iterate over the SNMP collector entries
    		Iterator iter = snmpCollectorEntries.iterator();
    		while (iter.hasNext()) {
    			SNMPCollectorEntry ifEntry = (SNMPCollectorEntry) iter.next();
    
    			// get the ifAlias if one exists
    			String aliasVal = getCurrentIfAliasSendingEventsIfNecessary(ifEntry);
    
    
                /*
                 * Use ifIndex to lookup the IfInfo object from the interface
                 * map.
                 */
                IfInfo ifInfo = (IfInfo) m_iface.getIfMap().get(new Integer(ifEntry.getIfIndex().intValue()));
                if (ifInfo == null) {
                    // no data needed for this interface
                    continue;
                }
    
                validateDsList(ifInfo);

                if (notScheduledForCollection(ifEntry, ifInfo) && !forceStoreByAlias(aliasVal)) {
                    logSkip(ifEntry, ifInfo);
                    continue;
                }

    			if (notScheduledForCollection(ifEntry, ifInfo) && forceStoreByAlias(aliasVal)) {
                	logStore(ifEntry, ifInfo);
                }
                
    			/*
    			 * Iterate over the interface datasource list and issue RRD
    			 * update commands to update each datasource which has a
    			 * corresponding value in the collected SNMP data.
    			 */
    			Iterator i = ifInfo.getDsList().iterator();
    			while (i.hasNext()) {
    				DataSource ds = (DataSource) i.next();
    
    				try {
    					String dsVal = ds.getRRDValue(ifEntry);
    
    					// Build RRD update command
    					if (dsVal == null) {
    						logNoDataRetrieved(ifEntry, ds);
    					} else {
    						if (shouldStoreByNode(ifEntry, ifInfo)) {
                                storeByNode(ifEntry, ifInfo, ds, dsVal);
    						}
    						if (shouldStoreByAlias()) {
    							storeByAlias(ifEntry, aliasVal, ds, dsVal);
    						}
    
    					}
    				} catch (IllegalArgumentException e) {
    					logUpdateFailed(ifEntry, ds, e);
    				}
    
    			} // end while(more datasources)
    		} // end while(more SNMP collector entries)
    	} // end if(ifCollector != null)
    
    	if (m_forceRescan) {
    		m_iface.sendForceRescanEvent(m_eventProxy);
    	}
    }

    private void logUpdateFailed(SNMPCollectorEntry ifEntry, DataSource ds, IllegalArgumentException e) {
        log().warn("buildRRDUpdateCmd: " + e.getMessage());
   
        log().warn(
        		"updateRRDs: call to buildRRDUpdateCmd() "
        				+ "failed for node/ifindex: " + m_iface.getNodeId()
        				+ "/" + ifEntry.getIfIndex().intValue() + " datasource: "
        				+ ds.getName());
    }

    private void storeByNode(SNMPCollectorEntry ifEntry, IfInfo ifInfo, DataSource ds, String dsVal) {
        String ifRepository = getRrdPath() + File.separator
        + String.valueOf(m_iface.getNodeId()) + File.separator
        + ifInfo.getLabel();
        if (ds.performUpdate(m_iface.getCollection(), m_iface.getInetAddress().getHostAddress(), ifRepository, ds.getName(), dsVal)) {
        	logUpdateFailed(ifEntry, ds);
        }
    }

    private void storeByAlias(SNMPCollectorEntry ifEntry, String aliasVal, DataSource ds, String dsVal) {
        if (isValidAlias(aliasVal)) {
        	String ifAliasRepository = getRrdPath() + File.separator
        			+ getDomain() + File.separator
        			+ aliasVal;
        	if (ds.performUpdate(m_iface.getCollection(), m_iface.getInetAddress()
        			.getHostAddress(), ifAliasRepository, ds
        			.getName(), dsVal)) {
        		logIfAliasUpdateFailed(ifEntry, aliasVal, ds);
        	}
        }
    }

    private boolean shouldStoreByAlias() {
        return getStoreByIfAlias().equals("true");
    }

    private boolean shouldStoreByNode(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        String byNode = getStoreByNodeID();
        if (notScheduledForCollection(ifEntry, ifInfo)) {
            if (byNode.equals("normal")) {
                byNode = "false";
            }                    
        } else {
            if (byNode.equals("normal")) {
                byNode = "true";
            }
        }
        
        return byNode.equals("true");
    }

    private void logIfAliasUpdateFailed(SNMPCollectorEntry ifEntry, String aliasVal, DataSource ds) {
        log()
        		.warn(
        				"updateRRDs: "
        						+ "ds.performUpdate() failed for "
        						+ "node/ifindex/domain/alias: "
        						+ m_iface.getNodeId()
        						+ "/"
        						+ ifEntry.getIfIndex().intValue()
        						+ "/"
        						+ getDomain()
        						+ "/"
        						+ aliasVal
        						+ " datasource: "
        						+ ds.getName());
    }

    private void logUpdateFailed(SNMPCollectorEntry ifEntry, DataSource ds) {
        log()
        		.warn(
        				"updateRRDs: "
        						+ "ds.performUpdate() failed for "
        						+ "node/ifindex: "
        						+ m_iface.getNodeId() + "/"
        						+ ifEntry.getIfIndex().intValue()
        						+ " datasource: "
        						+ ds.getName());
    }

    private void logNoDataRetrieved(SNMPCollectorEntry ifEntry, DataSource ds) {
        // Do nothing, no update is necessary
        if (log().isDebugEnabled()) {
        	log().debug(
        			"updateRRDs: Skipping update, "
        					+ "no data retrieved for "
        					+ "node/ifindex: " + m_iface.getNodeId()
        					+ "/" + ifEntry.getIfIndex().intValue()
        					+ " datasource: "
        					+ ds.getName());
        }
    }

    private void validateDsList(IfInfo ifInfo) {
        if (ifInfo.getDsList() == null) {
            throw new RuntimeException("Data Source list not "
                    + "available for primary IP addr "
                    + m_iface.getInetAddress().getHostAddress() + " and ifIndex "
                    + ifInfo.getIndex());
        }
    }

    private boolean forceStoreByAlias(String aliasVal) {
        return !getStorFlagOverride().equals("false") && (isValidAlias(aliasVal));
    }

    private boolean notScheduledForCollection(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        return (storingOnlyPrimary() && ifEntryIsNotPrimary(ifEntry)) || (storingOnlySelect() && ifaceIsNotSelect(ifInfo));
    }

    private boolean ifaceIsNotSelect(IfInfo ifInfo) {
        return ifInfo.getCollType().equals(CollectionType.NO_COLLECT);
    }

    private boolean storingOnlySelect() {
        return m_iface.getSnmpStorage().equals(SnmpCollector.SNMP_STORAGE_SELECT);
    }

    private boolean ifEntryIsNotPrimary(SNMPCollectorEntry ifEntry) {
        return ifEntry.getIfIndex().intValue() != m_iface.getIfIndex();
    }

    private boolean storingOnlyPrimary() {
        return m_iface.getSnmpStorage().equals(SnmpCollector.SNMP_STORAGE_PRIMARY);
    }

    private void logSkip(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        if (log().isDebugEnabled()) {
        	log()
        			.debug(
        					"updateRRDs: selectively storing "
        							+ "SNMP data for primary interface ("
        							+ m_iface.getIfIndex()
        							+ "), skipping ifIndex: "
        							+ Integer.toString(ifEntry.getIfIndex().intValue())
        							+ " because collType = "
        							+ ifInfo.getCollType());
        }
    }

    private void logSkipNonPrimary(SNMPCollectorEntry ifEntry) {
        if (log().isDebugEnabled()) {
        	log()
        			.debug(
        					"updateRRDs: only storing "
        							+ "SNMP data for primary interface ("
        							+ m_iface.getIfIndex()
        							+ "), skipping ifIndex: "
        							+ Integer.toString(ifEntry.getIfIndex().intValue()));
        }
    }

    private void logStore(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        if (log().isDebugEnabled()) {
            log()
                    .debug(
                            "updateRRDs: storFlagOverride "
                                    + "= true. Storing SNMP data for "
                                    + "interface " + Integer.toString(ifEntry.getIfIndex().intValue())
                                    + " with CollType = "
                                    + ifInfo.getCollType());
        }
    }

    private void logStoreNonPrimary(SNMPCollectorEntry ifEntry) {
        if (log().isDebugEnabled()) {
        	log()
        			.debug(
        					"updateRRDs: storFlagOverride "
        							+ "= true. Storing SNMP data for "
        							+ "non-primary interface "
        							+ Integer.toString(ifEntry.getIfIndex().intValue()));
        }
    }

    private String getCurrentIfAliasSendingEventsIfNecessary(SNMPCollectorEntry ifEntry) {
        String aliasVal = ifEntry.getValueForBase(SnmpCollector.IFALIAS_OID);
        if (isValidAlias(aliasVal)) {
        	aliasVal = aliasVal.trim();
   
        	/*
        	 * Check DB to see if ifAlias is current and flag a forced
        	 * rescan if not.
        	 */
        	if (!m_rescanPending) {
                Map snmpIfAliasMap = getIfAliasesFromDb(m_iface.getNodeId());
        		if (ifAliasChanged(Integer.toString(ifEntry.getIfIndex().intValue()), aliasVal, snmpIfAliasMap)) {
        			m_rescanPending = true;
        			m_forceRescan = true;
        			logForceRescan(ifEntry, aliasVal, snmpIfAliasMap);
        		}
        	}
        	if (getIfAliasComment() != null) {
        		int si = aliasVal.indexOf(getIfAliasComment());
        		if (si > -1) {
        			aliasVal = aliasVal.substring(0, si).trim();
        		}
        	}
        	if (isValidAlias(aliasVal)) {
        		aliasVal = AlphaNumeric.parseAndReplaceExcept(aliasVal,
        				SnmpCollector.nonAnRepl, SnmpCollector.AnReplEx);
        	}
        }
        
        logAlias(ifEntry.getValueForBase(SnmpCollector.IFALIAS_OID));

        return aliasVal;
    }

    private void logAlias(String alias) {
        if (log().isDebugEnabled()) {
            log().debug("getRRDIfAlias: ifAlias = " + alias);
        }
    }

    private void logForceRescan(SNMPCollectorEntry ifEntry, String aliasVal, Map snmpIfAliasMap) {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"Forcing rescan.  IfAlias " + aliasVal
        					+ " for index " + Integer.toString(ifEntry.getIfIndex().intValue())
        					+ " does not match DB value: "
        					+ snmpIfAliasMap.get(Integer.toString(ifEntry.getIfIndex().intValue())));
        }
    }

    private boolean isValidAlias(String aliasVal) {
        return aliasVal != null;
    }

    private boolean ifAliasChanged(String ifIdx, String aliasVal, Map snmpIfAliasMap) {
        return snmpIfAliasMap.get(ifIdx) == null || !snmpIfAliasMap.get(ifIdx).equals(aliasVal);
    }

    private void storeNodeData() {
        if (m_nodeCollector != null) {
        	log().debug("updateRRDs: processing node-level collection...");
        
            /*
        	 * Build path to node RRD repository. createRRD() will make the
        	 * appropriate directories if they do not already exist.
        	 */
        	String nodeRepository = getRrdPath() + File.separator + String.valueOf(m_iface.getNodeId());
        
        	SNMPCollectorEntry nodeEntry = m_nodeCollector.getEntry();
        
        	/*
        	 * Iterate over the node datasource list and issue RRD update
        	 * commands to update each datasource which has a corresponding
        	 * value in the collected SNMP data.
        	 */
        	Iterator it = m_iface.getNodeAttributeList().iterator();
        	while (it.hasNext()) {
                CollectionAttribute attr = (CollectionAttribute)it.next();
        		DataSource ds = attr.getDs();
        
        		try {
        			String dsVal = ds.getRRDValue(nodeEntry);
        			if (dsVal == null) {
        				// Do nothing, no update is necessary
        				logNoDataForValue(ds);
        			} else {
        				if (ds.performUpdate(m_iface.getCollection(), m_iface.getInetAddress().getHostAddress(), nodeRepository, ds.getName(), dsVal)) {
        					logUpdateFailed(ds);
        				}
        			}
        		} catch (IllegalArgumentException e) {
        			logExceptionOnUpdate(ds, e);
        		}
        
        	} // end while(more datasources)
        } // end if(nodeCollector != null)
    }

    private void logIfAliasConfig() {
        if (shouldStoreByAlias()) {
        	log()
            .debug(
            		"domain:storeByNodeID:storeByIfAlias:"
            				+ "storFlagOverride:ifAliasComment = "
            				+ getDomain() + ":" + getStoreByNodeID() + ":"
            				+ getStoreByIfAlias() + ":"
            				+ getStorFlagOverride() + ":"
            				+ getIfAliasComment());
        }
    }

    private String getIfAliasComment() {
        return ParameterMap.getKeyedString(m_parameters,
        		"ifAliasComment", null);
    }

    private String getStorFlagOverride() {
        return ParameterMap.getKeyedString(m_parameters,
        		"storFlagOverride", "false");
    }

    private String getStoreByIfAlias() {
        return ParameterMap.getKeyedString(m_parameters,
        		"storeByIfAlias", "false");
    }

    private String getStoreByNodeID() {
        return ParameterMap.getKeyedString(m_parameters,
        		"storeByNodeID", "normal");
    }

    private String getDomain() {
        return ParameterMap.getKeyedString(m_parameters, "domain",
        		"default");
    }

    private void logExceptionOnUpdate(DataSource ds1, IllegalArgumentException e1) {
        log().warn("getRRDValue: " + e1.getMessage());
        log().warn(
        		"updateRRDs: call to getRRDValue() failed "
        				+ "for node: " + m_iface.getNodeId() + " datasource: "
        				+ ds1.getName());
    }

    private void logUpdateFailed(DataSource ds1) {
        log().warn(
        		"updateRRDs: ds.performUpdate() "
        				+ "failed for node: " + m_iface.getNodeId()
        				+ " datasource: " + ds1.getName());
    }

    private void logNoDataForValue(DataSource ds1) {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"updateRRDs: Skipping update, no "
        					+ "data retrieved for nodeId: "
        					+ m_iface.getNodeId() + " datasource: "
        					+ ds1.getName());
        }
    }

    Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private String getRrdPath() {
        return DataCollectionConfigFactory.getInstance().getRrdPath();
    }

    /**
     * This method is responsible for retrieving an array of ifaliases indexed
     * by ifindex for the specified node
     * @param nodeID nodeID the nodeID of the node being checked
     */
    private Map getIfAliasesFromDb(int nodeID) {
    	java.sql.Connection dsConn = null;
    	Map ifAliasMap = new HashMap();
    	Category log = log();
        if (log.isDebugEnabled()) {
    		log.debug("building ifAliasMap for node " + nodeID);
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
    		log.error("Failed getting connection to the database.", e);
    		throw new UndeclaredThrowableException(e);
    	} finally {
    		// Done with the database so close the connection
    		try {
    			dsConn.close();
    		} catch (SQLException e) {
    			log
    					.info("SQLException while closing database connection",
    							e);
    		}
    	}
    	return ifAliasMap;
    }


}
