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
import org.testcontainers.utility.DockerImageName;

/**
 * Thanos Receive container that accepts Prometheus remote write requests.
 * Used as the write endpoint for the Cortex TSS plugin in integration tests.
 */
public class ThanosReceiveContainer extends GenericContainer<ThanosReceiveContainer> {

    public static final String ALIAS = "thanos-receive";
    public static final int REMOTE_WRITE_PORT = 19291;
    public static final int GRPC_PORT = 10901;
    public static final int HTTP_PORT = 10902;

    public ThanosReceiveContainer() {
        super(DockerImageName.parse("docker.io/thanosio/thanos:v0.35.1"));
        withCommand(
                "receive",
                "--tsdb.path=/data",
                "--grpc-address=0.0.0.0:" + GRPC_PORT,
                "--http-address=0.0.0.0:" + HTTP_PORT,
                "--remote-write.address=0.0.0.0:" + REMOTE_WRITE_PORT,
                "--tsdb.retention=30d",
                "--label=receive_replica=\"0\""
        );
        withExposedPorts(REMOTE_WRITE_PORT, GRPC_PORT, HTTP_PORT);
        withNetwork(Network.SHARED);
        withNetworkAliases(ALIAS);
        withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);
    }

    /**
     * @return the internal URL for Prometheus remote write (for use by containers on the shared network)
     */
    public String getInternalRemoteWriteUrl() {
        return String.format("http://%s:%d/api/v1/receive", ALIAS, REMOTE_WRITE_PORT);
    }
}
