/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.SelinuxContext;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.common.util.concurrent.TimeLimiter;

/**
 * Utility functions for developing and debugging our containers.
 *
 * @author jwhite
 */
public class DevDebugUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DevDebugUtils.class);

    public static final String M2_DEV_SYS_PROP = "org.opennms.dev.m2";
    public static final String CONTAINER_HOST_M2_SYS_PROP = "org.opennms.dev.container.host";

    private static final TimeLimiter LIMITER = SimpleTimeLimiter.create(Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("dev-debug-utils-pool-%d")
            .build()));

    public static String convertToContainerAccessibleUrl(String url, String defaultAlias, int defaultPort) {
        final URI uri;
        try {
            uri = new URI(url);

            final String containerHost = System.getProperty(CONTAINER_HOST_M2_SYS_PROP);
            final URI effectiveUri;
            if (Strings.isNullOrEmpty(containerHost)) {
                // None set, replace hostname and port with defaults given
                effectiveUri = new URI(uri.getScheme(), defaultAlias + ":" + defaultPort, uri.getPath(), uri.getQuery(), uri.getFragment());
            } else {
                // Keep the current port, but replace the host with the given value
                effectiveUri = new URI(uri.getScheme(), containerHost + ":" + uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
            }

            return effectiveUri.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Bind the local repository derived from the 'org.opennms.dev.m2' system property
     * to the specified path in the container.
     *
     * The path should typically be the value of the 'org.ops4j.pax.url.mvn.localRepository'
     * property in ${karaf.home}/etc/org.ops4j.pax.url.mvn.cfg.
     *
     * @param containerPath target path in the container
     */
    public static void setupMavenRepoBind(Container container, String containerPath) {
        final String m2dev = System.getProperty(M2_DEV_SYS_PROP);
        if (!Strings.isNullOrEmpty(m2dev)) {
            container.addFileSystemBind(m2dev, containerPath, BindMode.READ_WRITE, SelinuxContext.SINGLE);
        }
    }

    public static void triggerThreadDump(Container container) {
        try {
            LIMITER.callWithTimeout(() -> {
                LOG.info("kill -3 -1");
                container.execInContainer("kill", "-3", "1");
                LOG.info("Sleeping for 5 seconds to give the JVM a chance to respond...");
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                LOG.info("Thread dump should be ready.");
                return null;
            }, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            LOG.warn("Sending SIGQUIT to JVM in container failed. Thread dump may not be available.", e);
        }
    }

    public static void copyLogs(Container container, Path targetLogFolder, Path sourceLogFolder, List<String> logFiles) {
        try {
            Files.createDirectories(targetLogFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create " + targetLogFolder, e);
        }

        final Path containerLogOutputFile = targetLogFolder.resolve("container_stdout_stderr");
        try {
            LIMITER.runWithTimeout(() -> {
                try {
                    Files.write(containerLogOutputFile, container.getLogs().getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, 1, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            LOG.warn("Timeout when copying stdout/stderr from container to file {}.", containerLogOutputFile);
            // Don't attempt to copy any further files
            return;
        } catch (Exception e) {
            LOG.info("Failed to copy stdout/stderr from container to file {}: {}", containerLogOutputFile, e.getMessage());
        }

        for (String logFile : logFiles) {
            try {
                LIMITER.runWithTimeout(() -> {
                    try {
                        container.copyFileFromContainer(sourceLogFolder.resolve(logFile).toString(),
                                targetLogFolder.resolve(logFile).toString());
                    } catch (IOException|InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, 1, TimeUnit.MINUTES);
            } catch (TimeoutException e) {
                LOG.warn("Timeout when copying log file {} from container: {}", logFile, e.getMessage());
                // Don't attempt to copy any further files
                return;
            } catch (Exception e) {
                LOG.info("Failed to copy log file {} from container: {}", logFile, e.getMessage());
            }
        }
    }

}
