
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

import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCollectionAgent extends IPv4NetworkInterface implements CollectionAgent {

    /**
     * 
     */
    private static final long serialVersionUID = 6694654071513990997L;

    /**
     * <p>create</p>
     *
     * @param ifaceId a {@link java.lang.Integer} object.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.IpInterfaceDao} object.
     * @param transMgr a {@link org.springframework.transaction.PlatformTransactionManager} object.
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     */
    public static CollectionAgent create(Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
        return new DefaultCollectionAgent(DefaultCollectionAgentService.create(ifaceId, ifaceDao, transMgr));
    }

    // miscellaneous junk?
    private int m_ifCount = -1;
    private long m_sysUpTime = -1;

        // cached attributes
    private int m_nodeId = -1;
    private InetAddress m_inetAddress = null;
    private int m_ifIndex = -1;
    private PrimaryType m_isSnmpPrimary = null;
    private String m_sysObjId = null;
    
    private CollectionAgentService m_agentService;
    private Set<SnmpIfData> m_snmpIfData;

    private DefaultCollectionAgent(CollectionAgentService agentService) {
        super(null);
        m_agentService = agentService;
        
        if (Boolean.getBoolean("org.opennms.netmgt.collectd.DefaultCollectionAgent.loadSnmpDataOnInit")) {
            getSnmpInterfaceData();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getAddress() {
        return getInetAddress();
    }

    /** {@inheritDoc} */
    @Override
    public InetAddress getInetAddress() {
        if (m_inetAddress == null) {
            m_inetAddress = m_agentService.getInetAddress();
        }
        return m_inetAddress;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getHostAddress()
     */
    /**
     * <p>getHostAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getHostAddress() {
        return getInetAddress().getHostAddress();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#setSavedIfCount(int)
     */
    /** {@inheritDoc} */
    public void setSavedIfCount(int ifCount) {
        m_ifCount = ifCount;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getSavedIfCount()
     */
    /**
     * <p>getSavedIfCount</p>
     *
     * @return a int.
     */
    public int getSavedIfCount() {
        return m_ifCount;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getNodeId()
     */
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        if (m_nodeId == -1) {
            m_nodeId = m_agentService.getNodeId();
        }
        return m_nodeId; 
    }

    private int getIfIndex() {
        if (m_ifIndex == -1) {
            m_ifIndex = m_agentService.getIfIndex();
        }
        return m_ifIndex;
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getSysObjectId()
     */
    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysObjectId() {
        if (m_sysObjId == null) {
            m_sysObjId = m_agentService.getSysObjectId();
        }
        return m_sysObjId;
    }

    private PrimaryType getIsSnmpPrimary() {
        if (m_isSnmpPrimary == null) {
            m_isSnmpPrimary = m_agentService.getIsSnmpPrimary();
        }
        return m_isSnmpPrimary;
        
    }

    private void logCompletion() {

        if (log().isDebugEnabled()) {
            log().debug(
                        "initialize: initialization completed: nodeid = " + getNodeId()
                        + ", address = " + getHostAddress()
                        + ", primaryIfIndex = " + getIfIndex()
                        + ", isSnmpPrimary = " + getIsSnmpPrimary()
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
                        + ", isSnmpPrimary = " + getIsSnmpPrimary()
                        + ", sysoid = " + getSysObjectId()
            );
        }
    }

    private void validateIsSnmpPrimary() {
        if (!PrimaryType.PRIMARY.equals(getIsSnmpPrimary())) {
            throw new RuntimeException("Interface "
                                       + getHostAddress()
                                       + " is not the primary SNMP interface for nodeid "
                                       + getNodeId());
        }
    }

    private void validatePrimaryIfIndex() {
        if (getIfIndex() < 0) {
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#validateAgent()
     */
    /**
     * <p>validateAgent</p>
     */
    public void validateAgent() {
        logCollectionParms();
        validateIsSnmpPrimary();
        validatePrimaryIfIndex();
        validateSysObjId();
        logCompletion();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#toString()
     */
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "Agent[nodeid = "+getNodeId()+" ipaddr= "+getHostAddress()+']';
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getAgentConfig()
     */
    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpAgentConfig getAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(getInetAddress());
    }
    
    private Set<SnmpIfData> getSnmpInterfaceData() {
        if (m_snmpIfData == null) {
            m_snmpIfData = m_agentService.getSnmpInterfaceData();
        }
        return m_snmpIfData;
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getSnmpInterfaceInfo(org.opennms.netmgt.collectd.IfResourceType)
     */
    /** {@inheritDoc} */
    public Set<IfInfo> getSnmpInterfaceInfo(IfResourceType type) {
        Set<SnmpIfData> snmpIfData = getSnmpInterfaceData();
        
        Set<IfInfo> ifInfos = new LinkedHashSet<IfInfo>(snmpIfData.size());
        
        for (SnmpIfData ifData : snmpIfData) {
            IfInfo ifInfo = new IfInfo(type, this, ifData);
            ifInfos.add(ifInfo);
        }
        
        return ifInfos;
    }

    /** {@inheritDoc} */
    public String getSnmpInterfaceLabel(int ifIndex) {
        for (SnmpIfData ifData : getSnmpInterfaceData()) {
            if (ifData.getIfIndex() == ifIndex)
                return ifData.getLabelForRRD();
        }
        return null;
    }

    /**
     * <p>getSavedSysUpTime</p>
     *
     * @return a long.
     */
    public long getSavedSysUpTime() {
        return m_sysUpTime;
    }

    /** {@inheritDoc} */
    public void setSavedSysUpTime(long sysUpTime) {
        m_sysUpTime = sysUpTime;
    }
    

}
