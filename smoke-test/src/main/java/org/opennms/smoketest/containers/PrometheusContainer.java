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

import java.net.MalformedURLException;
import java.net.URL;

import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * Vanilla Prometheus container with remote write receiver enabled.
 * Provides the gold-standard Prometheus API for integration tests.
 *
 * <p>This container is not started by the default INTEGRATION stack (which uses
 * Thanos Receive + Query instead). It is available as a standalone building block
 * for custom test stacks that want a simpler single-node Prometheus backend.</p>
 */
public class PrometheusContainer extends GenericContainer<PrometheusContainer> {

    public static final String ALIAS = "prometheus";
    public static final int HTTP_PORT = 9090;

    public PrometheusContainer() {
        super(DockerImageName.parse("docker.io/prom/prometheus:v2.53.4"));
        withCommand(
                "--config.file=/etc/prometheus/prometheus.yml",
                "--storage.tsdb.path=/prometheus",
                "--storage.tsdb.retention.time=30d",
                "--web.enable-remote-write-receiver",
                "--web.listen-address=0.0.0.0:" + HTTP_PORT
        );
        withClasspathResourceMapping("prometheus.yml", "/etc/prometheus/prometheus.yml",
                BindMode.READ_ONLY);
        withExposedPorts(HTTP_PORT);
        withNetwork(Network.SHARED);
        withNetworkAliases(ALIAS);
        withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);
    }

    /**
     * @return the internal write URL for Prometheus remote write (for use by containers on the shared network)
     */
    public String getInternalWriteUrl() {
        return String.format("http://%s:%d/api/v1/write", ALIAS, HTTP_PORT);
    }

    /**
     * @return the internal read URL for the Prometheus query API (for use by containers on the shared network)
     */
    public String getInternalReadUrl() {
        return String.format("http://%s:%d/api/v1", ALIAS, HTTP_PORT);
    }

    /**
     * @return the external URL for the Prometheus query API (for use by the test host)
     */
    public URL getExternalQueryUrl() {
        try {
            return new URL(String.format("http://%s:%d", getContainerIpAddress(), getMappedPort(HTTP_PORT)));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
