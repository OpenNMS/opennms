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
