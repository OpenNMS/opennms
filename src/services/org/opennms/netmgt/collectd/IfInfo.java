//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.collectd;


import java.io.File;

import org.apache.log4j.Category;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;


/**
 * This class encapsulates all the information required by the SNMP collector in
 * order to perform data collection for an individual interface and store that
 * data in an appropriately named RRD file.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
final class IfInfo extends DbCollectionResource {
	
	OnmsSnmpInterface m_snmpIface;
    private SNMPCollectorEntry m_entry;
	
	public IfInfo(CollectionAgent agent, String collectionName, OnmsSnmpInterface snmpIface) {
        super(agent, collectionName);
    	m_snmpIface = snmpIface;
    	
    }

	public int getIndex() {
        return m_snmpIface.getIfIndex().intValue();
    }

    public int getType() {
        return m_snmpIface.getIfType().intValue();
    }

    public String getLabel() {
        return m_snmpIface.computeLabelForRRD();
    }

    public CollectionType getCollType() {
        return m_snmpIface.getCollectionType();
    }

    public void setEntry(SNMPCollectorEntry ifEntry) {
        m_entry = ifEntry;
    }
    
    protected SNMPCollectorEntry getEntry() {
        return m_entry;
    }

    String getNewIfAlias() {
        return getEntry().getValueForBase(SnmpCollector.IFALIAS_OID);
    }

    String getCurrentIfAlias() {
        return m_snmpIface.getIfAlias();
    }

    boolean currentAliasIsOutOfDate() {
        return getNewIfAlias() != null && !getNewIfAlias().equals(getCurrentIfAlias());
    }

    void logAlias() {
        Category log = log();
        if (log.isDebugEnabled()) {
            log.debug("getRRDIfAlias: ifAlias = " + getNewIfAlias());
        }
    }

    String getAliasDir(String ifAliasComment) {
        String aliasVal = getNewIfAlias();
        if (aliasVal != null) {
            if (ifAliasComment != null) {
        		int si = aliasVal.indexOf(ifAliasComment);
        		if (si > -1) {
        			aliasVal = aliasVal.substring(0, si).trim();
        		}
        	}
        	if (aliasVal != null) {
        		aliasVal = AlphaNumeric.parseAndReplaceExcept(aliasVal,
        				SnmpCollector.nonAnRepl, SnmpCollector.AnReplEx);
        	}
        }
        
        logAlias();
    
        return aliasVal;
    }

    void logForceRescan() {
        
        if (log().isDebugEnabled()) {
        	log().debug("Forcing rescan.  IfAlias " + getNewIfAlias()
        					+ " for index " + getIndex()
        					+ " does not match DB value: "
                            + getCurrentIfAlias());
        }
    }

    void checkForChangedIfAlias(ForceRescanState forceRescanState) {
        if (currentAliasIsOutOfDate()) {
            forceRescanState.rescanIndicated();
            logForceRescan();
        }
    }

    boolean isScheduledForCollection() {
        return getCollType().compareTo(getCollectionAgent().getMinimumCollectionType()) >= 0;
    }

    protected File getResourceDir(File rrdBaseDir) {
        File nodeDir = new File(rrdBaseDir, String.valueOf(getCollectionAgent().getNodeId()));
        File ifDir = new File(nodeDir, getLabel());
        return ifDir;
    }

    public String toString() {
        return getCollectionAgent().getNodeId() + "/" + getIndex();
    }

    boolean shouldStore(ServiceParameters serviceParameters) {
        if (serviceParameters.getStoreByNodeID().equals("normal"))
            return isScheduledForCollection();
        else
            return serviceParameters.getStoreByNodeID().equals("true");
    }

    public boolean shouldPersist(ServiceParameters serviceParameters) {
        return shouldStore(serviceParameters) && (isScheduledForCollection() || serviceParameters.forceStoreByAlias(getNewIfAlias()));
    }

} // end class
