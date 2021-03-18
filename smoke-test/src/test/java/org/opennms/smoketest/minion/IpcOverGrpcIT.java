/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2021 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;
import static org.opennms.smoketest.minion.RpcOverKafkaIT.addRequisition;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.StackModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(MinionTests.class)
public class IpcOverGrpcIT {

    private static final Logger LOG = LoggerFactory.getLogger(IpcOverGrpcIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withIpcStrategy(IpcStrategy.GRPC)
            .build());

    private static final String LOCALHOST = "127.0.0.1";

    @Test
    public void verifyGrpcRpcWithTcpServiceDetection() {
        // Add node and interface with minion location.
        addRequisition(stack.opennms().getRestClient(), stack.minion().getLocation(), LOCALHOST);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS)
                .until(() -> RpcOverKafkaIT.detectTcpAtLocationMinion(stack), containsString("'TCP' WAS detected on 127.0.0.1"));
    }

}
