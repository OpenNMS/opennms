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

package org.opennms.netmgt.collection.core;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentService;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Represents a remote agent on a specific IPv4 interface.
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultCollectionAgent implements CollectionAgent {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCollectionAgent.class);

    /**
     * <p>create</p>
     *
     * @param ifaceId a {@link java.lang.Integer} object.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.api.IpInterfaceDao} object.
     * @param transMgr a {@link org.springframework.transaction.PlatformTransactionManager} object.
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public static CollectionAgent create(final Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr) {
        return new DefaultCollectionAgent(DefaultCollectionAgentService.create(ifaceId, ifaceDao, transMgr));
    }

    public static CollectionAgent create(final Integer ifaceId, final IpInterfaceDao ifaceDao, final PlatformTransactionManager transMgr, final String location) {
        return new DefaultCollectionAgent(DefaultCollectionAgentService.create(ifaceId, ifaceDao, transMgr), location);
    }

    /**
     * Used to track system restarts
     */
    private long m_sysUpTime = -1;

    // fixed attributes
    private final int m_nodeId;
    private final InetAddress m_inetAddress;

    // cached attributes
    private String m_foreignSource = null;
    private String m_foreignId = null;
    private String m_locationName = null;
    private String m_nodeLabel = null;
    private ResourcePath m_storageResourcePath = null;

    protected CollectionAgentService m_agentService;

    private DefaultCollectionAgent(final CollectionAgentService agentService) {
        this(agentService, null);
    }

    protected DefaultCollectionAgent(final CollectionAgentService agentService, final String location) {
        m_agentService = agentService;
        m_storageResourcePath = agentService.getStorageResourcePath();
        m_locationName = location;
        m_inetAddress = m_agentService.getInetAddress();
        m_nodeId = m_agentService.getNodeId();
    }

    /** {@inheritDoc} */
    @Override
    public final InetAddress getAddress() {
        return m_inetAddress;
    }

    /**
     * The map of attributes for this interface.
     */
    private transient Map<String, Object> m_properties;

    @Override
    public final Set<String> getAttributeNames() {
        return m_properties != null ? m_properties.keySet() : Collections.emptySet();
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * This method is used to return the object that is associated with the
     * property name. This is very similar to the java.util.Map get() method,
     * but requires that the lookup be performed using a String name. The object
     * may be of any instance that the monitor previous stored.
     * </P>
     *
     * <P>
     * If there is no matching object for the property key, then a null pointer
     * is returned to the application.
     * </P>
     * @exception java.lang.IllegalArgumentException
     *                Thrown if the passed key is empty or null.
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public final synchronized <V> V getAttribute(String property) {
        Object rc = null;
        if (m_properties != null)
            rc = m_properties.get(property);

        // Can't avoid this unchecked cast
        @SuppressWarnings("unchecked")
        V retval = (V)rc;
        return retval;
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * This method is used to associate an object value with a textual key. If a
     * previous value was associated with the key then the old value is returned
     * to the caller. This is identical to the behavior defined by the
     * java.util.Map put() method. The only restriction is that the key must be
     * a java string instance.
     * </P>
     * @exception java.lang.IllegalArgumentException
     *                Thrown if the property name is empty or null.
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public final synchronized Object setAttribute(String property, Object value) {
        if (m_properties == null)
            m_properties = new HashMap<String, Object>();

        return m_properties.put(property, value);
    }

    /**
     * <p>isStoreByForeignSource</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    @Override
    public final Boolean isStoreByForeignSource() {
        return ResourceTypeUtils.isStoreByForeignSource();
    }

    /**
     * <p>getHostAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getHostAddress() {
        return InetAddressUtils.str(getAddress());
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    @Override
    public final int getNodeId() {
        return m_nodeId; 
    }

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getNodeLabel() {
        if (m_nodeLabel == null) {
            m_nodeLabel = m_agentService.getNodeLabel();
        }
        return m_nodeLabel;
    }

    /**
     * <p>getForeignSource</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getForeignSource() {
        if (m_foreignSource == null) {
            m_foreignSource = m_agentService.getForeignSource();
        }
        return m_foreignSource;
    }
 
    /**
     * <p>getForeignId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getForeignId() {
        if (m_foreignId == null) {
            m_foreignId = m_agentService.getForeignId();
        }
        return m_foreignId;
    }

    @Override
    public final String getLocationName() {
        if (m_locationName == null) {
            m_locationName = m_agentService.getLocationName();
        }
        return m_locationName;
    }

    @Override
    public final ResourcePath getStorageResourcePath() {
        return m_storageResourcePath;
    }

    protected void logCompletion() {
        LOG.debug("initialize: initialization completed: nodeid = {}, address = {}", getNodeId(), getHostAddress());
    }

    protected void logCollectionParms() {
        LOG.debug("initialize: db retrieval info: nodeid = {}, address = {}", getNodeId(), getHostAddress());
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "Agent[nodeid = "+getNodeId()+" ipaddr= "+getHostAddress()+']';
    }

    /**
     * <p>getSavedSysUpTime</p>
     *
     * @return a long.
     */
    @Override
    public final long getSavedSysUpTime() {
        return m_sysUpTime;
    }

    /** {@inheritDoc} */
    @Override
    public final void setSavedSysUpTime(final long sysUpTime) {
        m_sysUpTime = sysUpTime;
    }

}
