/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2023 The OpenNMS Group, Inc.
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
