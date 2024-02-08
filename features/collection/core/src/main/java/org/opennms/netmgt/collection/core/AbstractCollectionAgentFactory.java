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

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class AbstractCollectionAgentFactory<T extends CollectionAgent> implements CollectionAgentFactory  {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private PlatformTransactionManager transMgr;

    protected abstract T createAgent(Integer ipInterfaceId, IpInterfaceDao ipInterfaceDao, PlatformTransactionManager transMgr, String location);

    @Override
    public T createCollectionAgentAndOverrideLocation(String nodeCriteria, InetAddress ipAddr, String location) {
        final OnmsNode node = nodeDao.get(nodeCriteria);
        if (node == null) {
            throw new IllegalArgumentException(String.format("No node found with lookup criteria: %s",
                    nodeCriteria));
        }
        final OnmsIpInterface ipInterface = ipInterfaceDao.findByNodeIdAndIpAddress(
                node.getId(), InetAddressUtils.str(ipAddr));
        if (ipInterface == null) {
            throw new IllegalArgumentException(String.format("No interface found with IP %s on node %s",
                    InetAddressUtils.str(ipAddr), nodeCriteria));
        }
        final T agent = createAgent(ipInterface.getId(), ipInterfaceDao, transMgr, location);
        return agent;
    }

    @Override
    public T createCollectionAgent(String nodeCriteria, InetAddress ipAddr) {
        return createCollectionAgentAndOverrideLocation(nodeCriteria, ipAddr, null);
    }

    @Override
    public T createCollectionAgent(OnmsIpInterface ipInterface) {
        final T agent = createAgent(ipInterface.getId(), ipInterfaceDao, transMgr, null);
        return agent;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        this.ipInterfaceDao = ipInterfaceDao;
    }

    public void setPlatformTransactionManager(PlatformTransactionManager transMgr) {
        this.transMgr = transMgr;
    }
}
