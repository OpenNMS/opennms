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
