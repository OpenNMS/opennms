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

package org.opennms.netmgt.provision.plugin;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.opennms.netmgt.config.hardware.HwInventoryAdapterConfiguration;
import org.opennms.netmgt.model.HwEntityAttributeType;
import org.opennms.netmgt.model.OnmsHwEntity;
import org.opennms.netmgt.provision.snmp.EntityPhysicalTableRow;
import org.opennms.netmgt.provision.snmp.EntityPhysicalTableTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpEntityPlugin implements EntityPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpEntityPlugin.class);

    private SnmpAgentConfig agentConfig;
    private String nodeSysOid;
    private Map<SnmpObjId, HwEntityAttributeType> vendorAttributes = new HashMap<SnmpObjId, HwEntityAttributeType>();
    private HwInventoryAdapterConfiguration adapterConfiguration;

    public SnmpEntityPlugin(HwInventoryAdapterConfiguration adapterConfiguration, Map<SnmpObjId, HwEntityAttributeType> vendorAttributes, SnmpAgentConfig agentConfig, String nodeSysOid) {
        this.adapterConfiguration = adapterConfiguration;
        this.vendorAttributes = vendorAttributes;
        this.agentConfig = agentConfig;
        this.nodeSysOid = nodeSysOid;
    }

    @Override
    public OnmsHwEntity getRootEntity(int nodeId, InetAddress ipAddress) throws EntityPluginException {
        LOG.debug("getRootEntity: Getting ENTITY-MIB using {}", agentConfig);

        final List<SnmpObjId> vendorOidList = adapterConfiguration.getVendorOid(nodeSysOid);
        final SnmpObjId[] vendorOids = vendorOidList.toArray(new SnmpObjId[vendorOidList.size()]);
        final SnmpObjId[] allOids = (SnmpObjId[]) ArrayUtils.addAll(EntityPhysicalTableRow.ELEMENTS, vendorOids);
        final EntityPhysicalTableTracker tracker = new EntityPhysicalTableTracker(vendorAttributes, allOids);
        final String trackerName = tracker.getClass().getSimpleName() + '_' + nodeId;

        final SnmpWalker walker = SnmpUtils.createWalker(agentConfig, trackerName, tracker);
        walker.start();
        try {
            walker.waitFor();
            if (walker.timedOut()) {
                throw new EntityPluginException("Aborting entities scan: Agent timed out while scanning the " + trackerName + " table");
            }  else if (walker.failed()) {
                throw new EntityPluginException("Aborting entities scan: Agent failed while scanning the " + trackerName + " table: " + walker.getErrorMessage());
            }
        } catch (final InterruptedException e) {
            throw new EntityPluginException("ENTITY-MIB node collection interrupted, exiting");
        }

        OnmsHwEntity root = tracker.getRootEntity();
        if (root == null) {
            throw new EntityPluginException("Cannot get root entity for nodeId " + nodeId);
        }

        return root;
    }

}
