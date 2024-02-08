/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.collectd;

import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.core.DefaultCollectionAgent;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.common.base.Strings;

/**
 * Represents a remote SNMP agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultSnmpCollectionAgent extends DefaultCollectionAgent implements SnmpCollectionAgent {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSnmpCollectionAgent.class);

    /**
     * <p>create</p>
     *
     * @param ifaceId a {@link java.lang.Integer} object.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     * @param transMgr a {@link org.springframework.transaction.PlatformTransactionManager} object.
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public static SnmpCollectionAgent create(final Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
        return new DefaultSnmpCollectionAgent(DefaultSnmpCollectionAgentService.create(ifaceId, ifaceDao, transMgr));
    }

    public static SnmpCollectionAgent create(final Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr, final String location) {
        return new DefaultSnmpCollectionAgent(DefaultSnmpCollectionAgentService.create(ifaceId, ifaceDao, transMgr), location);
    }

    /**
     * Used to check for new interfaces
     */
    private int m_ifCount = -1;

    // cached attributes
    private int m_ifIndex = -1;
    private PrimaryType m_isSnmpPrimary = null;
    private String m_sysObjId = null;
    private Set<SnmpIfData> m_snmpIfData;

    private DefaultSnmpCollectionAgent(final SnmpCollectionAgentService agentService) {
        this(agentService, null);
    }

    protected DefaultSnmpCollectionAgent(final SnmpCollectionAgentService agentService, final String location) {
        super(agentService, location);

        if (Boolean.getBoolean("org.opennms.netmgt.collectd.DefaultCollectionAgent.loadSnmpDataOnInit")) {
            getSnmpInterfaceData();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setSavedIfCount(final int ifCount) {
        m_ifCount = ifCount;
    }

    /**
     * <p>getSavedIfCount</p>
     *
     * @return a int.
     */
    @Override
    public int getSavedIfCount() {
        return m_ifCount;
    }

    private int getIfIndex() {
        if (m_ifIndex == -1) {
            m_ifIndex = ((SnmpCollectionAgentService)m_agentService).getIfIndex();
        }
        return m_ifIndex;
        
    }

    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getSysObjectId() {
        if (m_sysObjId == null) {
            m_sysObjId = ((SnmpCollectionAgentService)m_agentService).getSysObjectId();
            // Intern the string value to save RAM
            m_sysObjId = (m_sysObjId == null ? null : m_sysObjId.intern());
        }
        return m_sysObjId;
    }

    private PrimaryType getIsSnmpPrimary() {
        if (m_isSnmpPrimary == null) {
            m_isSnmpPrimary = ((SnmpCollectionAgentService)m_agentService).getIsSnmpPrimary();
        }
        return m_isSnmpPrimary;
        
    }

    @Override
    protected void logCompletion() {
        LOG.debug("initialize: initialization completed: nodeid = {}, address = {}, primaryIfIndex = {}, isSnmpPrimary = {}, sysoid = {}", getNodeId(), getHostAddress(), getIfIndex(), getIsSnmpPrimary(), getSysObjectId());
    }

    private void validateSysObjId() throws CollectionInitializationException {
        if (getSysObjectId() == null) {
            throw new CollectionInitializationException("System Object ID for interface "
                                       + getHostAddress()
                                       + " does not exist in the database.");
        }
    }

    @Override
    protected void logCollectionParms() {
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

    @Override
    public SnmpAgentConfig getAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(getAddress(), getLocationName());
    }
    
    private Set<SnmpIfData> getSnmpInterfaceData() {
        if (m_snmpIfData == null) {
            m_snmpIfData = ((SnmpCollectionAgentService)m_agentService).getSnmpInterfaceData();
        }
        return m_snmpIfData;
        
    }

    /** {@inheritDoc} */
    @Override
    public Set<IfInfo> getSnmpInterfaceInfo(final IfResourceType type) {
        final Set<SnmpIfData> snmpIfData = getSnmpInterfaceData();
        final Set<IfInfo> ifInfos = new LinkedHashSet<IfInfo>(snmpIfData.size());

        for (final SnmpIfData ifData : snmpIfData) {
            if (Strings.isNullOrEmpty(ifData.getLabelForRRD())) {
                LOG.warn("Unable to compute resource index for interface: {}. " +
                        "Associated metrics will not be added to the CollectionSet and will not be considered" +
                        " for persistence,thresholding or other processing.", ifData);
                continue;
            }
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

}
