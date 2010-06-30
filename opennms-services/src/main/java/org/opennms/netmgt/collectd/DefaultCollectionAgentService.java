
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Modifications:

//2008 Mar 04: Use load() instead of get() to get the OnmsIpInterface from
//             the DAO since it should be there and we should throw a sane
//             error message, not NPEs if it isn't. - dj@opennms.org
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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCollectionAgentService implements CollectionAgentService {

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
     * @return a {@link org.opennms.netmgt.collectd.CollectionAgentService} object.
     */
    public static CollectionAgentService create(Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
        CollectionAgentService agent = new DefaultCollectionAgentService(ifaceId, ifaceDao);
        
        TransactionProxyFactoryBean bean = new TransactionProxyFactoryBean();
        bean.setTransactionManager(transMgr);
        bean.setTarget(agent);
        
        Properties props = new Properties();
        props.put("*", "PROPAGATION_REQUIRED,readOnly");
        
        bean.setTransactionAttributes(props);
        
        bean.afterPropertiesSet();
        
        return (CollectionAgentService) bean.getObject();
    }

    // the interface of the Agent
    private Integer m_ifaceId;
    private IpInterfaceDao m_ifaceDao;
    
    //Unused; delete
    //private int m_maxVarsPerPdu;
    //private int m_ifCount;
    
    private DefaultCollectionAgentService(Integer ifaceId, IpInterfaceDao ifaceDao) {
        // we pass in null since we override calls to getAddress and getInetAddress
        m_ifaceId = ifaceId;
        m_ifaceDao = ifaceDao;
    }
    

    OnmsIpInterface getIpInterface() {
        return m_ifaceDao.load(m_ifaceId);
    }

    private OnmsNode getNode() {
        return getIpInterface().getNode();
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
     * @see org.opennms.netmgt.collectd.CollectionAgent#getNodeId()
     */
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return getIpInterface().getNode().getId() == null ? -1 : getIpInterface().getNode().getId().intValue();
    }

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return (getIpInterface().getIfIndex() == null ? -1 : getIpInterface().getIfIndex().intValue());
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
        return getIpInterface().getNode().getSysObjectId();
    }

    /**
     * <p>getCollectionType</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsIpInterface.CollectionType} object.
     */
    public CollectionType getCollectionType() {
        return getIpInterface().getIsSnmpPrimary();
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getSnmpInterfaceInfo(org.opennms.netmgt.collectd.IfResourceType)
     */
    /**
     * <p>getSnmpInterfaceData</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<SnmpIfData> getSnmpInterfaceData() {
        
        Set<OnmsSnmpInterface> snmpIfs = getSnmpInterfaces();
    	
        Set<SnmpIfData> ifData = new LinkedHashSet<SnmpIfData>(snmpIfs.size());
        
        for(OnmsSnmpInterface snmpIface : snmpIfs) {
    		logInitializeSnmpIf(snmpIface);
    		SnmpIfData snmpIfData = new SnmpIfData(snmpIface);
    		ifData.add(snmpIfData);
            //ifInfos.add(new IfInfo(type, agent, snmpIfData));
    	}
        return ifData;
    }


    private Set<OnmsSnmpInterface> getSnmpInterfaces() {
        OnmsNode node = getNode();
    
    	Set<OnmsSnmpInterface> snmpIfs = node.getSnmpInterfaces();
    	
    	if (snmpIfs.size() == 0) {
            log().debug("no known SNMP interfaces for node " + node);
    	}
        return snmpIfs;
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

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }


    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getInetAddress() {
        return getIpInterface().getInetAddress();
    }

}
