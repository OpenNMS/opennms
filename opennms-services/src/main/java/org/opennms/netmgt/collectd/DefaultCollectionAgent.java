
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.poller.support.InetNetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCollectionAgent extends InetNetworkInterface implements SnmpCollectionAgent {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCollectionAgent.class);

    /**
     * 
     */
    private static final long serialVersionUID = 6694654071513990997L;

    /**
     * <p>create</p>
     *
     * @param ifaceId a {@link java.lang.Integer} object.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     * @param transMgr a {@link org.springframework.transaction.PlatformTransactionManager} object.
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public static SnmpCollectionAgent create(final Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
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
    private String m_locationName = null;
    private String m_nodeLabel = null;
    private ResourcePath m_storageResourcePath = null;
    
    private CollectionAgentService m_agentService;
    private Set<SnmpIfData> m_snmpIfData;

    private DefaultCollectionAgent(final CollectionAgentService agentService) {
        super(null);
        m_agentService = agentService;
        m_storageResourcePath = agentService.getStorageResourcePath();
        
        if (Boolean.getBoolean("org.opennms.netmgt.collectd.DefaultCollectionAgent.loadSnmpDataOnInit")) {
            getSnmpInterfaceData();
        }
    }

    /** {@inheritDoc} */
    @Override
    public InetAddress getAddress() {
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
    @Override
    public Boolean isStoreByForeignSource() {
        return ResourceTypeUtils.isStoreByForeignSource();
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
        return InetAddressUtils.str(getAddress());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#setSavedIfCount(int)
     */
    /** {@inheritDoc} */
    @Override
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
    @Override
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
    @Override
    public int getNodeId() {
        if (m_nodeId == -1) {
            m_nodeId = m_agentService.getNodeId();
        }
        return m_nodeId; 
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#getNodeLabel()
     */
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getNodeLabel() {
        if (m_nodeLabel == null) {
            m_nodeLabel = m_agentService.getNodeLabel();
        }
        return m_nodeLabel;
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
    @Override
    public String getForeignId() {
        if (m_foreignId == null) {
            m_foreignId = m_agentService.getForeignId();
        }
        return m_foreignId;
    }

    @Override
    public String getLocationName() {
        if (m_locationName == null) {
            m_locationName = m_agentService.getLocationName();
        }
        return m_locationName;
    }

    @Override
    public ResourcePath getStorageResourcePath() {
        return m_storageResourcePath;
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
    @Override
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
        LOG.debug("initialize: initialization completed: nodeid = {}, address = {}, primaryIfIndex = {}, isSnmpPrimary = {}, sysoid = {}", getNodeId(), getHostAddress(), getIfIndex(), getIsSnmpPrimary(), getSysObjectId());
    }

    private void validateSysObjId() throws CollectionInitializationException {
        if (getSysObjectId() == null) {
            throw new CollectionInitializationException("System Object ID for interface "
                                       + getHostAddress()
                                       + " does not exist in the database.");
        }
    }

    private void logCollectionParms() {
        LOG.debug("initialize: db retrieval info: nodeid = {}, address = {}, primaryIfIndex = {}, isSnmpPrimary = {}, sysoid = {}", getNodeId(), getHostAddress(), getIfIndex(), getIsSnmpPrimary(), getSysObjectId());
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
            LOG.debug("initialize: db retrieval info: node {} does not have a legitimate primaryIfIndex.  Assume node does not supply ipAddrTable and continue...", getNodeId());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.CollectionAgent#validateAgent()
     */
    /**
     * <p>validateAgent</p>
     * @throws CollectionInitializationException 
     */
    @Override
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
    @Override
    public String toString() {
        return "Agent[nodeid = "+getNodeId()+" ipaddr= "+getHostAddress()+']';
    }

    @Override
    public SnmpAgentConfig getAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(getAddress(), getLocationName());
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
    @Override
    public Set<IfInfo> getSnmpInterfaceInfo(final IfResourceType type) {
        final Set<SnmpIfData> snmpIfData = getSnmpInterfaceData();
        final Set<IfInfo> ifInfos = new LinkedHashSet<IfInfo>(snmpIfData.size());
        
        for (final SnmpIfData ifData : snmpIfData) {
            ifInfos.add(new IfInfo(type, this, ifData));
        }
        
        return ifInfos;
    }

    /** {@inheritDoc} */
    @Override
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
    @Override
    public long getSavedSysUpTime() {
        return m_sysUpTime;
    }

    /** {@inheritDoc} */
    @Override
    public void setSavedSysUpTime(final long sysUpTime) {
        m_sysUpTime = sysUpTime;
    }

}
