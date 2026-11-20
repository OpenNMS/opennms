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

import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Single-node VictoriaMetrics, a Prometheus remote-write compatible time series store.
 */
public class VictoriaMetricsContainer extends GenericContainer<VictoriaMetricsContainer> {
    public static final String ALIAS = "victoriametrics";
    public static final int HTTP_PORT = 8428;
    public static final String IMAGE = "victoriametrics/victoria-metrics:v1.156.0";

    public VictoriaMetricsContainer() {
        super(IMAGE);
        withNetwork(Network.SHARED);
        withNetworkAliases(ALIAS);
        withExposedPorts(HTTP_PORT);
        withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);
        waitingFor(Wait.forHttp("/health").forPort(HTTP_PORT));
    }

    /**
     * Gets the base URL that the test host uses.
     */
    public String getBaseUrl() {
        return String.format("http://%s:%d", getHost(), getMappedPort(HTTP_PORT));
    }
}
