
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCollectionAgent extends InetNetworkInterface implements CollectionAgent {

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
    public static CollectionAgent create(final Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
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
    private String m_foreignSource = null;
    private String m_foreignId = null;
    
    private CollectionAgentService m_agentService;
    private Set<SnmpIfData> m_snmpIfData;

    private DefaultCollectionAgent(final CollectionAgentService agentService) {
        super(null);
        m_agentService = agentService;
        
        if (Boolean.getBoolean("org.opennms.netmgt.collectd.DefaultCollectionAgent.loadSnmpDataOnInit")) {
            getSnmpInterfaceData();
        }
    }

    /** {@inheritDoc} */
    @Override
    public InetAddress getAddress() {
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
     * @see org.opennms.netmgt.collectd.CollectionAgent#isStoreByForeignSource()
     */
    /**
     * <p>isStoreByForeignSource</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean isStoreByForeignSource() {
        return Boolean.getBoolean("org.opennms.rrd.storeByForeignSource");
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
        return InetAddressUtils.str(getInetAddress());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#setSavedIfCount(int)
     */
    /** {@inheritDoc} */
    public void setSavedIfCount(final int ifCount) {
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

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getForeignSource()
     */
    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignSource() {
        if (m_foreignSource == null) {
            m_foreignSource = m_agentService.getForeignSource();
        }
        return m_foreignSource;
    }
 
    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getForeignId()
     */
    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getForeignId() {
        if (m_foreignId == null) {
            m_foreignId = m_agentService.getForeignId();
        }
        return m_foreignId;
    }
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getStorageDir()
     */
    /**
     * <p>getStorageDir</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getStorageDir() {
       File dir = new File(String.valueOf(getNodeId()));
       if(isStoreByForeignSource() && !(getForeignSource() == null) && !(getForeignId() == null)) {
               File fsDir = new File(DefaultResourceDao.FOREIGN_SOURCE_DIRECTORY, m_foreignSource);
               dir = new File(fsDir, m_foreignId);
       }
       LogUtils.debugf(this, " getStorageDir: isStoreByForeignSource = %s, foreignSource = %s, foreignId = %s, dir = %s",
                       isStoreByForeignSource(),
                       m_foreignSource,
                       m_foreignId,
                       dir
       );
       return dir;
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
            // Intern the string value to save RAM
            m_sysObjId = (m_sysObjId == null ? null : m_sysObjId.intern());
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
        LogUtils.debugf(this, "initialize: initialization completed: nodeid = %s, address = %s, primaryIfIndex = %s, isSnmpPrimary = %s, sysoid = %s",
                        getNodeId(),
                        getHostAddress(),
                        getIfIndex(),
                        getIsSnmpPrimary(),
                        getSysObjectId()
        );
    }

    private void validateSysObjId() throws CollectionInitializationException {
        if (getSysObjectId() == null) {
            throw new CollectionInitializationException("System Object ID for interface "
                                       + getHostAddress()
                                       + " does not exist in the database.");
        }
    }

    private void logCollectionParms() {
        LogUtils.debugf(this, "initialize: db retrieval info: nodeid = %s, address = %s, primaryIfIndex = %s, isSnmpPrimary = %s, sysoid = %s",
                        getNodeId(),
                        getHostAddress(),
                        getIfIndex(),
                        getIsSnmpPrimary(),
                        getSysObjectId()
        );
    }

    private void validateIsSnmpPrimary() throws CollectionInitializationException {
        if (!PrimaryType.PRIMARY.equals(getIsSnmpPrimary())) {
            throw new CollectionInitializationException("Interface "
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
            LogUtils.debugf(this, "initialize: db retrieval info: node %s does not have a legitimate primaryIfIndex.  Assume node does not supply ipAddrTable and continue...", getNodeId());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#validateAgent()
     */
    /**
     * <p>validateAgent</p>
     * @throws CollectionInitializationException 
     */
    public void validateAgent() throws CollectionInitializationException {
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
    public Set<IfInfo> getSnmpInterfaceInfo(final IfResourceType type) {
        final Set<SnmpIfData> snmpIfData = getSnmpInterfaceData();
        final Set<IfInfo> ifInfos = new LinkedHashSet<IfInfo>(snmpIfData.size());
        
        for (final SnmpIfData ifData : snmpIfData) {
            ifInfos.add(new IfInfo(type, this, ifData));
        }
        
        return ifInfos;
    }

    /** {@inheritDoc} */
    public String getSnmpInterfaceLabel(final int ifIndex) {
        for (final SnmpIfData ifData : getSnmpInterfaceData()) {
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
    public void setSavedSysUpTime(final long sysUpTime) {
        m_sysUpTime = sysUpTime;
    }
    

}
