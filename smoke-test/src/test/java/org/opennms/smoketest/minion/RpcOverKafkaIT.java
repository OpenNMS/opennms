/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.IpcStrategy;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.CommandTestUtils;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcOverKafkaIT {
    private static final Logger LOG = LoggerFactory.getLogger(RpcOverKafkaIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withIpcStrategy(IpcStrategy.KAFKA)
            .build());

    private static final String LOCALHOST = "127.0.0.1";

    @Test
    public void verifyKafkaRpcWithTcpServiceDetection() {
        // Add node and interface with minion location.
        addRequisition(stack.opennms().getRestClient(), stack.minion().getLocation(), LOCALHOST);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS)
                .until(this::detectTcpAtLocationMinion, containsString("'TCP' WAS detected on 127.0.0.1"));
    }

    private String detectTcpAtLocationMinion() throws Exception {
        try (final SshClient sshClient = new SshClient(stack.opennms().getSshAddress(), "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println(String.format("detect -l %s TCP 127.0.0.1 port=8201", stack.minion().getLocation()));
            pipe.println("logout");
            await().atMost(90, SECONDS).until(sshClient.isShellClosedCallable());
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "detect -l");
            LOG.info("Detect output: {}", shellOutput);
            return shellOutput;
        }
    }

    @Test
    public void verifyKafkaRpcWithJdbcServiceDetection() {
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(this::detectJdbcAtLocationMinion, containsString("'JDBC' WAS detected"));
    }

    private String detectJdbcAtLocationMinion() throws Exception {
        // Retrieve Postgres address and add form a URL
        String jdbcUrl = String.format("jdbc:postgresql://%s:5432/opennms", OpenNMSContainer.DB_ALIAS);
        try (final SshClient sshClient = stack.opennms().ssh()) {
            // Perform JDBC service detection on Minion
            final PrintStream pipe = sshClient.openShell();
            pipe.println(String.format("detect -l %s JDBC 127.0.0.1 url=%s user=opennms password=opennms", stack.minion().getLocation(), jdbcUrl));
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
            // Sanitize the output
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "detect -l");
            LOG.info("Detect output: {}", shellOutput);
            return shellOutput;
        }
    }

    public static void addRequisition(RestClient client, String location, String ipAddress) {
        Requisition requisition = new Requisition("foreignSource");
        List<RequisitionInterface> interfaces = new ArrayList<>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr(ipAddress);
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel(ipAddress);
        node.setLocation(location);
        node.setInterfaces(interfaces);
        node.setForeignId("foreignId");
        requisition.insertNode(node);

        client.addOrReplaceRequisition(requisition);
        client.importRequisition("foreignSource");
    }

}
