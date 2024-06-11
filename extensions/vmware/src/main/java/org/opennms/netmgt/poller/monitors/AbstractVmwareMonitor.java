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
package org.opennms.netmgt.poller.monitors;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;
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

                final Map<String, VmwareServer> serverMap = m_vmwareConfigDao.getServerMap();
                if (serverMap == null) {
                    logger.error("Error getting vmware-config.xml's server map.");
                } else {
                    final VmwareServer vmwareServer = serverMap.get(vmwareManagementServer);
                    if (vmwareServer == null) {
                        logger.error("Error getting credentials for VMware management server '{}'.", vmwareManagementServer);
                    } else {
                        runtimeAttributes.put(VmwareImporter.VMWARE_MANAGEMENT_SERVER_USERNAME_KEY, Interpolator.pleaseInterpolate(vmwareServer.getUsername()));
                        runtimeAttributes.put(VmwareImporter.VMWARE_MANAGEMENT_SERVER_PASSWORD_KEY, Interpolator.pleaseInterpolate(vmwareServer.getPassword()));
                    }
                }

                runtimeAttributes.put(VmwareImporter.METADATA_MANAGEMENT_SERVER, vmwareManagementServer);
                runtimeAttributes.put(VmwareImporter.METADATA_MANAGED_ENTITY_TYPE, vmwareManagedEntityType);
                runtimeAttributes.put(VmwareImporter.METADATA_MANAGED_OBJECT_ID, vmwareManagedObjectId);

                return null;
            }
        });

        return runtimeAttributes;
    }
}
