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

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.SelinuxContext;

import com.github.dockerjava.api.exception.NotFoundException;
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
    public static final String CONTAINER_STDOUT_STDERR = "container_stdout_stderr";

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

    public static void clearLogs(Path targetLogFolder) {
        // We don't want to intermix old and new log files.
        if (Files.exists(targetLogFolder)) {
            FileSystemUtils.deleteRecursively(targetLogFolder.toFile());
        }
    }

    /**
     * Gather a thread dump on the JVM process at PID 1 in the container.
     *
     * @param container       Container to gather a thread dump from.
     * @param targetLogFolder if set, store the thread dump in a "threadDump.log" in this directory.
     * @param outputLog       if set, this is the log file in the container where we expect to see the thread dump.
     *                        If null, will use getLogs().
     * @return path to thread dump file if one was stored, null otherwise.
     */
    public static Path gatherThreadDump(Container container, Path targetLogFolder, Path outputLog) {
        LOG.info("Gathering thread dump...");

        if (!container.isRunning()) {
            LOG.warn("gatherThreadDump can only be used on a running container. Container [{}] is not running",
                    container.getDockerImageName());
            return null;
        }

        LOG.info("send SIGQUIT to process in container");
        try {
            LIMITER.callWithTimeout(() -> {
                container.execInContainer("kill", "-3", "1");
                return null;
            }, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            LOG.warn("Sending SIGQUIT to JVM in container failed. Thread dump may not be available.", e);
        }

        final Callable<String> threadDumpCallable;
        if (outputLog != null) {
            threadDumpCallable = () -> TestContainerUtils.getFileFromContainerAsString(container, outputLog);
        } else {
            threadDumpCallable = container::getLogs;
        }

        try {
            await("waiting for thread dump to complete")
                    .atMost(Duration.ofSeconds(5))
                    .failFast("container is no longer running", () -> !container.isRunning())
                    .ignoreException(NotFoundException.class)
                    .until(threadDumpCallable, containsString("JNI global refs")); // shows up near the end
        } catch (Exception e) {
            LOG.warn("Did not see thread dump in container {} within timeout",
                    outputLog != null ? outputLog : "console logs",
                    e);
        }

        if (targetLogFolder == null) {
            return null;
        }

        try {
            Files.createDirectories(targetLogFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create " + targetLogFolder, e);
        }

        var targetFile = targetLogFolder.resolve("threadDump.log");
        try {
            // Example:
            //
            //     2022-11-13 16:46:30
            //     Full thread dump OpenJDK 64-Bit Server VM (11.0.16+8-post-Ubuntu-0ubuntu122.04 mixed mode):
            //     ...
            //
            //     Heap
            //      garbage-first heap   total 2097152K, used 474813K [0x0000000080000000, 0x0000000100000000)
            //       region size 1024K, 111 young (113664K), 20 survivors (20480K)
            //      Metaspace       used 303094K, capacity 321939K, committed 322160K, reserved 1335296K
            //       class space    used 31196K, capacity 35846K, committed 35916K, reserved 1048576K
            //
            // A few tricky things:
            // - We optionally include the time stamp on the line before "Full thread dump".
            // - We end our match once we see an empty line after "Heap".
            var threadDump = threadDumpCallable.call().replaceFirst(
                    "(?ms).*?((^[^\r\n]*$[\r\n]+)?^Full thread dump .*^Heap$.*?^$).*?",
                    "$1"
            );
            try (Writer fileWriter = new OutputStreamWriter(
                    new FileOutputStream(targetFile.toFile()), StandardCharsets.UTF_8)) {
                fileWriter.write(
                        "# IntelliJ IDEA users: might I suggest Code | Analyze Stack Trace or Thread Dump.\n"
                                + "# See: https://www.jetbrains.com/help/idea/analyzing-external-stacktraces.html\n"
                                + "\n");
                fileWriter.write(threadDump);
            }
            return targetFile;
        } catch (Exception e) {
            LOG.warn("Could not retrieve or store thread dump in file {}", targetFile, e);
            return null;
        }
    }

    public static void copyLogs(Container container, Path targetLogFolder, Path sourceLogFolder, List<String> logFiles) {
        try {
            Files.createDirectories(targetLogFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create " + targetLogFolder, e);
        }

        final Path containerLogOutputFile = targetLogFolder.resolve(CONTAINER_STDOUT_STDERR);
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

        var missingLogs = new TreeSet<String>();
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
                if (ExceptionUtils.getRootCause(e).getClass() == NotFoundException.class) {
                    missingLogs.add(logFile);
                } else {
                    LOG.warn("Failed to copy log file {} from container: {}", logFile, e.getMessage());
                }
            }
        }
        if (!missingLogs.isEmpty()) {
            LOG.warn("Failed to copy log files from the container because container does not have files: {}", missingLogs);
        }
    }

}
