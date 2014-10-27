/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpHwInventoryAdapterConfigDao;
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
import org.opennms.netmgt.provision.snmp.EntityPhysicalTableRow;
import org.opennms.netmgt.provision.snmp.EntityPhysicalTableTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The Class SnmpHardwareInventoryProvisioningAdapter.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@EventListener(name=SnmpHardwareInventoryProvisioningAdapter.NAME)
public class SnmpHardwareInventoryProvisioningAdapter extends SimplerQueuedProvisioningAdapter implements InitializingBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SnmpHardwareInventoryProvisioningAdapter.class);

    /** The Constant PREFIX. */
    public static final String PREFIX = "Provisiond.";

    /** The Constant NAME. */
    public static final String NAME = "SnmpHardwareInventoryProvisioningAdapter";

    /** The node DAO. */
    private NodeDao m_nodeDao;

    /** The hardware entity DAO. */
    private HwEntityDao m_hwEntityDao;

    /** The hardware entity attribute type DAO. */
    private HwEntityAttributeTypeDao m_hwEntityAttributeTypeDao;

    /** The event forwarder. */
    private EventForwarder m_eventForwarder;

    /** The SNMP configuration DAO. */
    private SnmpAgentConfigFactory m_snmpConfigDao;

    /** The hardware inventory adapter configuration DAO. */
    private SnmpHwInventoryAdapterConfigDao m_hwInventoryAdapterConfigDao;

    /** The vendor attributes. */
    private Map<SnmpObjId, HwEntityAttributeType> m_vendorAttributes = new HashMap<SnmpObjId, HwEntityAttributeType>();

    /**
     * The Constructor.
     */
    public SnmpHardwareInventoryProvisioningAdapter() {
        super(NAME);
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_nodeDao, "Node DAO cannot be null");
        Assert.notNull(m_hwEntityDao, "Hardware Entity DAO cannot be null");
        Assert.notNull(m_hwEntityAttributeTypeDao, "Hardware Entity Attribute Type DAO cannot be null");
        Assert.notNull(m_snmpConfigDao, "SNMP Configuration DAO cannot be null");
        Assert.notNull(m_hwInventoryAdapterConfigDao, "Hardware Inventory Configuration DAO cannot be null");
        Assert.notNull(m_eventForwarder, "Event Forwarder cannot be null");
        initializeVendorAttributes();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.SimplerQueuedProvisioningAdapter#doAddNode(int)
     */
    @Override
    public void doAddNode(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doAdd: adding nodeid: {}", nodeId);
        synchronizeInventory(nodeId);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.SimplerQueuedProvisioningAdapter#doUpdateNode(int)
     */
    @Override
    public void doUpdateNode(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doUpdate: updating nodeid: {}", nodeId);
        synchronizeInventory(nodeId);
    }

    /**
     * Synchronize inventory.
     * <p>Obtain the ENTITY-MIB and vendor attributes from the target node through SNMP.</p>
     * <p>If the node has a hardware inventory data on the database, this is going to be overridden only,
     * if the gathered data differs from the data at the database, otherwise the gathered data will be
     * discarded.</p>
     *
     * @param nodeId the node id
     */
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
            if (node.getSysObjectId() == null) {
                LOG.warn("Skiping hardware discover because the node {} doesn't support SNMP", nodeId);
                return;
            }
            SnmpAgentConfig agentConfig = m_snmpConfigDao.getAgentConfig(ipAddress);
            final OnmsHwEntity newRoot = getRootEntity(agentConfig, node);
            newRoot.setNode(node);
            final OnmsHwEntity currentRoot = m_hwEntityDao.findRootByNodeId(node.getId());
            if (newRoot.equals(currentRoot)) {
                LOG.info("No changes detected on the hardware inventory for nodeId {}", nodeId);
                return;
            }
            if (currentRoot == null) {
                LOG.info("Saving hardware inventory for nodeId {}", nodeId);
            } else {
                LOG.info("Updating hardware inventory for nodeId {}", nodeId);
                m_hwEntityDao.delete(currentRoot);
                m_hwEntityDao.flush();
            }
            m_hwEntityDao.saveOrUpdate(newRoot);
            ebldr = new EventBuilder(EventConstants.HARDWARE_INVENTORY_SUCCESSFUL_UEI, PREFIX + NAME);
        } catch (Throwable e) {
            ebldr = new EventBuilder(EventConstants.HARDWARE_INVENTORY_FAILED_UEI, PREFIX + NAME);
            ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
        }

        if (ebldr != null) {
            ebldr.setNodeid(nodeId);
            ebldr.setInterface(ipAddress);
            getEventForwarder().sendNow(ebldr.getEvent());
        }
    }

    /**
     * Initialize vendor attributes.
     */
    private void initializeVendorAttributes() {
        m_vendorAttributes.clear();
        for (HwEntityAttributeType type : m_hwEntityAttributeTypeDao.findAll()) {
            LOG.debug("Loading attribute type {}", type);
            m_vendorAttributes.put(type.getSnmpObjId(), type);
        }
        for (HwExtension ext : m_hwInventoryAdapterConfigDao.getConfiguration().getExtensions()) {
            for (MibObj obj : ext.getMibObjects()) {
                HwEntityAttributeType type = m_vendorAttributes.get(obj.getOid());
                if (type == null) {
                    type = new HwEntityAttributeType(obj.getOid().toString(), obj.getAlias(), obj.getType());
                    LOG.info("Creating attribute type {}", type);
                    m_hwEntityAttributeTypeDao.save(type);
                    m_vendorAttributes.put(type.getSnmpObjId(), type);
                }
            }
        }
    }

    /**
     * Gets the root entity.
     *
     * @param agentConfig the agent configuration
     * @param node the node
     * @return the root entity
     * @throws HardwareInventoryException the hardware inventory exception
     */
    private OnmsHwEntity getRootEntity(SnmpAgentConfig agentConfig, OnmsNode node) throws SnmpHardwareInventoryException {
        LOG.debug("getRootEntity: Getting ENTITY-MIB using {}", agentConfig);

        final List<SnmpObjId> vendorOidList = m_hwInventoryAdapterConfigDao.getConfiguration().getVendorOid(node.getSysObjectId());
        final Map<String,String> replacementMap = m_hwInventoryAdapterConfigDao.getConfiguration().getReplacementMap();
        final SnmpObjId[] vendorOids = vendorOidList.toArray(new SnmpObjId[vendorOidList.size()]);
        final SnmpObjId[] allOids = (SnmpObjId[]) ArrayUtils.addAll(EntityPhysicalTableRow.ELEMENTS, vendorOids);
        final EntityPhysicalTableTracker tracker = new EntityPhysicalTableTracker(m_vendorAttributes, allOids, replacementMap);
        final String trackerName = tracker.getClass().getSimpleName() + '_' + node.getLabel();

        final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, trackerName, tracker);
        walker.start();
        try {
            walker.waitFor();
            if (walker.timedOut()) {
                throw new SnmpHardwareInventoryException("Aborting entities scan: Agent timed out while scanning the " + trackerName + " table");
            }  else if (walker.failed()) {
                throw new SnmpHardwareInventoryException("Aborting entities scan: Agent failed while scanning the " + trackerName + " table: " + walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            throw new SnmpHardwareInventoryException("ENTITY-MIB node collection interrupted, exiting");
        }

        OnmsHwEntity root = tracker.getRootEntity();
        if (root == null) {
            throw new SnmpHardwareInventoryException("Cannot get root entity for node " + node.getLabel() + ", it seems like the node does not have an implementation for the entPhysicalTable of the ENTITY-MIB.");
        }

        return root;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.SimplerQueuedProvisioningAdapter#doNotifyConfigChange(int)
     */
    @Override
    public void doNotifyConfigChange(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doNodeConfigChanged: nodeid: {}", nodeId);
    }

    /**
     * Gets the hardware entity DAO.
     *
     * @return the hardware entity DAO
     */
    public HwEntityDao getHwEntityDao() {
        return m_hwEntityDao;
    }

    /**
     * Sets the hardware entity DAO.
     *
     * @param hwEntityDao the hardware entity DAO
     */
    public void setHwEntityDao(HwEntityDao hwEntityDao) {
        this.m_hwEntityDao = hwEntityDao;
    }

    /**
     * Gets the hardware entity attribute type DAO.
     *
     * @return the hardware entity attribute type DAO
     */
    public HwEntityAttributeTypeDao getHwEntityAttributeTypeDao() {
        return m_hwEntityAttributeTypeDao;
    }

    /**
     * Sets the hardware entity attribute type DAO.
     *
     * @param hwEntityAttributeTypeDao the hardware entity attribute type DAO
     */
    public void setHwEntityAttributeTypeDao(HwEntityAttributeTypeDao hwEntityAttributeTypeDao) {
        this.m_hwEntityAttributeTypeDao = hwEntityAttributeTypeDao;
    }

    /**
     * Gets the node DAO.
     *
     * @return the node DAO
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * Sets the node DAO.
     *
     * @param dao the node DAO
     */
    public void setNodeDao(final NodeDao dao) {
        m_nodeDao = dao;
    }

    /**
     * Gets the event forwarder.
     *
     * @return the event forwarder
     */
    public EventForwarder getEventForwarder() {
        return m_eventForwarder;
    }

    /**
     * Sets the event forwarder.
     *
     * @param eventForwarder the event forwarder
     */
    public void setEventForwarder(final EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    /**
     * Gets the SNMP peer factory.
     *
     * @return the SNMP peer factory
     */
    public SnmpAgentConfigFactory getSnmpPeerFactory() {
        return m_snmpConfigDao;
    }

    /**
     * Sets the SNMP peer factory.
     *
     * @param snmpConfigDao the SNMP peer factory
     */
    public void setSnmpPeerFactory(final SnmpAgentConfigFactory snmpConfigDao) {
        this.m_snmpConfigDao = snmpConfigDao;
    }

    /**
     * Gets the hardware adapter configuration DAO.
     *
     * @return the hardware adapter configuration DAO
     */
    public SnmpHwInventoryAdapterConfigDao getHwAdapterConfigDao() {
        return m_hwInventoryAdapterConfigDao;
    }

    /**
     * Sets the hardware inventory adapter configuration DAO.
     *
     * @param hwInventoryAdapterConfigurationDao the hardware inventory adapter configuration DAO
     */
    public void setHwInventoryAdapterConfigDao(SnmpHwInventoryAdapterConfigDao hwInventoryAdapterConfigDao) {
        this.m_hwInventoryAdapterConfigDao = hwInventoryAdapterConfigDao;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.SimplerQueuedProvisioningAdapter#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Handle reload configuration event.
     *
     * @param event the event
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(final Event event) {
        if (isReloadConfigEventTarget(event)) {
            EventBuilder ebldr = null;
            LOG.debug("Reloading the Hardware Inventory adapter configuration");
            try {
                m_hwInventoryAdapterConfigDao.reload();
                initializeVendorAttributes();
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, PREFIX + NAME);
            } catch (Throwable e) {
                LOG.warn("Unable to reload Hardware Inventory adapter configuration", e);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
            }
            if (ebldr != null) {
                getEventForwarder().sendNow(ebldr.getEvent());
            }
        }
    }

    /**
     * Checks if is reload configuration event target.
     *
     * @param event the event
     * @return true, if checks if is reload configuration event target
     */
    private boolean isReloadConfigEventTarget(final Event event) {
        boolean isTarget = false;
        for (final Parm parm : event.getParmCollection()) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && (PREFIX + NAME).equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        LOG.debug("isReloadConfigEventTarget: Provisiond. {} was target of reload event: {}", NAME, isTarget);
        return isTarget;
    }

    /**
     * Gets the vendor attribute map.
     *
     * @return the vendor attribute map
     */
    protected Map<SnmpObjId, HwEntityAttributeType> getVendorAttributeMap() {
        return m_vendorAttributes;
    }
}
