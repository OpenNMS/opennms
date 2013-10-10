/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.upgrade.implementations;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.RrdLabelUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.service.snmp.IfTable;
import org.opennms.netmgt.provision.service.snmp.IfTableEntry;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.upgrade.api.OnmsUpgradeException;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The Class RRD/JRB Migrator for SNMP Interfaces Data (Offline Version)
 * 
 * <p>1.12 always add the MAC Address to the snmpinterface table if exist, which
 * is different from the 1.10 behavior. For this reason, some interfaces are going
 * to appear twice, and the data must be merged.</p>
 * 
 * <ul>
 * <li>NMS-6056</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class SnmpInterfaceRrdMigratorOffline extends AbstractSnmpInterfaceRrdMigrator {

    /**
     * Instantiates a new SNMP interface RRD migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public SnmpInterfaceRrdMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 2;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Merge SNMP Interface directories (Offline Version): NMS-6056";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    /**
     * Gets the interfaces to merge.
     *
     * @return the interfaces to merge
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected Map<File,File> getInterfacesToMerge() throws OnmsUpgradeException {
        final NodeDao nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        final IpInterfaceDao ipInterfaceDao = BeanUtils.getBean("daoContext", "ipInterfaceDao", IpInterfaceDao.class);
        final TransactionTemplate transactionTemplate = BeanUtils.getBean("daoContext", "transactionTemplate", TransactionTemplate.class);
        return transactionTemplate.execute(new TransactionCallback<Map<File,File>>() {
            @Override
            public  Map<File,File> doInTransaction(TransactionStatus status) {
                Map<File,File> interfacesToMerge = new TreeMap<File,File>();
                CriteriaBuilder b = new CriteriaBuilder(OnmsIpInterface.class);
                b.alias("monitoredServices", "service", JoinType.LEFT_JOIN);
                b.alias("service.serviceType", "serviceType", JoinType.LEFT_JOIN);
                b.eq("isSnmpPrimary", PrimaryType.PRIMARY);
                b.eq("serviceType.name", "SNMP");
                List<OnmsIpInterface> interfaces = ipInterfaceDao.findMatching(b.toCriteria());
                for (OnmsIpInterface ip : interfaces) {
                    OnmsNode node = ip.getNode();
                    IfTable ifTable = null;
                    try {
                        log("Retrieving IF-MIB::ifTable for node %s using IP %s\n", node.getLabel(), ip.getIpAddress().getHostAddress());
                        final SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ip.getIpAddress());
                        ifTable = new IfTable(ip.getIpAddress());
                        SnmpWalker walker = SnmpUtils.createWalker(agentConfig, "ifTable", ifTable);
                        walker.start();
                        walker.waitFor();
                    } catch (Exception e) {
                        log("Can't retrieve SNMP data from %s\n", ip.getIpAddress().getHostAddress());
                        continue;
                    }
                    if (ifTable != null) {
                        log("Updating the SNMP Interfaces for node %s\n", node.getLabel());
                        ifTable.updateSnmpInterfaceData(node);
                        nodeDao.update(node);
                        for (IfTableEntry entry : ifTable.getEntries()) {
                            OnmsSnmpInterface snmpIface = node.getSnmpInterfaceWithIfIndex(entry.getIfIndex());
                            String oldId = RrdLabelUtils.computeLabelForRRD(snmpIface.getIfName(), snmpIface.getIfDescr(), null);
                            String newId = RrdLabelUtils.computeLabelForRRD(snmpIface.getIfName(), snmpIface.getIfDescr(), snmpIface.getPhysAddr());
                            if (!oldId.equals(newId)) {
                                File nodeDir = getNodeDirectory(node);
                                File oldFile = new File(nodeDir, oldId);
                                File newFile = new File(nodeDir, newId);
                                interfacesToMerge.put(oldFile, newFile);
                            }
                        }
                    }
                }
                return interfacesToMerge;
            }
        });
    }

}
