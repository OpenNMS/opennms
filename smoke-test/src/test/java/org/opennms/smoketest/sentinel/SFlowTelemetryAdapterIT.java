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

package org.opennms.smoketest.sentinel;

import static com.jayway.awaitility.Awaitility.await;
import static io.restassured.RestAssured.preemptive;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.opennms.smoketest.OpenNMSSeleniumTestCase.BASIC_AUTH_PASSWORD;
import static org.opennms.smoketest.OpenNMSSeleniumTestCase.BASIC_AUTH_USERNAME;
import static org.opennms.smoketest.flow.FlowStackIT.sendNetflowPacket;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.flow.FlowStackIT;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * Verifies that sflow packets containing samples are also persisted if set up correctly
 */
public class SFlowTelemetryAdapterIT {

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder()
                    .opennms()
                    .minion()
                    .es6()
                    .newts()
                    .sentinel();

            // Enable Netflow 5 Adapter
            builder.withSentinelEnvironment()
                    .addFile(getClass().getResource("/sentinel/features-newts-jms.xml"), "deploy/features.xml")

                    .addFile(getClass().getResource("/sentinel/sflow-host.groovy"), "etc/sflow-host.groovy");

            // Enable Flow-Listeners
            builder.withMinionEnvironment()
                    .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50003.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-sflow.cfg")
            ;

            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            return builder.build();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
    public void verifySflow() throws Exception {
        // Determine endpoints
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);
        final InetSocketAddress minionSflowListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, FlowStackIT.SFLOW_LISTENER_UDP_PORT, "udp");
        final InetSocketAddress opennmsHttpAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.OPENNMS, 8980);
        final InetSocketAddress postgresqlAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.POSTGRES, 5432);

        // Configure RestAssured
        RestAssured.baseURI = String.format("http://%s:%s/opennms", opennmsHttpAddress.getHostName(), opennmsHttpAddress.getPort());
        RestAssured.port = opennmsHttpAddress.getPort();
        RestAssured.basePath = "/rest";
        RestAssured.authentication = preemptive().basic(BASIC_AUTH_USERNAME, BASIC_AUTH_PASSWORD);

        // The SFlow packet contains a node, which must be created in order to have the adapter store it to newts
        // so a requisition is created first
        createRequisition(opennmsHttpAddress, postgresqlAddress);

        // Wait until a route for SFlow procession is actually started
        waitForSentinelStartup(sentinelSshAddress);

        // Now sentinel is up and running, we should re-sync the datasource,as the
        // earlier created node may not be visible to sentinel yet.
        syncDataSource(sentinelSshAddress);

        // Ensure no measurement data available
        final Response response = RestAssured.given().accept(ContentType.JSON)
                .get("/measurements/node[telemetry-sflow:dummy-node].nodeSnmp[]/load_avg_5min");
        Assert.assertEquals(404, response.statusCode());

        // Send flow packet to minion
        sendNetflowPacket(minionSflowListenerAddress, "/flows/sflow2.dat"); // ? record

        await().atMost(3, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS).until(
                () -> {
                    final Response theResponse = RestAssured.given().accept(ContentType.JSON)
                            .get("/measurements/node[telemetry-sflow:dummy-node].nodeSnmp[]/load_avg_5min");
                    return theResponse.statusCode() == 200;
                }
        );
    }

    private void syncDataSource(InetSocketAddress sentinelSshAddress) {
        // Sync DataSoucre
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sentinelSshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("nodecache:sync");
                        pipe.println("log:display");
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        logger.info("log:display");
                        logger.info("{}", shellOutput);
                        return true;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify sentinel startup: {}", ex.getMessage());
                        return false;
                    }
                });
    }

    public void createRequisition(final InetSocketAddress opennmsHttpAddress, final InetSocketAddress postgresqlAddress) {
        // Build requisition object
        final List<RequisitionInterface> interfaces = new ArrayList<>();
        final RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr("172.18.45.116"); // IP-Address from the sflow-package we are going to send
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);

        final RequisitionNode node = new RequisitionNode();
        node.setNodeLabel("Dummy-Node");
        node.setForeignId("dummy-node");
        node.setInterfaces(interfaces);
        node.setLocation("MINION"); // The node must be in the same location as the sender, which is MINION

        final Requisition requisition = new Requisition("telemetry-sflow");
        requisition.insertNode(node);


        // Create requisition and trigger import
        final RestClient client = new RestClient(opennmsHttpAddress);
        client.addOrReplaceRequisition(requisition);
        client.importRequisition("telemetry-sflow");

        // Verify that node has been created
        final HibernateDaoFactory daoFactory = new HibernateDaoFactory(postgresqlAddress);
        final NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);
        final OnmsNode onmsNode = await().atMost(3, MINUTES).pollInterval(30, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class)
                        .eq("label", "Dummy-Node").toCriteria()), notNullValue());
        assertNotNull(onmsNode);
    }

    // localhost:32781/opennms/rest/measurements/node%5btest:1536585523417%5d.nodeSnmp%5b%5d/load_avg_5min
    private void waitForSentinelStartup(InetSocketAddress sentinelSshAddress) throws Exception {
        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = new SshClient(sentinelSshAddress, "admin", "admin")) {
                        final PrintStream pipe = sshClient.openShell();
                        pipe.println("log:display");
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final String sentinelReadyString = "Route: Sink.Server.Telemetry-SFlow started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-SFlow";
                        final boolean routeStarted = shellOutput.contains(sentinelReadyString);

                        logger.info("log:display");
                        logger.info("{}", shellOutput);
                        return routeStarted;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify sentinel startup: {}", ex.getMessage());
                        return false;
                    }
                });
    }

    public static void main(String[] args) throws IOException {
//        final InetSocketAddress minionSflowListenerAddress = new InetSocketAddress("192.168.1.119", 32773);
        final InetSocketAddress minionSflowListenerAddress = new InetSocketAddress("localhost", 50003);
        sendNetflowPacket(minionSflowListenerAddress, "/flows/sflow2.dat"); // ? record
    }
}
