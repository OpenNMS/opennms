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
package org.opennms.smoketest.minion;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.CommandTestUtils;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKafkaCompressionRpcIT {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractKafkaCompressionRpcIT.class);
    protected static final String LOCALHOST = "127.0.0.1";

    protected abstract OpenNMSStack stack();



    @Test
    public void verifyKafkaRpcWithTcpServiceDetection() {
        addRequisition(stack().opennms().getRestClient(), stack().minion().getLocation(), LOCALHOST);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS)
                .until(() -> detectTcpAtLocationMinion(stack()), containsString("'TCP' WAS detected on 127.0.0.1"));
    }

    static String detectTcpAtLocationMinion(OpenNMSStack stack) throws Exception {
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
        try (final SshClient sshClient = stack().opennms().ssh()) {
            // Perform JDBC service detection on Minion
            final PrintStream pipe = sshClient.openShell();
            pipe.println(String.format("detect -l %s JDBC 127.0.0.1 url=%s user=opennms password=opennms", stack().minion().getLocation(), jdbcUrl));
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