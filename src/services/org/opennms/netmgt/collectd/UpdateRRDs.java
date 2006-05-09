//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.utils.ParameterMap;
import org.springframework.jdbc.core.JdbcTemplate;

public class UpdateRRDs {

    private CollectionAgent m_agent;

    private Map m_parameters;

    private ForceRescanState m_forceRescanState;

    void execute(CollectionAgent agent, Map parms, ForceRescanState forceRescanState) {
        m_agent = agent;
        m_parameters = parms;
        m_forceRescanState = forceRescanState;
        
        /*
    	 * Write relevant collected SNMP statistics to RRD database First the
    	 * node level RRD info will be updated. Secondly the interface level RRD
    	 * info will be updated.
    	 */
    
        // Node data
        storeNodeData();
    
        saveInterfaceData();
    
    }

    private void saveInterfaceData() {
        if (getIfCollector() != null) {
            
    		logIfAliasConfig();
    
    		/*
    		 * Retrieve list of SNMP collector entries generated for the remote
    		 * node's interfaces.
    		 */
            if (!getIfCollector().hasData()) {
    			log().warn("updateRRDs: No data retrieved for the agent at " + m_agent.getHostAddress());
    		}
            
            
    		// Iterate over the SNMP collector entries
    		Iterator iter = getIfCollector().getEntries().iterator();
    		while (iter.hasNext()) {
    			SNMPCollectorEntry ifEntry = (SNMPCollectorEntry) iter.next();

                int ifIndex = ifEntry.getIfIndex().intValue();
                /*
                 * Use ifIndex to lookup the IfInfo object from the interface
                 * map.
                 */
                IfInfo ifInfo = m_agent.getIfInfo(ifIndex);
                if (ifInfo == null) {
                    m_forceRescanState.rescanIndicated();
                    continue;
                }
    
                // TODO: This will send rescans for entries that have no IfInfo... so moving below IfInfo may fail to catch all the cases
                // However... since we send an event if ifNumber changed maybe we don't need these?
                if (currentAliasIsOutOfDate(ifEntry)) {
                    m_forceRescanState.rescanIndicated();
                    logForceRescan(ifEntry);
                }
    
                validateAttrList(ifInfo);

                if (notScheduledForCollection(ifEntry, ifInfo) && !forceStoreByAlias(getAliasDirName(ifEntry))) {
                    logSkip(ifEntry, ifInfo);
                    continue;
                }

    			if (notScheduledForCollection(ifEntry, ifInfo) && forceStoreByAlias(getAliasDirName(ifEntry))) {
                	logStore(ifEntry, ifInfo);
                }
                
    			/*
    			 * Iterate over the interface datasource list and issue RRD
    			 * update commands to update each datasource which has a
    			 * corresponding value in the collected SNMP data.
    			 */
    			Iterator i = ifInfo.getAttributeList().iterator();
    			while (i.hasNext()) {
                    CollectionAttribute attr = (CollectionAttribute)i.next();
    				DataSource ds = attr.getDs();
    
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
    							storeByAlias(ifEntry, getAliasDirName(ifEntry), ds, dsVal);
    						}
    
    					}
    				} catch (IllegalArgumentException e) {
    					logUpdateFailed(ifEntry, ds, e);
    				}
    
    			} // end while(more datasources)
    		} // end while(more SNMP collector entries)
    	} // end if(ifCollector != null)
    }

    private void logUpdateFailed(SNMPCollectorEntry ifEntry, DataSource ds, IllegalArgumentException e) {
        log().warn("buildRRDUpdateCmd: " + e.getMessage());
   
        log().warn(
        		"updateRRDs: call to buildRRDUpdateCmd() "
        				+ "failed for node/ifindex: " + m_agent.getNodeId()
        				+ "/" + ifEntry.getIfIndex().intValue() + " datasource: "
        				+ ds.getName());
    }

    private void storeByNode(SNMPCollectorEntry ifEntry, IfInfo ifInfo, DataSource ds, String dsVal) {
        String ifRepository = getRrdPath() + File.separator
        + String.valueOf(m_agent.getNodeId()) + File.separator
        + ifInfo.getLabel();
        if (ds.performUpdate(m_agent.getCollection(), m_agent.getInetAddress().getHostAddress(), ifRepository, ds.getName(), dsVal)) {
        	logUpdateFailed(ifEntry, ds);
        }
    }

    private void storeByAlias(SNMPCollectorEntry ifEntry, String aliasVal, DataSource ds, String dsVal) {
        if (aliasVal != null) {
        	String ifAliasRepository = getRrdPath() + File.separator
        			+ getDomain() + File.separator
        			+ aliasVal;
        	if (ds.performUpdate(m_agent.getCollection(), m_agent.getInetAddress()
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
        log().warn("updateRRDs: ds.performUpdate() failed for "
                + "node/ifindex/domain/alias: "
                + m_agent.getNodeId()
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
        log().warn("updateRRDs: ds.performUpdate() failed for "
                + "node/ifindex: "
                + m_agent.getNodeId() + "/"
                + ifEntry.getIfIndex().intValue()
                + " datasource: "
                + ds.getName());
    }

    private void logNoDataRetrieved(SNMPCollectorEntry ifEntry, DataSource ds) {
        // Do nothing, no update is necessary
        if (log().isDebugEnabled()) {
        	log().debug("updateRRDs: Skipping update, "
        					+ "no data retrieved for "
        					+ "node/ifindex: " + m_agent.getNodeId()
        					+ "/" + ifEntry.getIfIndex().intValue()
        					+ " datasource: "
        					+ ds.getName());
        }
    }

    private void validateAttrList(IfInfo ifInfo) {
        if (ifInfo.getAttributeList() == null) {
            throw new RuntimeException("Data Source list not "
                    + "available for primary IP addr "
                    + m_agent.getInetAddress().getHostAddress() + " and ifIndex "
                    + ifInfo.getIndex());
        }
    }

    private boolean forceStoreByAlias(String aliasVal) {
        return !getStorFlagOverride().equals("false") && ((aliasVal != null));
    }

    private boolean notScheduledForCollection(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        return (storingOnlyPrimary() && ifEntryIsNotPrimary(ifEntry)) || (storingOnlySelect() && ifaceIsNotSelect(ifInfo));
    }

    private boolean ifaceIsNotSelect(IfInfo ifInfo) {
        return ifInfo.getCollType().equals(CollectionType.NO_COLLECT);
    }

    private boolean storingOnlySelect() {
        return m_agent.getSnmpStorage().equals(SnmpCollector.SNMP_STORAGE_SELECT);
    }

    private boolean ifEntryIsNotPrimary(SNMPCollectorEntry ifEntry) {
        return ifEntry.getIfIndex().intValue() != m_agent.getIfIndex();
    }

    private boolean storingOnlyPrimary() {
        return m_agent.getSnmpStorage().equals(SnmpCollector.SNMP_STORAGE_PRIMARY);
    }

    private void logSkip(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        if (log().isDebugEnabled()) {
        	log()
        			.debug(
        					"updateRRDs: selectively storing "
        							+ "SNMP data for primary interface ("
        							+ m_agent.getIfIndex()
        							+ "), skipping ifIndex: "
        							+ Integer.toString(ifEntry.getIfIndex().intValue())
        							+ " because collType = "
        							+ ifInfo.getCollType());
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

    private boolean currentAliasIsOutOfDate(SNMPCollectorEntry ifEntry) {
        return getNewIfAlias(ifEntry) != null && !getNewIfAlias(ifEntry).equals(getCurrentIfAlias(ifEntry));
    }

    private String getNewIfAlias(SNMPCollectorEntry ifEntry) {
        return ifEntry.getValueForBase(SnmpCollector.IFALIAS_OID);
    }

    private String getCurrentIfAlias(SNMPCollectorEntry ifEntry) {
         JdbcTemplate template = new JdbcTemplate(DataSourceFactory.getInstance());
        return (String)template.queryForObject(SnmpCollector.SQL_GET_SNMPIFALIASES, new Object[] { m_agent.getNode().getId(), ifEntry.getIfIndex() }, String.class);
    }

    private String getAliasDirName(SNMPCollectorEntry ifEntry) {
        String aliasVal = getNewIfAlias(ifEntry);
        if (aliasVal != null) {
        	if (getIfAliasComment() != null) {
        		int si = aliasVal.indexOf(getIfAliasComment());
        		if (si > -1) {
        			aliasVal = aliasVal.substring(0, si).trim();
        		}
        	}
        	if (aliasVal != null) {
        		aliasVal = AlphaNumeric.parseAndReplaceExcept(aliasVal,
        				SnmpCollector.nonAnRepl, SnmpCollector.AnReplEx);
        	}
        }
        
        logAlias(getNewIfAlias(ifEntry));

        return aliasVal;
    }

    private void logAlias(String alias) {
        if (log().isDebugEnabled()) {
            log().debug("getRRDIfAlias: ifAlias = " + alias);
        }
    }

    private void logForceRescan(SNMPCollectorEntry ifEntry) {
        if (log().isDebugEnabled()) {
        	log().debug("Forcing rescan.  IfAlias " + getNewIfAlias(ifEntry)
        					+ " for index " + Integer.toString(ifEntry.getIfIndex().intValue())
        					+ " does not match DB value: "
                            + getCurrentIfAlias(ifEntry));
        }
    }

    private void storeNodeData() {
        if (getNodeCollector() != null) {
        	log().debug("updateRRDs: processing node-level collection...");
        
            /*
        	 * Build path to node RRD repository. createRRD() will make the
        	 * appropriate directories if they do not already exist.
        	 */
        	String nodeRepository = getRrdPath() + File.separator + String.valueOf(m_agent.getNodeId());
        
        	SNMPCollectorEntry nodeEntry = getNodeCollector().getEntry();
        
        	/*
        	 * Iterate over the node datasource list and issue RRD update
        	 * commands to update each datasource which has a corresponding
        	 * value in the collected SNMP data.
        	 */
        	Iterator it = m_agent.getNodeAttributeList().iterator();
        	while (it.hasNext()) {
                CollectionAttribute attr = (CollectionAttribute)it.next();
        		DataSource ds = attr.getDs();
        
        		try {
        			String dsVal = ds.getRRDValue(nodeEntry);
        			if (dsVal == null) {
        				// Do nothing, no update is necessary
        				logNoDataForValue(ds);
        			} else {
        				if (ds.performUpdate(m_agent.getCollection(), m_agent.getInetAddress().getHostAddress(), nodeRepository, ds.getName(), dsVal)) {
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
        				+ "for node: " + m_agent.getNodeId() + " datasource: "
        				+ ds1.getName());
    }

    private void logUpdateFailed(DataSource ds1) {
        log().warn(
        		"updateRRDs: ds.performUpdate() "
        				+ "failed for node: " + m_agent.getNodeId()
        				+ " datasource: " + ds1.getName());
    }

    private void logNoDataForValue(DataSource ds1) {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"updateRRDs: Skipping update, no "
        					+ "data retrieved for nodeId: "
        					+ m_agent.getNodeId() + " datasource: "
        					+ ds1.getName());
        }
    }

    Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    private String getRrdPath() {
        return DataCollectionConfigFactory.getInstance().getRrdPath();
    }

    private SnmpNodeCollector getNodeCollector() {
        return m_agent.getNodeCollector();
    }

    private SnmpIfCollector getIfCollector() {
        return m_agent.getIfCollector();
    }


}
