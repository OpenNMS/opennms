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
package org.opennms.smoketest.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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
import com.github.dockerjava.api.model.Ports;
import com.google.common.io.CharStreams;

public class TestContainerUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestContainerUtils.class);

    // Constants used when setting UID/GID behavior to match OpenShift
    public static final int OPENSHIFT_CONTAINER_UID_RANGE_MIN = 1000600000;
    public static final int OPENSHIFT_CONTAINER_UID_RANGE_MAX = 1000700000;
    public static final int OPENSHIFT_CONTAINER_GID = 0;

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
        // Add already exposed TCP ports
        List<ExposedPort> exposedPorts = new ArrayList<>();
        for (ExposedPort p : cmd.getExposedPorts()) {
            exposedPorts.add(p);
        }
        // Add our UDP portts
        for (int port : ports) {
            exposedPorts.add(ExposedPort.udp(port));
        }
        cmd.withExposedPorts(exposedPorts);

        // Add previous port bindings
        final var hostConfig = cmd.getHostConfig();
        if (hostConfig != null) {
            Ports portBindings = hostConfig.getPortBindings();
            // Add port bindings for our UDP ports
            for (int port : ports) {
                portBindings.bind(ExposedPort.udp(port), Ports.Binding.empty());
            }
            hostConfig.withPortBindings(portBindings);
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
        final ExposedPort searchForPort = new ExposedPort(port, InternetProtocol.UDP);
        final Ports.Binding[] bindings = container.getContainerInfo()
                .getNetworkSettings()
                .getPorts()
                .getBindings()
                .get(searchForPort);
        if (bindings == null || bindings.length == 0) {
            throw new RuntimeException("No exposed port bindings found for " + searchForPort);
        }
        return Integer.parseInt(bindings[0].getHostPortSpec());
    }

}
