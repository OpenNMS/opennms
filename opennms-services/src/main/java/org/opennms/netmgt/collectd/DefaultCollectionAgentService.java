
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.io.File;
import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCollectionAgentService implements CollectionAgentService {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCollectionAgentService.class);
    
    /**
     * <p>create</p>
     *
     * @param ifaceId a {@link java.lang.Integer} object.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
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
    @Override
    public String getHostAddress() {
        return InetAddressUtils.str(getInetAddress());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#isStoreByForeignSource()
     */
    /**
     * <p>isStoreByForeignSource</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    @Override
    public Boolean isStoreByForeignSource() {
        return Boolean.getBoolean("org.opennms.rrd.storeByForeignSource");
    }
    
     /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getNodeId()
     */
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    @Override
    public int getNodeId() {
        return getIpInterface().getNode().getId() == null ? -1 : getIpInterface().getNode().getId().intValue();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getForeignSource()
     */
    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getForeignSource() {
       return getIpInterface().getNode().getForeignSource() == null ? null : getIpInterface().getNode().getForeignSource();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getForeignId()
     */
    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getForeignId() {
       return getIpInterface().getNode().getForeignId() == null ? null : getIpInterface().getNode().getForeignId();
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getStorageDir()
     */
    /**
     * <p>getStorageDir</p>
     *
     * @return a {@link java.io.File} object.
     */
    @Override
    public File getStorageDir() {
        File dir = new File(String.valueOf(getIpInterface().getNode().getId()));
        if(isStoreByForeignSource() && !(getIpInterface().getNode().getForeignSource() == null) && !(getIpInterface().getNode().getForeignId() == null)) {
               File fsDir = new File(DefaultResourceDao.FOREIGN_SOURCE_DIRECTORY, getIpInterface().getNode().getForeignSource());
            dir = new File(fsDir, getIpInterface().getNode().getForeignId());
        }
        return dir;
    }
    
    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    @Override
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
    @Override
    public String getSysObjectId() {
        return getIpInterface().getNode().getSysObjectId();
    }

    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    @Override
    public PrimaryType getIsSnmpPrimary() {
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
    @Override
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
    @Override
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
    @Override
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
            LOG.debug("no known SNMP interfaces for node {}", node);
    	}
        return snmpIfs;
    }

    private void logInitializeSnmpIf(OnmsSnmpInterface snmpIface) {
        LOG.debug("initialize: snmpifindex = {}, snmpifname = {}, snmpifdescr = {}, snmpphysaddr = -{}-", snmpIface.getIfIndex().intValue(), snmpIface.getIfName(), snmpIface.getIfDescr(), snmpIface.getPhysAddr());
        LOG.debug("initialize: ifLabel = '{}'", snmpIface.computeLabelForRRD());
    }

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    @Override
    public InetAddress getInetAddress() {
        return getIpInterface().getIpAddress();
    }

}
