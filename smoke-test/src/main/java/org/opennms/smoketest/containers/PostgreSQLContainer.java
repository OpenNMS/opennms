/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.containers;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.smoketest.utils.DevDebugUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class PostgreSQLContainer extends org.testcontainers.containers.PostgreSQLContainer implements TestLifecycleAware {
    private static final Logger LOG = LoggerFactory.getLogger(PostgreSQLContainer.class);

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
                .withCreateContainerCmdModifier(cmd -> {
                    final CreateContainerCmd createCmd = (CreateContainerCmd)cmd;
                    TestContainerUtils.setGlobalMemAndCpuLimits(createCmd);
                });
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
            LOG.info("Gathering logs...");
            copyLogs(this, prefix);
        }
    }

    private static void copyLogs(PostgreSQLContainer container, String prefix) {
        DevDebugUtils.copyLogs(container,
                // dest
                Paths.get("target", "logs", prefix, "postgresql"),
                // source folder
                Paths.get("/var", "lib", "postgresql", "data"),
                // no log files to copy, everything is available via the container logs
                Collections.emptyList());
    }

}
