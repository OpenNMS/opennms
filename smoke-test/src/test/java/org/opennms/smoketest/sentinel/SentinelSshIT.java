/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.sentinel;


import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static org.opennms.smoketest.utils.KarafShellUtils.awaitHealthCheckSucceeded;


@Category(SentinelTests.class)
public class SentinelSshIT {

    private static final Logger LOG = LoggerFactory.getLogger(SentinelSshIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.SENTINEL;

    @Test
    public void testSshHealthOnSentinel(){
        //Test for no exception to occur
        LOG.info("Waiting for Sentinel ssh health check...");
        final InetSocketAddress karafSsh = stack.sentinel().getSshAddress();
        awaitHealthCheckSucceeded(karafSsh, 3, "Sentinel");
    }
}
