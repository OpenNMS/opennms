/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.health;

import java.net.InetSocketAddress;

import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

public class SentinelHealthCheckIT extends AbstractHealthCheckIT {

    @Override
    protected void applyTestEnvironment(TestEnvironmentBuilder builder) {
        builder.opennms()
                .minion()
                .es6()
                .sentinel();

        // Install some features to have health:check process something,
        // as by default sentinel does not start any bundles
        builder.withSentinelEnvironment()
                .addFile(getClass().getResource("/sentinel/features-jms.xml"), "deploy/features.xml");
    }

    @Override
    public InetSocketAddress getSshAddress() {
        return testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);
    }

    @Override
    public int getExpectedHealthCheckServices() {
        return 5;
    }
}
