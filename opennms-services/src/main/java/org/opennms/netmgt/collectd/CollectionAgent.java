
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Modifications:

//2006 Aug 15: Javadoc. - dj@opennms.org

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/


package org.opennms.netmgt.collectd;

import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 */
public class CollectionAgent extends IPv4NetworkInterface {

    /**
     * 
     */
    private static final long serialVersionUID = 6694654071513990997L;

    public static CollectionAgent create(final OnmsIpInterface iface, final TransactionTemplate transTemplate) {
        return new CollectionAgent(iface, transTemplate);
    }

    // the interface of the Agent
    private OnmsIpInterface m_iface;

    // miscellaneous junk?
    private int m_maxVarsPerPdu = 0;
    private int m_ifCount = -1;

    private TransactionTemplate m_transTemplate;

    private CollectionAgent(OnmsIpInterface iface, TransactionTemplate transTemplate) {
        super(iface.getInetAddress());
        m_iface = iface;
        m_transTemplate = transTemplate;
    }

    private OnmsIpInterface getIpInterface() {
        return m_iface;
    }

    private OnmsNode getNode() {
        return m_iface.getNode();
    }

    public void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
        if (log().isDebugEnabled()) {
            log().debug("maxVarsPerPdu=" + maxVarsPerPdu);
        }
    }

    public int getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    public String getHostAddress() {
        return getInetAddress().getHostAddress();
    }

    public void setSavedIfCount(int ifCount) {
        m_ifCount = ifCount;
    }

    public int getSavedIfCount() {
        return m_ifCount;
    }

    public int getNodeId() {
        return getIpInterface().getNode().getId() == null ? -1 : getIpInterface().getNode().getId().intValue();
    }

    private int getIfIndex() {
        return (getIpInterface().getIfIndex() == null ? -1 : getIpInterface().getIfIndex().intValue());
    }

    public String getSysObjectId() {
        return getIpInterface().getNode().getSysObjectId();
    }

    private CollectionType getCollectionType() {
        return getIpInterface().getIsSnmpPrimary();
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

    public void validateAgent() {
        logCollectionParms();
        validateIsSnmpPrimary();
        validatePrimaryIfIndex();
        validateSysObjId();
        logCompletion();
    }

    public String toString() {
        return "Agent[nodeid = "+getNodeId()+" ipaddr= "+getHostAddress()+']';
    }

    public SnmpAgentConfig getAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(getInetAddress());
    }

    public Set<IfInfo> getSnmpInterfaceInfo(IfResourceType type) {
        
        OnmsNode node = getNode();
    
    	Set<OnmsSnmpInterface> snmpIfs = node.getSnmpInterfaces();
    	
    	if (snmpIfs.size() == 0) {
            log().debug("no known SNMP interfaces for node " + node);
    	}
    	
        Set<IfInfo> ifInfos = new LinkedHashSet<IfInfo>(snmpIfs.size());
        
        for(OnmsSnmpInterface snmpIface : snmpIfs) {
    		logInitializeSnmpIf(snmpIface);
            ifInfos.add(new IfInfo(type, this, snmpIface));
    	}
        return ifInfos;
    }

    private void logInitializeSnmpIf(OnmsSnmpInterface snmpIface) {
        if (log().isDebugEnabled()) {
        	log().debug(
        			"initialize: snmpifindex = " + snmpIface.getIfIndex().intValue()
        			+ ", snmpifname = " + snmpIface.getIfName()
        			+ ", snmpifdescr = " + snmpIface.getIfDescr()
        			+ ", snmpphysaddr = -"+ snmpIface.getPhysAddr() + "-");
        	log().debug("initialize: ifLabel = '" + snmpIface.computeLabelForRRD() + "'");
        }
    }

}
