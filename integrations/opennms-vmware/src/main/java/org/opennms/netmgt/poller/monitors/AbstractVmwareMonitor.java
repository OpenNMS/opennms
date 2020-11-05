/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.VmwareConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.opennms.netmgt.provision.service.vmware.VmwareImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AbstractVmwareMonitor extends AbstractServiceMonitor {
    private final Logger logger = LoggerFactory.getLogger(AbstractVmwareMonitor.class);

    /**
     * the node dao object for retrieving assets
     */
    private NodeDao m_nodeDao = null;

    private TransactionTemplate m_transactionTemplate = null;

    /**
     * the config dao
     */
    private VmwareConfigDao m_vmwareConfigDao = null;

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        if (m_nodeDao == null) {
            m_nodeDao = BeanUtils.getBean("daoContext", "nodeDao", NodeDao.class);
        }

        if (m_vmwareConfigDao == null) {
            m_vmwareConfigDao = BeanUtils.getBean("daoContext", "vmwareConfigDao", VmwareConfigDao.class);
        }

        if (m_transactionTemplate == null) {
            m_transactionTemplate = BeanUtils.getBean("daoContext", "transactionTemplate", TransactionTemplate.class);
        }

        final Map<String, Object> runtimeAttributes = new HashMap<>();

        m_transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {
                final OnmsNode onmsNode = m_nodeDao.get(svc.getNodeId());
                if (onmsNode == null) {
                    throw new IllegalArgumentException("No node found with ID: " + svc.getNodeId());
                }

                // retrieve the assets

                final String vmwareManagementServer = VmwareImporter.getManagementServer(onmsNode);
                final String vmwareManagedEntityType = VmwareImporter.getManagedEntityType(onmsNode);

                final String vmwareManagedObjectId = onmsNode.getForeignId();

                String vmwareMangementServerUsername = null;
                String vmwareMangementServerPassword = null;
                final Map<String, VmwareServer> serverMap = m_vmwareConfigDao.getServerMap();
                if (serverMap == null) {
                    logger.error("Error getting vmware-config.xml's server map.");
                } else {
                    final VmwareServer vmwareServer = serverMap.get(vmwareManagementServer);
                    if (vmwareServer == null) {
                        logger.error("Error getting credentials for VMware management server '{}'.", vmwareManagementServer);
                    } else {
                        vmwareMangementServerUsername = vmwareServer.getUsername();
                        vmwareMangementServerPassword = vmwareServer.getPassword();
                    }
                }

                runtimeAttributes.put(VmwareImporter.METADATA_MANAGEMENT_SERVER, vmwareManagementServer);
                runtimeAttributes.put(VmwareImporter.METADATA_MANAGED_ENTITY_TYPE, vmwareManagedEntityType);
                runtimeAttributes.put(VmwareImporter.METADATA_MANAGED_OBJECT_ID, vmwareManagedObjectId);
                runtimeAttributes.put(VmwareImporter.VMWARE_MANAGEMENT_SERVER_USERNAME_KEY, vmwareMangementServerUsername);
                runtimeAttributes.put(VmwareImporter.VMWARE_MANAGEMENT_SERVER_PASSWORD_KEY, vmwareMangementServerPassword);

                return null;
            }
        });

        return runtimeAttributes;
    }
}
