/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.coordination.zookeeper;

import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.opennms.features.distributed.coordination.api.DomainManager;
import org.opennms.features.distributed.coordination.api.DomainManagerFactory;
import org.opennms.features.distributed.coordination.common.AbstractDomainManagerFactory;

/**
 * A {@link DomainManagerFactory} that uses Apache ZooKeeper to manage all domains.
 */
public class ZookeeperDomainManagerFactory extends AbstractDomainManagerFactory {
    /**
     * The connection string to connect to ZooKeeper with.
     */
    private final String connectString;

    /**
     * The namespace to prefix all keys with.
     */
    private final String namespace;

    /**
     * Constructor.
     *
     * @param connectString the connection string for Zookeeper
     * @param namespace     the namespace to prefix the domain with
     */
    public ZookeeperDomainManagerFactory(String connectString, String namespace) {
        this.connectString = connectString;
        this.namespace = namespace;
    }

    @Override
    protected DomainManager createManagerForDomain(String domain) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .namespace(namespace)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3));

        return ZookeeperDomainManager.of(domain, builder);
    }

    @Override
    public String toString() {
        return "ZookeeperDomainManagerFactory{} " + super.toString();
    }
}
