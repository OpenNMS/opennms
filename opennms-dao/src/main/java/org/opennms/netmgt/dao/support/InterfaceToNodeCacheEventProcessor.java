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
package org.opennms.netmgt.dao.support;

import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.OnmsNode;
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
        // Initialization of the cache has moved to the cache implementation itself.
        // The same is true for refreshing the cache
    }

    @EventHandler(uei=EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)
    @Transactional
    public void handleNodeGainedInterface(IEvent event) {
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
    public void handleInterfaceDeleted(IEvent event) {
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
    public void handleInterfaceReparented(IEvent event) {
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

    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    @Transactional
    public void handleNodeDeleted(IEvent event) {
        Long nodeId = event.getNodeid();
        LOG.debug("Received event: {} with nodeId = {}", event.getUei(), nodeId);
        if (nodeId == null) {
            LOG.error("{} : Event with no node ID: {}", EventConstants.NODE_DELETED_EVENT_UEI, event.toString());
            return;
        }
        // remove all interfaces for this node.
        m_cache.removeInterfacesForNode(nodeId.intValue());
    }
}
