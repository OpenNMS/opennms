/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.core;

import java.net.InetAddress;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.dto.CollectionAgentDTO;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.support.TransactionOperations;

public class OsgiCollectionAgentFactory implements CollectionAgentFactory {

    // This is probably not known when running in an OSGi container, but for now we leave it as is
    private static boolean storeByForeignSource =  Boolean.getBoolean("org.opennms.rrd.storeByForeignSource");

    private NodeDao nodeDao;

    private IpInterfaceDao ipInterfaceDao;

    private TransactionOperations transactionOperations;

    public OsgiCollectionAgentFactory(NodeDao nodeDao, IpInterfaceDao ipInterfaceDao, TransactionOperations transactionOperations) {
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.ipInterfaceDao = Objects.requireNonNull(ipInterfaceDao);
        this.transactionOperations = Objects.requireNonNull(transactionOperations);
    }

    @Override
    public CollectionAgent createCollectionAgent(OnmsIpInterface ipIf) {
        return transactionOperations.execute((status) -> {
            final OnmsNode node = ipIf.getNode();
            if (node.getLocation() != null) {
                return createAgent(ipIf, node, node.getLocation().getLocationName());
            }
            return createAgent(ipIf, node, null);
        });
    }

    @Override
    public CollectionAgent createCollectionAgent(String nodeCriteria, InetAddress ipAddr) {
        return transactionOperations.execute((status) -> {
            final OnmsNode node = lookupNode(nodeCriteria);
            final OnmsIpInterface ipInterface = lookupIpInterface(node, InetAddressUtils.str(ipAddr));
            final CollectionAgent agent = createAgent(ipInterface, node, node.getLocation() != null ? node.getLocation().getLocationName() : null);
            return agent;
        });
    }

    @Override
    public CollectionAgent createCollectionAgentAndOverrideLocation(String nodeCriteria, InetAddress ipAddr, String location) {
        return transactionOperations.execute((status) -> {
            final OnmsNode node = lookupNode(nodeCriteria);
            final OnmsIpInterface ipInterface = lookupIpInterface(node, InetAddressUtils.str(ipAddr));
            final CollectionAgent agent = createAgent(ipInterface, node, location);
            return agent;
        });
    }

    private CollectionAgent createAgent(OnmsIpInterface ipIf, OnmsNode node, String location) {
        final CollectionAgentDTO agent = new CollectionAgentDTO();
        agent.setAddress(ipIf.getIpAddress());
        agent.setForeignId(node.getForeignId());
        agent.setForeignSource(node.getForeignSource());
        agent.setNodeId(node.getId());
        agent.setNodeLabel(node.getLabel());
        agent.setLocationName(location);
        agent.setStorageResourcePath(DefaultCollectionAgentService.createStorageResourcePath(agent));
        agent.setStoreByForeignSource(storeByForeignSource);
        return agent;
    }

    private OnmsNode lookupNode(String nodeCriteria) {
        final OnmsNode node = nodeDao.get(nodeCriteria);
        if (node == null) {
            throw new IllegalArgumentException(String.format("No node found with lookup criteria: %s", nodeCriteria));
        }
        return node;
    }

    private OnmsIpInterface lookupIpInterface(OnmsNode node, String ipAddress) {
        final OnmsIpInterface ipInterface = ipInterfaceDao.findByNodeIdAndIpAddress(node.getId(), ipAddress);
        if (ipInterface == null) {
            throw new IllegalArgumentException(String.format("No interface found with IP %s on node %s", ipAddress, node.getId()));
        }
        return ipInterface;
    }
}
