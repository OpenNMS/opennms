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
