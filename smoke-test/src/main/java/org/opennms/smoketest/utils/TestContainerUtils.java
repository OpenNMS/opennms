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

package org.opennms.smoketest.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.InternetProtocol;
import com.google.common.io.CharStreams;

public class TestContainerUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestContainerUtils.class);

    private static final int NUM_CPUS_PER_CONTAINER = 2;

    /**
     * Set memory and CPU limits on the container in oder to help provide
     * more consistent timing across systems and runs.
     *
     * @param cmd reference to the create container command, obtain with withCreateContainerCmdModifier
     */
    public static void setGlobalMemAndCpuLimits(CreateContainerCmd cmd) {
        HostConfig hostConfig = cmd.getHostConfig();
        if (hostConfig == null) {
            hostConfig = new HostConfig();
        }
        hostConfig.withMemory(4 * 1024 * 1024 * 1024L); // Hard limit to 4GB of memory
        // cpu-period denotes the period in which container CPU utilisation is tracked
        hostConfig.withCpuPeriod(TimeUnit.MILLISECONDS.toMicros(100));
        // cpu-quota is the total amount of CPU time that a container can use in each cpu-period
        // say this is equal to the cpu-period set above, then the container can use 1 CPU at 100%
        // if this is 2x the cpu-period. then the container can use 2 CPUs at 100%, and so on...
        hostConfig.withCpuQuota(hostConfig.getCpuPeriod() * NUM_CPUS_PER_CONTAINER);
    }

    /**
     * Reach into the container, pull the contents of a file, and return it as a string.
     *
     * @param container container
     * @param pathInContainer path to the file in the container
     * @return the contents of the file as a string
     */
    public static String getFileFromContainerAsString(Container container, Path pathInContainer) {
        return (String)container.copyFileFromContainer(pathInContainer.toString(),
                is -> CharStreams.toString(new InputStreamReader((InputStream)is, StandardCharsets.UTF_8)));
    }

    /**
     * Retrieves the internal IP address of the container.
     *
     * @param container container
     * @return ip address
     */
    public static String getInternalIpAddress(Container container) {
        return container.getContainerInfo().getNetworkSettings().getNetworks()
                .entrySet().iterator().next().getValue().getIpAddress();
    }

    /**
     * Restarts the container.
     *
     * @param container container to restart
     */
    public static void restartContainer(Container container) {
        final DockerClient docker = container.getDockerClient();
        final String id = container.getContainerId();
        try {
            LOG.info("Restarting container: {} -> {}", container.getDockerImageName(), id);
            docker.restartContainerCmd(container.getContainerId()).exec();
            LOG.info("Container restarted: {} -> {}", container.getDockerImageName(), id);
        } catch (DockerException e) {
            LOG.warn("Unexpected exception while restarting container {}", id, e);
        }
    }

    /**
     * Workaround for UDP ports -- see https://github.com/testcontainers/testcontainers-java/issues/554
     *
     * @param cmd
     * @param ports
     */
    public static void exposePortsAsUdp(CreateContainerCmd cmd, int... ports) {
        final ExposedPort[] exposedPorts = cmd.getExposedPorts();
        if (exposedPorts == null && ports.length > 0) {
            throw new RuntimeException("There are 1+ ports to convert to UDP, but no exposed ports were found.");
        }

        // Index the ports for easy lookup
        final Map<Integer, Integer> portToIdx = new HashMap<>();
        for (int i = 0; i < exposedPorts.length; i++) {
            portToIdx.put(exposedPorts[i].getPort(), i);
        }

        for (int port : ports) {
            final Integer idx = portToIdx.get(port);
            if (idx == null) {
                throw new RuntimeException("No exposed port entry found for: " + port);
            }
            exposedPorts[idx] = new ExposedPort(port, InternetProtocol.UDP);
        }
    }

    /**
     * Workaround for UDP ports -- see https://github.com/testcontainers/testcontainers-java/issues/554
     *
     * @param container
     * @param port
     * @return
     */
    //
    public static int getMappedUdpPort(Container container, int port) {
        final String hostPortSpec = container.getContainerInfo().getNetworkSettings().getPorts()
                .getBindings().get(new ExposedPort(port, InternetProtocol.UDP))[0].getHostPortSpec();
        final int hostPort = Integer.parseInt(hostPortSpec);
        return hostPort;
    }

}
