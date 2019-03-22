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

package org.opennms.smoketest;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.SentinelContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.smoketest.containers.PostgreSQLContainer;

/**
 * Verify that the Sentinel container starts up.
 */
public class SentinelStartupIT {
    private final Logger LOG = LoggerFactory.getLogger(SentinelStartupIT.class);

    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static final OpenNMSContainer opennmsContainer = new OpenNMSContainer();

    private static final SentinelContainer sentinelContainer = new SentinelContainer();

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(postgreSQLContainer)
            .around(opennmsContainer)
            .around(sentinelContainer);

    @Test
    public void canStartSentinel() {
        // Containers started and health check passed, we're all set
    }

}
