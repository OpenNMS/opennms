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
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

public class OpenNMSCassandraContainer extends org.testcontainers.containers.CassandraContainer<OpenNMSCassandraContainer> {

    public OpenNMSCassandraContainer() {
        // support 3 + 4, but always test against latest 3
        super(DockerImageName.parse("cassandra").withTag("3"));
        // Reduce JVM heap to 512m
        withEnv("JVM_OPTS", "-Xms512m -Xmx512m")
                .withNetwork(Network.SHARED)
                .withNetworkAliases(OpenNMSContainer.CASSANDRA_ALIAS)
                .withCreateContainerCmdModifier(TestContainerUtils::setGlobalMemAndCpuLimits);
    }
}
