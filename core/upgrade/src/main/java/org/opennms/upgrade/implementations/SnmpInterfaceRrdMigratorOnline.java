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

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.RrdLabelUtils;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.upgrade.api.OnmsUpgradeException;

import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The Class RRD/JRB Migrator for SNMP Interfaces Data (Online Version)
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
public class SnmpInterfaceRrdMigratorOnline extends AbstractSnmpInterfaceRrdMigrator {

    /**
     * Instantiates a new SNMP interface RRD migrator online.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public SnmpInterfaceRrdMigratorOnline() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 3;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Merge SNMP Interface directories (Online Version): NMS-6056";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return true;
    }

    /**
     * Gets the interfaces to merge.
     *
     * @return the interfaces to merge
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected Map<File,File> getInterfacesToMerge() throws OnmsUpgradeException {
        final SnmpInterfaceDao snmpInterfaceDao = BeanUtils.getBean("daoContext", "snmpInterfaceDao", SnmpInterfaceDao.class);
        final TransactionTemplate transactionTemplate = BeanUtils.getBean("daoContext", "transactionTemplate", TransactionTemplate.class);
        return transactionTemplate.execute(new TransactionCallback<Map<File,File>>() {
            @Override
            public  Map<File,File> doInTransaction(TransactionStatus status) {
                Map<File,File> interfacesToMerge = new TreeMap<File,File>();
                CriteriaBuilder b = new CriteriaBuilder(OnmsSnmpInterface.class);
                b.isNotNull("physAddr");
                List<OnmsSnmpInterface> interfaces = snmpInterfaceDao.findMatching(b.toCriteria());
                for (OnmsSnmpInterface snmpIface : interfaces) {
                    OnmsNode node = snmpIface.getNode();
                    String oldId = RrdLabelUtils.computeLabelForRRD(snmpIface.getIfName(), snmpIface.getIfDescr(), null);
                    String newId = RrdLabelUtils.computeLabelForRRD(snmpIface.getIfName(), snmpIface.getIfDescr(), snmpIface.getPhysAddr());
                    if (!oldId.equals(newId)) {
                        File nodeDir = getNodeDirectory(node);
                        File oldFile = new File(nodeDir, oldId);
                        File newFile = new File(nodeDir, newId);
                        interfacesToMerge.put(oldFile, newFile);
                    }
                }
                return interfacesToMerge;
            }
        });
    }

}
