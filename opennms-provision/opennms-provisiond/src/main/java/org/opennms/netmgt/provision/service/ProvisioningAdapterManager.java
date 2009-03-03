/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service;

import java.util.Collection;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.EventSubscriptionService;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.ProvisioningAdapter;
import org.opennms.netmgt.provision.ProvisioningAdapterException;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


/**
 * And adapter manager.  Makes writing tests much easier.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@EventListener(name="ProvisioningAdapterManager:EventListener")
public class ProvisioningAdapterManager implements InitializingBean {

    private PluginRegistry m_pluginRegistry;
    private Collection<ProvisioningAdapter> m_adapters;
    
    //may use this at some point
    private volatile EventSubscriptionService m_eventSubscriptionService;
    private volatile EventForwarder m_eventForwarder;
    
    
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_pluginRegistry, "pluginRegistry must be set");
        Assert.notNull(m_eventSubscriptionService);
        m_adapters =  m_pluginRegistry.getAllPlugins(ProvisioningAdapter.class);
    }

    public PluginRegistry getPluginRegistry() {
        return m_pluginRegistry;
    }

    public void setPluginRegistry(PluginRegistry pluginRegistry) {
        m_pluginRegistry = pluginRegistry;
    }

    @EventHandler(uei = EventConstants.NODE_ADDED_EVENT_UEI)
    public void handleNodeAddedEvent(Event e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            log().info("handleNodeAddedEvent: Calling adapter:"+adapter.getName()+" for node: "+e.getNodeid());
            try {
                adapter.addNode((int) e.getNodeid());
            } catch (ProvisioningAdapterException pae) {
                log().error("handleNodeAddedEvent: Adapter threw known exception: "+adapter.getName(), pae);
            } catch (Throwable t) {
                log().error("handleNodeAddedEvent: Unanticpated exception when calling adapter: "+adapter.getName(), t);
            }
        }
    }

    @EventHandler(uei = EventConstants.NODE_UPDATED_EVENT_UEI)
    public void handleNodeUpdatedEvent(Event e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            log().info("handleNodeUpdatedEvent: Calling adapter:"+adapter.getName()+" for node: "+e.getNodeid());
            try {
                adapter.updateNode((int) e.getNodeid());
            } catch (ProvisioningAdapterException pae) {
                log().error("handleNodeUpdatedEvent: Adapter threw known exception: "+adapter.getName(), pae);
            } catch (Throwable t) {
                log().error("handleNodeUpdatedEvent: Unanticpated exception when calling adapter: "+adapter.getName(), t);
            }
        }
    }
    
    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void handleNodeDeletedEvent(Event e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            log().info("handleNodeDeletedEvent: Calling adapter:"+adapter.getName()+" for node: "+e.getNodeid());
            try {
                adapter.deleteNode((int) e.getNodeid());
            } catch (ProvisioningAdapterException pae) {
                log().error("handleNodeDeletedEvent: Adapter threw known exception: "+adapter.getName(), pae);
            } catch (Throwable t) {
                log().error("handleNodeDeletedEvent: Unanticpated exception when calling adapter: "+adapter.getName(), t);
            }
        }
    }
    
    @EventHandler(uei = EventConstants.NODE_CONFIG_CHANGE_UEI)
    public void handleNodeChangedEvent(Event e) {
        for (ProvisioningAdapter adapter : m_adapters) {
            log().info("handleNodeChangedEvent: Calling adapter:"+adapter.getName()+" for node: "+e.getNodeid());
            try {
                if (e.getNodeid() != 0) {
                    adapter.nodeConfigChanged((int) e.getNodeid());
                } else {
                    log().warn("handleNodeChangedEvent: received configChanged event without nodeId: "+e);
                }
            } catch (ProvisioningAdapterException pae) {
                log().error("handleNodeChangedEvent: Adapter threw known exception: "+adapter.getName(), pae);
            } catch (Throwable t) {
                log().error("handleNodeChangedEvent: Unanticpated exception when calling adapter: "+adapter.getName(), t);
            }
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        m_eventSubscriptionService = eventSubscriptionService;
    }

    public EventSubscriptionService getEventSubscriptionService() {
        return m_eventSubscriptionService;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

}
