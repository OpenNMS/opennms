/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.wsman;

import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.opennms.core.wsman.Identity;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.PersistsAgentInfo;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;

/**
 * Detects the presence of the WS-Man service by verifying that the
 * endpoint responds to the Identify command. 
 *
 * @author jwhite
 */
@Component
@Scope("prototype")
public class WsManDetector extends SyncAbstractDetector implements InitializingBean, PersistsAgentInfo {
    public static final Logger LOG = LoggerFactory.getLogger(WsManDetector.class);

    private static final String PROTOCOL_NAME = "WS-Man";

    private final WSManClientFactory m_factory = new CXFWSManClientFactory();

    private final ReadWriteLock m_lock = new ReentrantReadWriteLock();

    private final Map<InetAddress, Identity> m_identifyByInetAddress = Maps.newHashMap();

    @Autowired
    private WSManConfigDao m_wsmanConfigDao;

    @Autowired
    private NodeDao m_nodeDao;

    private boolean m_updateAssets = true;

    protected WsManDetector() {
        super(PROTOCOL_NAME, 0);
    }

    @Override
    public synchronized boolean isServiceDetected(InetAddress address) {
        // Issue the "Identify" request
        final WSManEndpoint endpoint = m_wsmanConfigDao.getEndpoint(address);
        final WSManClient client = m_factory.getClient(endpoint);
        Identity identity = null;
        try {
            identity = client.identify();
            LOG.info("Identify succeeded for address {} with product vendor '{}' and product version '{}'.", address, identity.getProductVendor(), identity.getProductVersion());
        } catch (WSManException e) {
            LOG.info("Identify failed for address {} with endpoint {}.", address, endpoint, e);
        }

        // Cache the result
        m_lock.writeLock().lock();
        try {
            m_identifyByInetAddress.put(address, identity);
        } finally {
            m_lock.writeLock().unlock();
        }

        return identity != null;
    }

    @Override
    @Transactional
    public void persistAgentInfo(Integer nodeId, InetAddress address) {
        if (!m_updateAssets) {
            LOG.debug("Asset updates disabled.");
            return;
        }

        Identity identity = null;
        m_lock.readLock().lock();
        try {
            identity = m_identifyByInetAddress.get(address);
        } finally {
            m_lock.readLock().unlock();
        }

        if (identity == null) {
            // Nothing to persist
            return;
        }

        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            LOG.warn("No node was found with id: {}", nodeId);
            return;
        }

        LOG.debug("Updating vendor and modelNumber assets on node[{}] with '{}' and '{}'",
                nodeId, identity.getProductVendor(), identity.getProductVersion());
        node.getAssetRecord().setVendor(identity.getProductVendor());
        node.getAssetRecord().setModelNumber(identity.getProductVersion());
        m_nodeDao.update(node);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(m_wsmanConfigDao, "wsmanConfigDao attribute");
        Objects.requireNonNull(m_nodeDao, "nodeDao attribute");
    }

    @Override
    protected void onInit() {
        // pass
    }

    @Override
    public void dispose() {
        // pass
    }

    public void setUpdateAssets(boolean updateAssets) {
        m_updateAssets = updateAssets;
    }

    public boolean getUpdateAssets() {
        return m_updateAssets;
    }
}
