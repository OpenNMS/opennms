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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.opennms.smoketest.utils.OpenNMSRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.SelinuxContext;

public class OpenNMSContainer extends GenericContainer {
    public static final String DB_ALIAS = "db";
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSContainer.class);
    private static final int OPENNMS_WEB_PORT = 8980;
    private static final int OPENNMS_SSH_PORT = 8101;
    static final String ALIAS = "opennms";

    public OpenNMSContainer() {
        super("horizon");
        withExposedPorts(OPENNMS_WEB_PORT, OPENNMS_SSH_PORT)
                .withEnv("POSTGRES_HOST", DB_ALIAS)
                .withEnv("POSTGRES_PORT", Integer.toString(PostgreSQLContainer.POSTGRESQL_PORT))
                // User/pass are hardcoded in PostgreSQLContainer but are not exposed
                .withEnv("POSTGRES_USER", "test")
                .withEnv("POSTGRES_PASSWORD", "test")
                .withEnv("OPENNMS_DBNAME", "opennms")
                .withEnv("OPENNMS_DBUSER", "opennms")
                .withEnv("OPENNMS_DBPASS", "opennms")
                .withEnv("KARAF_FEATURES", "producer")
                .withClasspathResourceMapping("opennms-overlay", "/opt/opennms-overlay", BindMode.READ_ONLY, SelinuxContext.SINGLE)
                .withNetwork(Network.SHARED)
                .withNetworkAliases(ALIAS)
                .withCommand("-s")
                .waitingFor(new WaitForOpenNMS(this));
    }

    /**
     * @return the URL in a form consumable by containers networked with this one using the alias and internal port
     */
    public URL getBaseUrlInternal() {
        try {
            return new URL(String.format("http://%s:%d/", ALIAS, OPENNMS_WEB_PORT));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the URL in a form consumable by the host using the mapped port
     */
    public URL getBaseUrlExternal() {
        try {
            return new URL(String.format("http://%s:%d/", getContainerIpAddress(), getMappedPort(OPENNMS_WEB_PORT)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static class WaitForOpenNMS extends org.testcontainers.containers.wait.strategy.AbstractWaitStrategy {
        private final OpenNMSContainer openNMSContainer;

        public WaitForOpenNMS(OpenNMSContainer openNMSContainer) {
            this.openNMSContainer = Objects.requireNonNull(openNMSContainer);
        }

        @Override
        protected void waitUntilReady() {
            LOG.info("Waiting for OpenNMS...");
            final OpenNMSRestClient nmsRestClient;
            try {
                nmsRestClient = new OpenNMSRestClient(new URL(openNMSContainer.getBaseUrlExternal() + "opennms"));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            await().atMost(5, MINUTES)
                    .pollInterval(5, SECONDS).pollDelay(0, SECONDS)
                    .ignoreExceptions()
                    .until(nmsRestClient::getDisplayVersion, notNullValue());
            LOG.info("OpenNMS is ready");
        }
    }
}
