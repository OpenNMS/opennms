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
package org.opennms.smoketest.containers;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class PostgreSQLContainer extends org.testcontainers.containers.PostgreSQLContainer<PostgreSQLContainer> implements TestLifecycleAware {
    private LoadingCache<Integer, HibernateDaoFactory> daoFactoryCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, HibernateDaoFactory>() {
                        public HibernateDaoFactory load(Integer mappedPort) {
                            // Connect to the PostgreSQL in the container
                            final InetSocketAddress pgsql = new InetSocketAddress(getContainerIpAddress(), mappedPort);
                            return new HibernateDaoFactory(pgsql);
                        }
                    });

    private HibernateDaoFactory daoFactory;

    public PostgreSQLContainer() {
        super("postgres:10.7-alpine");
        withNetwork(Network.SHARED)
                .withNetworkAliases(OpenNMSContainer.DB_ALIAS)
                .withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);
    }

    @Override
    protected void configure() {
        super.configure();
        // Override command set by parent
        setCommand("postgres",
                // Disable fsync - speed > consistency for tests
                "-c", "fsync=off",
                // More memory
                "-c", "shared_buffers=256MB",
                // More connections
                "-c", "max_connections=200");
    }

    public HibernateDaoFactory getDaoFactory() {
        try {
            // Cache the DAO factory by the mapped port
            return daoFactoryCache.get(getMappedPort(POSTGRESQL_PORT));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends AbstractDaoHibernate<?, ?>> T dao(Class<T> clazz) {
        return getDaoFactory().getDao(clazz);
    }

    @Override
    public void afterTest(TestDescription description, Optional<Throwable> throwable) {
        // Ensure that the next test creates a new DAO factory, if it happens it re-use the same instance of this container
        daoFactoryCache.invalidateAll();
        retainLogsfNeeded(description.getFilesystemFriendlyName(), !throwable.isPresent());
    }

    private void retainLogsfNeeded(String prefix, boolean succeeded) {
        if (!succeeded) {
            logger().info("Gathering logs...");
            Path targetLogFolder = Paths.get("target", "logs", prefix, "postgresql");
            DevDebugUtils.clearLogs(targetLogFolder);
            DevDebugUtils.copyLogs(this,
                    // dest
                    targetLogFolder,
                    // source folder
                    Paths.get("/var", "lib", "postgresql", "data"),
                    // no log files to copy, everything is available via the container logs
                    Collections.emptyList());
        }
    }

}
