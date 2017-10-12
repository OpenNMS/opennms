/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Seth
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
@EventListener(name="OpenNMS.InterfaceToNodeCache", logPrefix="eventd")
public class InterfaceToNodeCacheEventProcessor implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceToNodeCacheEventProcessor.class);

    @Autowired
    private InterfaceToNodeCache m_cache;

    @Autowired
    private NodeDao m_nodeDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        // Initialize the cache when this listener is created
        //
        // TODO: Should we periodically rebuild the cache from
        // scratch?
        //
        m_cache.dataSourceSync();
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)
    @Transactional
    public void handleNodeGainedInterface(Event event) {
        LOG.debug("Received event: {}", event.getUei());
        Long nodeId = event.getNodeid();
        if (nodeId == null) {
            LOG.error(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI + ": Event with no node ID: " + event.toString());
            return;
        }
        OnmsNode node = m_nodeDao.get(nodeId.intValue());
        if (node == null) {
            LOG.warn(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI + ": Cannot find node in DB: " + nodeId);
            return;
        }
        // add to known nodes
        m_cache.setNodeId(node.getLocation().getLocationName(), event.getInterfaceAddress(), nodeId.intValue());
    }

    @EventHandler(uei=EventConstants.INTERFACE_DELETED_EVENT_UEI)
    @Transactional
    public void handleInterfaceDeleted(Event event) {
        LOG.debug("Received event: {}", event.getUei());
        Long nodeId = event.getNodeid();
        if (nodeId == null) {
            LOG.error(EventConstants.INTERFACE_DELETED_EVENT_UEI + ": Event with no node ID: " + event.toString());
            return;
        }
        OnmsNode node = m_nodeDao.get(nodeId.intValue());
        if (node == null) {
            LOG.warn(EventConstants.INTERFACE_DELETED_EVENT_UEI + ": Cannot find node in DB: " + nodeId);
            return;
        }
        // remove from known nodes
        m_cache.removeNodeId(node.getLocation().getLocationName(), event.getInterfaceAddress(), nodeId.intValue());
    }

    @EventHandler(uei=EventConstants.INTERFACE_REPARENTED_EVENT_UEI)
    @Transactional
    public void handleInterfaceReparented(Event event) {
        LOG.debug("Received event: {}", event.getUei());

        final String oldNodeId = event.getParm(EventConstants.PARM_OLD_NODEID).getValue().getContent();
        final String newNodeId = event.getParm(EventConstants.PARM_NEW_NODEID).getValue().getContent();

        if (oldNodeId == null) {
            LOG.error(EventConstants.INTERFACE_REPARENTED_EVENT_UEI + ": Event with no node ID: " + event.toString());
            return;
        }

        if (newNodeId == null) {
            LOG.error(EventConstants.INTERFACE_REPARENTED_EVENT_UEI + ": Event with no node ID: " + event.toString());
            return;
        }

        final OnmsNode oldNode = m_nodeDao.get(oldNodeId);
        if (oldNode == null) {
            LOG.warn(EventConstants.INTERFACE_REPARENTED_EVENT_UEI + ": Cannot find node in DB: " + oldNodeId);
            return;
        }

        final OnmsNode newNode = m_nodeDao.get(newNodeId);
        if (newNode == null) {
            LOG.warn(EventConstants.INTERFACE_REPARENTED_EVENT_UEI + ": Cannot find node in DB: " + newNodeId);
            return;
        }

        // add to known nodes
        m_cache.removeNodeId(oldNode.getLocation().getLocationName(), event.getInterfaceAddress(), oldNode.getId());
        m_cache.setNodeId(newNode.getLocation().getLocationName(), event.getInterfaceAddress(), newNode.getId());
    }
}
