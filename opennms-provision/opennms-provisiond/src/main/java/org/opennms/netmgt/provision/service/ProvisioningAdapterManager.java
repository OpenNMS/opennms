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
package org.opennms.netmgt.provision.service;

import java.util.Collection;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.provision.ProvisioningAdapter;
import org.opennms.netmgt.provision.ProvisioningAdapterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * An adapter manager.  Makes writing tests much easier.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@EventListener(name="ProvisioningAdapterManager:EventListener", logPrefix="provisiond")
public class ProvisioningAdapterManager implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningAdapterManager.class);

    private PluginRegistry m_pluginRegistry;
    private Collection<ProvisioningAdapter> m_adapters;
    
    //may use this at some point
    private volatile EventForwarder m_eventForwarder;
    
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_pluginRegistry, "pluginRegistry must be set");
        m_adapters =  m_pluginRegistry.getAllPlugins(ProvisioningAdapter.class);
    }

    /**
     * <p>getPluginRegistry</p>
     *
     * @return a {@link org.opennms.netmgt.provision.service.PluginRegistry} object.
     */
    public PluginRegistry getPluginRegistry() {
        return m_pluginRegistry;
    }

    /**
     * <p>setPluginRegistry</p>
     *
     * @param pluginRegistry a {@link org.opennms.netmgt.provision.service.PluginRegistry} object.
     */
    public void setPluginRegistry(PluginRegistry pluginRegistry) {
        m_pluginRegistry = pluginRegistry;
    }

    /**
     * <p>handleNodeAddedEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAddedEvent(IEvent e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            LOG.info("handleNodeAddedEvent: Calling adapter:{} for node: {}", adapter.getName(), e.getNodeid());
            try {
                adapter.addNode(e.getNodeid().intValue());
            } catch (ProvisioningAdapterException pae) {
                LOG.error("handleNodeAddedEvent: Adapter threw known exception: {}", adapter.getName(), pae);
            } catch (Throwable t) {
                LOG.error("handleNodeAddedEvent: Unanticpated exception when calling adapter: {}", adapter.getName(), t);
            }
        }
    }

    /**
     * <p>handleNodeUpdatedEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_UPDATED_EVENT_UEI)
    public void handleNodeUpdatedEvent(IEvent e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            LOG.info("handleNodeUpdatedEvent: Calling adapter:{} for node: {}", adapter.getName(), e.getNodeid());
            try {
                adapter.updateNode(e.getNodeid().intValue());
            } catch (ProvisioningAdapterException pae) {
                LOG.error("handleNodeUpdatedEvent: Adapter threw known exception: {}", adapter.getName(), pae);
            } catch (Throwable t) {
                LOG.error("handleNodeUpdatedEvent: Unanticpated exception when calling adapter: {}", adapter.getName(), t);
            }
        }
    }
    
    /**
     * <p>handleNodeDeletedEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeletedEvent(IEvent e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            LOG.info("handleNodeDeletedEvent: Calling adapter:{} for node: {}", adapter.getName(), e.getNodeid());
            try {
                adapter.deleteNode(e.getNodeid().intValue());
            } catch (ProvisioningAdapterException pae) {
                LOG.error("handleNodeDeletedEvent: Adapter threw known exception: {}", adapter.getName(), pae);
            } catch (Throwable t) {
                LOG.error("handleNodeDeletedEvent: Unanticpated exception when calling adapter: {}", adapter.getName(), t);
            }
        }
    }
    
    /**
     * <p>handleNodeScanCompletedEvent</p>
     * 
     * Note: If the operations are properly scheduled and handled using the SimpleQueuedProvisioningAdapter, even though
     * this event is sent following a nodeUpdated event, the update operation task should be reduced to 1 operation on the queue.
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.PROVISION_SCAN_COMPLETE_UEI)
    public void handleNodeScanCompletedEvent(IEvent e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            LOG.info("handleScanCompletedEvent: Calling adapter:{} for node: {}", adapter.getName(), e.getNodeid());
            try {
                adapter.updateNode(e.getNodeid().intValue());
            } catch (ProvisioningAdapterException pae) {
                LOG.error("handleNodeScanCompletedEvent: Adapter threw known exception: {}", adapter.getName(), pae);
            } catch (Throwable t) {
                LOG.error("handleNodeScanCompletedEvent: Unanticpated exception when calling adapter: {}", adapter.getName(), t);
            }
        }
    }
    
    /**
     * <p>handleNodeChangedEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    @EventHandler(uei = EventConstants.NODE_CONFIG_CHANGE_UEI)
    public void handleNodeChangedEvent(IEvent e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            LOG.info("handleNodeChangedEvent: Calling adapter:{} for node: {}", adapter.getName(), e.getNodeid());
            try {
                if (e.getNodeid() != 0) {
                    adapter.nodeConfigChanged(e.getNodeid().intValue());
                } else {
                    LOG.warn("handleNodeChangedEvent: received configChanged event without nodeId: {}", e);
                }
            } catch (ProvisioningAdapterException pae) {
                LOG.error("handleNodeChangedEvent: Adapter threw known exception: {}", adapter.getName(), pae);
            } catch (Throwable t) {
                LOG.error("handleNodeChangedEvent: Unanticpated exception when calling adapter: {}", adapter.getName(), t);
            }
        }
    }

    /**
     * <p>setEventForwarder</p>
     *
     * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     */
    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * <p>getEventForwarder</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventForwarder} object.
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * <p>initializeAdapters</p>
     */
    public void initializeAdapters() {
        for (ProvisioningAdapter adapter : m_adapters) {
            adapter.init();
        }
    }

}
