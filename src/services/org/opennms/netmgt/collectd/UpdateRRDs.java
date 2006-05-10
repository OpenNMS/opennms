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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.utils.ParameterMap;

public class UpdateRRDs {

    CollectionAgent m_agent;

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

            Collection scheduledResources = skipUnscheduledEntries(m_agent.getIfInfos());
            saveResourcData(scheduledResources);
        } // end if(ifCollector != null)
    }
    
    private Collection skipUnscheduledEntries(Collection resourceList) {
        
        List scheduledResources = new LinkedList();
        for (Iterator iter = resourceList.iterator(); iter.hasNext();) {
            IfInfo ifInfo = (IfInfo) iter.next();
            AliasedResource aliasedResource = new AliasedResource(getDomain(), ifInfo, getIfAliasComment());

            SNMPCollectorEntry ifEntry = ifInfo.getEntry();

            aliasedResource.checkForAliasChanged(m_forceRescanState);

            if ((!ifInfo.isScheduledForCollection()) && !forceStoreByAlias(aliasedResource.getAliasDir())) {
                logSkip(ifEntry, ifInfo);
            } else {
                if ((!ifInfo.isScheduledForCollection()) && forceStoreByAlias(aliasedResource.getAliasDir())) {
                    logStore(ifEntry, ifInfo);
                }

                if (shouldStore(ifInfo))
                    scheduledResources.add(ifInfo);
                
                
                if (shouldStoreByAlias(aliasedResource))
                    scheduledResources.add(aliasedResource);
            }
        } 
        return scheduledResources;
    }

    private void saveResourcData(Collection resourceList) {
        for (Iterator iter = resourceList.iterator(); iter.hasNext();) {
            CollectionResource resource = (CollectionResource)iter.next();
            resource.storeAttributes(getRrdBaseDir());
        } 

    }

    private boolean shouldStore(IfInfo ifInfo) {
        if (getStoreByNodeID().equals("normal"))
            return ifInfo.isScheduledForCollection();
        else
            return getStoreByNodeID().equals("true");
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

    private boolean shouldStoreByAlias(AliasedResource aliasedResource) {
        return aliasesEnabled() && aliasedResource.getAliasDir() != null;
    }

    private boolean aliasesEnabled() {
        return getStoreByIfAlias().equals("true");
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
