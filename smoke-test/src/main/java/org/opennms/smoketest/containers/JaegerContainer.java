/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022-2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.SystemInfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.TestDescription;
import org.testcontainers.lifecycle.TestLifecycleAware;

public class JaegerContainer extends GenericContainer<JaegerContainer> implements TestLifecycleAware {
    public static final String ALIAS = "jaeger";
    public static final int WEB_PORT = 16686;
    public static final int THRIFT_HTTP_PORT = 14268;
    public static final String IMAGE = "jaegertracing/all-in-one:1.39";
    private static final Logger LOG = LoggerFactory.getLogger(JaegerContainer.class);

    public JaegerContainer() {
        super(IMAGE);
        withNetwork(Network.SHARED);
        withNetworkAliases(ALIAS);
        withExposedPorts(WEB_PORT);
    }

    public URL getURL(String path) throws MalformedURLException {
        Objects.requireNonNull(path);
        return new URL("http://" + getHost() + ":" + getMappedPort(WEB_PORT).toString() + path);
    }

    /**
     * Gets the Thrift HTTP URL.
     * @return String suitable to pass to JAEGER_ENDPOINT
     */
    public static String getThriftHttpURL() {
        return String.format("http://%s:%d/api/traces", ALIAS, THRIFT_HTTP_PORT);
    }

    @Override
    public void afterTest(final TestDescription description, final Optional<Throwable> throwable) {
        retainLogsIfNeeded(description.getFilesystemFriendlyName(), !throwable.isPresent());
    }

    private void retainLogsIfNeeded(String prefix, boolean failed) {
        // This can take a few seconds, so we only do it on failures
        if (failed) {
            try {
                Path opennms = Paths.get("target", "logs", prefix, ALIAS, "opennms-traces.json");
                FileUtils.copyURLToFile(getURL("/api/traces?service=" +
                        URLEncoder.encode(SystemInfoUtils.getInstanceId(), Charset.defaultCharset())),
                        opennms.toFile());
                LOG.info("OpenNMS Jaeger trace JSON: {}", opennms.toUri());
            } catch (Exception e) {
                System.err.println("Received exception while trying to save all Jaeger traces");
                e.printStackTrace(System.err);
            }
        }
    }
}
