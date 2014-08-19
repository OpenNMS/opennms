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
import java.util.HashMap;
import java.util.Map;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.HwInventoryAdapterConfigurationDao;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.hardware.HwExtension;
import org.opennms.netmgt.config.hardware.MibObj;
import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.provision.plugin.SnmpEntityPlugin;
import org.opennms.netmgt.provision.plugin.EntityPlugin;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
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
    private HwEntityDao m_hwEntityDao;
    private HwEntityAttributeTypeDao m_hwEntityAttributeTypeDao;
    private EventForwarder m_eventForwarder;
    private SnmpAgentConfigFactory m_snmpConfigDao;
    private HwInventoryAdapterConfigurationDao m_hwInventoryAdapterConfigurationDao;

    private Map<SnmpObjId, HwEntityAttributeType> m_vendorAttributes = new HashMap<SnmpObjId, HwEntityAttributeType>();
    private MapperFacade m_mapper;

    public static final String NAME = "HardwareInventoryProvisioningAdapter";

    public HardwareInventoryProvisioningAdapter() {
        super(NAME);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_nodeDao, "Node DAO cannot be null");
        Assert.notNull(m_hwEntityDao, "Hardware Entity DAO cannot be null");
        Assert.notNull(m_hwEntityAttributeTypeDao, "Hardware Entity Attribute Type DAO cannot be null");
        Assert.notNull(m_snmpConfigDao, "SNMP Configuration DAO cannot be null");
        Assert.notNull(m_hwInventoryAdapterConfigurationDao, "Hardware Inventory Configuration DAO cannot be null");
        Assert.notNull(m_eventForwarder, "Event Forwarder cannot be null");
        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        factory.classMap(OnmsHwEntity.class, OnmsHwEntity.class).exclude("id").exclude("node").register();
        m_mapper = factory.getMapperFacade();
        initializeVendorAttributes();
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

    // FIXME Set on the configuration file if the data must be overridden if exist. 
    private void synchronizeInventory(int nodeId) {
        final OnmsNode node = m_nodeDao.get(nodeId);
        if (node == null) {
            throw new ProvisioningAdapterException("Failed to return node for given nodeId: " + nodeId);
        }

        final OnmsIpInterface intf = node.getPrimaryInterface();
        if (intf == null) {
            throw new ProvisioningAdapterException("Can't find the SNMP Primary IP address for nodeId: " + nodeId);            
        }
        final InetAddress ipAddress = intf.getIpAddress();

        EventBuilder ebldr = null;
        try {
            EntityPlugin plugin = null;
            if (node.getSysObjectId() == null) {
                LOG.warn("Skiping hardware discover because the node {} doesn't support SNMP", nodeId);
                return;
                // TODO, use ScriptEntityPlugin
            } else {
                SnmpAgentConfig agentConfig = m_snmpConfigDao.getAgentConfig(ipAddress);
                plugin = new SnmpEntityPlugin(m_hwInventoryAdapterConfigurationDao.getConfiguration(), m_vendorAttributes, agentConfig, node.getSysObjectId());
            }
            // EntityPlugin should always return a valid root otherwise it will throw an exception.
            OnmsHwEntity newRoot = plugin.getRootEntity(nodeId, ipAddress);
            // If there is an entity associated with the node it should be removed first, in order to override the data.
            final OnmsHwEntity currentRoot = m_hwEntityDao.findRootByNodeId(node.getId());
            if (currentRoot != null) {
                LOG.info("Merging existing hardware information for nodeId {}", nodeId);
                m_mapper.map(newRoot, currentRoot);
                m_hwEntityDao.update(currentRoot);
            } else {
                newRoot.setNode(node);
                m_hwEntityDao.save(newRoot);
            }
            ebldr = new EventBuilder(EventConstants.HARDWARE_INVENTORY_SUCCESSFUL_UEI, "Provisiond." + NAME);
        } catch (Throwable e) {
            ebldr = new EventBuilder(EventConstants.HARDWARE_INVENTORY_FAILED_UEI, "Provisiond." + NAME);
            ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
        }

        if (ebldr != null) {
            ebldr.setNodeid(nodeId);
            ebldr.setInterface(ipAddress);
            getEventForwarder().sendNow(ebldr.getEvent());
        }
    }

    private void initializeVendorAttributes() {
        m_vendorAttributes.clear();
        for (HwEntityAttributeType type : m_hwEntityAttributeTypeDao.findAll()) {
            m_vendorAttributes.put(type.getSnmpObjId(), type);
        }
        for (HwExtension ext : m_hwInventoryAdapterConfigurationDao.getConfiguration().getExtensions()) {
            for (MibObj obj : ext.getMibObjects()) {
                HwEntityAttributeType type = m_vendorAttributes.get(obj.getSnmpObjId());
                if (type == null) {
                    type = new HwEntityAttributeType(obj.getOid(), obj.getAlias(), obj.getType());
                    LOG.info("Creating attribute type {}", type);
                    m_hwEntityAttributeTypeDao.save(type);
                    m_vendorAttributes.put(type.getSnmpObjId(), type);
                }
            }
        }
    }

    @Override
    public void doNotifyConfigChange(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doNodeConfigChanged: nodeid: {}", nodeId);
    }

    public HwEntityDao getHwEntityDao() {
        return m_hwEntityDao;
    }

    public void setHwEntityDao(HwEntityDao hwEntityDao) {
        this.m_hwEntityDao = hwEntityDao;
    }

    public HwEntityAttributeTypeDao getHwEntityAttributeTypeDao() {
        return m_hwEntityAttributeTypeDao;
    }

    public void setHwEntityAttributeTypeDao(HwEntityAttributeTypeDao hwEntityAttributeTypeDao) {
        this.m_hwEntityAttributeTypeDao = hwEntityAttributeTypeDao;
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

    public HwInventoryAdapterConfigurationDao getHwAdapterConfigurationDao() {
        return m_hwInventoryAdapterConfigurationDao;
    }

    public void setHwInventoryAdapterConfigurationDao(HwInventoryAdapterConfigurationDao hwInventoryAdapterConfigurationDao) {
        this.m_hwInventoryAdapterConfigurationDao = hwInventoryAdapterConfigurationDao;
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
                m_hwInventoryAdapterConfigurationDao.reload();
                initializeVendorAttributes();
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
