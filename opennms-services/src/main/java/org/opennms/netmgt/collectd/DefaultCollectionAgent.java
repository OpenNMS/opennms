
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
import java.util.Properties;
import java.util.Set;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 */
public class DefaultCollectionAgent extends IPv4NetworkInterface implements CollectionAgent {

    /**
     * 
     */
    private static final long serialVersionUID = 6694654071513990997L;

    public static CollectionAgent create(Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
        CollectionAgent agent = new DefaultCollectionAgent(ifaceId, ifaceDao);
        
        TransactionProxyFactoryBean bean = new TransactionProxyFactoryBean();
        bean.setTransactionManager(transMgr);
        bean.setTarget(agent);
        
        Properties props = new Properties();
        props.put("*", "PROPAGATION_REQUIRED,readOnly");
        
        bean.setTransactionAttributes(props);
        
        bean.afterPropertiesSet();
        
        return (CollectionAgent) bean.getObject();
    }

    // the interface of the Agent
    private Integer m_ifaceId;

    // miscellaneous junk?
    private int m_maxVarsPerPdu = 0;
    private int m_ifCount = -1;

    private IpInterfaceDao m_ifaceDao;
    
    // cached attributes
    private int m_nodeId = -1;
    private InetAddress m_inetAddress = null;
    private int m_ifIndex = -1;
    private CollectionType m_collType = null;
    private String m_sysObjId = null;

    private DefaultCollectionAgent(Integer ifaceId, IpInterfaceDao ifaceDao) {
        // we pass in null since we override calls to getAddress and getInetAddress
        super(null);
        m_ifaceId = ifaceId;
        m_ifaceDao = ifaceDao;
    }

    private OnmsIpInterface getIpInterface() {
        return m_ifaceDao.get(m_ifaceId);
    }

    private OnmsNode getNode() {
        return getIpInterface().getNode();
    }

    @Override
    public Object getAddress() {
        return getInetAddress();
    }

    @Override
    public InetAddress getInetAddress() {
        if (m_inetAddress == null) {
            m_inetAddress = getIpInterface().getInetAddress();
        }
        return m_inetAddress;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#setMaxVarsPerPdu(int)
     */
    public void setMaxVarsPerPdu(int maxVarsPerPdu) {
        m_maxVarsPerPdu = maxVarsPerPdu;
        if (log().isDebugEnabled()) {
            log().debug("maxVarsPerPdu=" + maxVarsPerPdu);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getMaxVarsPerPdu()
     */
    public int getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getHostAddress()
     */
    public String getHostAddress() {
        return getInetAddress().getHostAddress();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#setSavedIfCount(int)
     */
    public void setSavedIfCount(int ifCount) {
        m_ifCount = ifCount;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getSavedIfCount()
     */
    public int getSavedIfCount() {
        return m_ifCount;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getNodeId()
     */
    public int getNodeId() {
        if (m_nodeId == -1) {
            m_nodeId = getIpInterface().getNode().getId() == null ? -1 : getIpInterface().getNode().getId().intValue();
        }
        return m_nodeId; 
    }

    private int getIfIndex() {
        if (m_ifIndex == -1) {
            m_ifIndex = (getIpInterface().getIfIndex() == null ? -1 : getIpInterface().getIfIndex().intValue());
        }
        return m_ifIndex;
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getSysObjectId()
     */
    public String getSysObjectId() {
        if (m_sysObjId == null) {
            m_sysObjId = getIpInterface().getNode().getSysObjectId();
        }
        return m_sysObjId;
    }

    private CollectionType getCollectionType() {
        if (m_collType == null) {
            m_collType = getIpInterface().getIsSnmpPrimary();
        }
        return m_collType;
        
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#validateAgent()
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
    public String toString() {
        return "Agent[nodeid = "+getNodeId()+" ipaddr= "+getHostAddress()+']';
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getAgentConfig()
     */
    public SnmpAgentConfig getAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(getInetAddress());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getSnmpInterfaceInfo(org.opennms.netmgt.collectd.IfResourceType)
     */
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
