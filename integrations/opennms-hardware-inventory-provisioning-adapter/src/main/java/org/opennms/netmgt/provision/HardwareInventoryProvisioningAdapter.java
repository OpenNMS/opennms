/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.net.InetAddress;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.snmp.EntityPhysicalTableTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

@EventListener(name=HardwareInventoryProvisioningAdapter.NAME)
public class HardwareInventoryProvisioningAdapter extends SimplerQueuedProvisioningAdapter implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(HardwareInventoryProvisioningAdapter.class);

    private NodeDao m_nodeDao;
    private EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_snmpConfigDao;

    public static final String NAME = "HardwareInventoryProvisioningAdapter";

    public HardwareInventoryProvisioningAdapter() {
        super(NAME);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_nodeDao, "Node DAO cannot be null");
        Assert.notNull(m_snmpConfigDao, "SNMP Configuration DAO cannot be null");
        Assert.notNull(m_eventForwarder, "Event Forwarder cannot be null");
    }

    @Override
    public void doAddNode(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doAdd: adding nodeid: {}", nodeId);
        synchronizeInventory(nodeId);
    }

    @Override
    public void doUpdateNode(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doUpdate: updating nodeid: {}", nodeId);
        synchronizeInventory(nodeId);
    }

    // FIXME Load and Execute Plugins
    // FIXME StandardEntityMibInventoryPlugin
    // FIXME VendorEntityMibInventoryPlugin
    private void synchronizeInventory(int nodeId) throws ProvisioningAdapterException {
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ProvisioningAdapterException("Failed to return node for given nodeId: " + nodeId);
        }

        final OnmsIpInterface intf = node.getPrimaryInterface();
        if (intf == null) {
            throw new ProvisioningAdapterException("Can't find the SNMP Primary IP address for nodeId: " + nodeId);            
        }
        final InetAddress ipAddress = intf.getIpAddress();

        SnmpAgentConfig agentConfig = m_snmpConfigDao.getAgentConfig(ipAddress);
        LOG.debug("synchronizeInventory: Getting ENTITY-MIB using {}", agentConfig);

        final EntityPhysicalTableTracker tracker = new EntityPhysicalTableTracker();
        final String trackerName = tracker.getClass().getSimpleName() + '_' + nodeId;
        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, trackerName, tracker);
        walker.start();
        try {
            walker.waitFor();
            if (walker.timedOut()) {
                LOG.info("synchronizeInventory: Aborting entities scan: Agent timed out while scanning the {} table", trackerName);
                return;
            }  else if (walker.failed()) {
                LOG.info("synchronizeInventory: Aborting entities scan: Agent failed while scanning the {} table: {}", trackerName,walker.getErrorMessage());
                return;
            }
        } catch (final InterruptedException e) {
            LOG.error("synchronizeInventory: ENTITY-MIB node collection interrupted, exiting",e );
            return;
        }
        
        OnmsHwEntity root = tracker.getRootEntity();
        if (root == null) {
            LOG.error("synchronizeInventory: Cannot get root entity for nodeId {}", nodeId);
            return;
        }

        node.setRootHwEntity(root);
        m_nodeDao.saveOrUpdate(node);
        m_nodeDao.flush();
    }

    @Override
    public void doNotifyConfigChange(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doNodeConfigChanged: nodeid: {}", nodeId);
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(final NodeDao dao) {
        m_nodeDao = dao;
    }

    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    public void setEventForwarder(final EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public SnmpAgentConfigFactory getSnmpPeerFactory() {
        return m_snmpConfigDao;
    }

    public void setSnmpPeerFactory(final SnmpAgentConfigFactory snmpConfigDao) {
        this.m_snmpConfigDao = snmpConfigDao;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(final Event event) {
        if (isReloadConfigEventTarget(event)) {
            EventBuilder ebldr = null;
            LOG.debug("Reloading the Hardware Inventory adapter configuration");
            try {

                // FIXME Reload Configuration

                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Provisiond." + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond." + NAME);
            } catch (Throwable e) {
                LOG.info("Unable to reload SNMP asset adapter configuration", e);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Provisiond." + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond." + NAME);
                ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(1, 128));
            }
            if (ebldr != null) {
                getEventForwarder().sendNow(ebldr.getEvent());
            }
        }
    }

    private boolean isReloadConfigEventTarget(final Event event) {
        boolean isTarget = false;
        for (final Parm parm : event.getParmCollection()) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && ("Provisiond." + NAME).equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        LOG.debug("isReloadConfigEventTarget: Provisiond. {} was target of reload event: {}", isTarget, NAME);
        return isTarget;
    }

}
