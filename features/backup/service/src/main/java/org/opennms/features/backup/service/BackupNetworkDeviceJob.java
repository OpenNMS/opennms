/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.backup.service;

import java.util.List;

import org.opennms.core.backup.client.LocationAwareBackupClientImpl;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.backup.LocationAwareBackupClient;
import org.opennms.features.backup.service.api.NetworkDeviceBackupManager;
import org.opennms.netmgt.dao.api.MonitoringLocationUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackupNetworkDeviceJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(BackupNetworkDeviceJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LOG.debug("Start execute BackupNetworkDeviceJob!");
        LocationAwareBackupClientImpl backupRpcClient = (LocationAwareBackupClientImpl) jobExecutionContext.getMergedJobDataMap().get("backupRpcClient");
        NodeDao nodeDao = (NodeDao) jobExecutionContext.getMergedJobDataMap().get("nodeDao");
        NetworkDeviceBackupManager networkDeviceBackupManager = (NetworkDeviceBackupManager)
                jobExecutionContext.getMergedJobDataMap().get("networkDeviceBackupManager");

        List<OnmsNode> nodes = nodeDao.findAll();
        LOG.debug("Nodes size = " + nodes.size());

        handleBackup(nodes, backupRpcClient, networkDeviceBackupManager);
    }

    private void handleBackup(List<OnmsNode> nodes, LocationAwareBackupClientImpl backupRpcClient,
                              NetworkDeviceBackupManager networkDeviceBackupManager) {
        nodes.forEach(node -> {
            LOG.info("Working on device: " + node.getLabel());
            LOG.debug(node.getAssetRecord().getUsername() + " " + node.getAssetRecord().getPassword());

            if (node.getAssetRecord().getUsername() == null || node.getAssetRecord().getPassword() == null) {
                LOG.warn("SKIP node[{}] with empty username and password.", node.getNodeId());
                return;
            }

            if (node.getPrimaryInterface() == null) {
                LOG.warn("SKIP node[{}] without primary interface.", node.getNodeId());
                return;
            }

            final LocationAwareBackupClient client = networkDeviceBackupManager.getClient();
            client.backup()
                    .withLocation(MonitoringLocationUtils.getLocationNameOrNullIfDefault(node))
                    .withHost(InetAddressUtils.str(node.getPrimaryInterface().getIpAddress()))
                    .withAttribute("username", node.getAssetRecord().getUsername())
                    .withAttribute("password", node.getAssetRecord().getPassword())
                    .execute()
                    .whenComplete((config, ex) -> {
                        if (ex == null) {
                            LOG.debug("Received config: {} for node[{}].", config, node.getNodeId());
                            try {
                                networkDeviceBackupManager.saveConfig(node.getId(), config);
                            } catch (Exception e) {
                                LOG.error("Failed to save config: {} for node[{}].", config, node.getNodeId());
                            }
                        } else {
                            LOG.error("Failed to retrieve config for node[{}].", node.getNodeId(), ex);
                        }
                    });
        });
    }

}
