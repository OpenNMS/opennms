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
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.utils.ParameterMap;

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
        m_agent.storeNodeData(getRrdBaseDir());
    
        saveInterfaceData();
    
    }

    private void saveInterfaceData() {
        if (m_agent.getIfCollector() != null) {
            
    		logIfAliasConfig();
    
    		/*
    		 * Retrieve list of SNMP collector entries generated for the remote
    		 * node's interfaces.
    		 */
            if (!m_agent.getIfCollector().hasData()) {
    			log().warn("updateRRDs: No data retrieved for the agent at " + m_agent.getHostAddress());
    		}
            
            putEntriesInIfInfos();
            
            for (Iterator iter = m_agent.getIfInfos().iterator(); iter.hasNext();) {
                IfInfo ifInfo = (IfInfo) iter.next();
                
    			SNMPCollectorEntry ifEntry = ifInfo.getEntry();

                ifInfo.checkForChangedIfAlias(m_forceRescanState);
    
                if ((!ifInfo.isScheduledForCollection()) && !forceStoreByAlias(ifInfo.getAliasDir(getIfAliasComment()))) {
                    logSkip(ifEntry, ifInfo);
                    continue;
                }

    			if ((!ifInfo.isScheduledForCollection()) && forceStoreByAlias(ifInfo.getAliasDir(getIfAliasComment()))) {
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
    				try {
                    	// Build RRD update command
                    	if (attr.getDs().getRRDValue(ifEntry) == null) {
                    		logNoDataRetrieved(ifEntry, attr.getDs());
                    	} else {
                    		if (shouldStoreByNode(ifEntry, ifInfo)) {
                                storeByNode(ifEntry, ifInfo, attr.getDs(), attr.getDs().getRRDValue(ifEntry));
                    		}
                    		if (shouldStoreByAlias(ifEntry, ifInfo)) {
                    			storeByAlias(ifEntry, ifInfo.getAliasDir(getIfAliasComment()), attr.getDs(), attr.getDs().getRRDValue(ifEntry));
                    		}
                    
                    	}
                    } catch (IllegalArgumentException e) {
                    	logUpdateFailed(ifEntry, attr.getDs(), e);
                    }
    
    			} // end while(more datasources)
    		} // end while(more SNMP collector entries)
    	} // end if(ifCollector != null)
    }

    private void putEntriesInIfInfos() {
        // Iterate over the SNMP collector entries
        Iterator iter = m_agent.getIfCollector().getEntries().iterator();
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
            } else {
                ifInfo.setEntry(ifEntry);
            }

        }
     
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
        if (ds.performUpdate(m_agent.getCollection(), m_agent.getInetAddress().getHostAddress(), getInterfaceRepo(ifInfo), ds.getName(), dsVal)) {
        	logUpdateFailed(ifEntry, ds);
        }
    }

    private File getInterfaceRepo(IfInfo ifInfo) {
        File rrdBaseDir = getRrdBaseDir();
        File nodeDir = new File(rrdBaseDir, String.valueOf(ifInfo.getCollectionAgent().getNodeId()));
        File ifDir = new File(nodeDir, ifInfo.getLabel());
        return ifDir;
    }

    private void storeByAlias(SNMPCollectorEntry ifEntry, String aliasVal, DataSource ds, String dsVal) {
        if (ds.performUpdate(m_agent.getCollection(), m_agent.getInetAddress().getHostAddress(), getAliasRepo(aliasVal), ds.getName(), dsVal)) {
            logIfAliasUpdateFailed(ifEntry, aliasVal, ds);
        }
    }

    private File getAliasRepo(String aliasVal) {
        File rrdBaseDir = getRrdBaseDir();
        File domainDir = new File(rrdBaseDir, getDomain());
        File aliasDir = new File(domainDir, aliasVal);
        return aliasDir;
    }

    private boolean shouldStoreByAlias(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        return aliasesEnabled() && ifInfo.getAliasDir(getIfAliasComment()) != null;
    }

    private boolean aliasesEnabled() {
        return getStoreByIfAlias().equals("true");
    }

    private boolean shouldStoreByNode(SNMPCollectorEntry ifEntry, IfInfo ifInfo) {
        String byNode = getStoreByNodeID();
        if (!ifInfo.isScheduledForCollection()) {
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

    private boolean forceStoreByAlias(String aliasVal) {
        return !getStorFlagOverride().equals("false") && ((aliasVal != null));
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

    File getRrdBaseDir() {
        String rrdPath = DataCollectionConfigFactory.getInstance().getRrdPath();
        File rrdBaseDir = new File(rrdPath);
        return rrdBaseDir;
    }

    private void logIfAliasConfig() {
        if (aliasesEnabled()) {
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

    Category log() {
        return ThreadCategory.getInstance(getClass());
    }


}
