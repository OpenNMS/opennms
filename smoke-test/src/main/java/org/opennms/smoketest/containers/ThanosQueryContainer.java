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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * Thanos Query container that provides a Prometheus-compatible query API.
 * Used as the read endpoint for the Cortex TSS plugin in integration tests.
 */
public class ThanosQueryContainer extends GenericContainer<ThanosQueryContainer> {

    public static final String ALIAS = "thanos-query";
    public static final int HTTP_PORT = 9090;
    public static final int GRPC_PORT = 10903;

    public ThanosQueryContainer() {
        super(DockerImageName.parse("docker.io/thanosio/thanos:v0.35.1"));
        withCommand(
                "query",
                "--http-address=0.0.0.0:" + HTTP_PORT,
                "--grpc-address=0.0.0.0:" + GRPC_PORT,
                "--store=" + ThanosReceiveContainer.ALIAS + ":" + ThanosReceiveContainer.GRPC_PORT
        );
        withExposedPorts(HTTP_PORT, GRPC_PORT);
        withNetwork(Network.SHARED);
        withNetworkAliases(ALIAS);
        withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);
    }

    /**
     * @return the internal URL for the Prometheus query API (for use by containers on the shared network)
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
