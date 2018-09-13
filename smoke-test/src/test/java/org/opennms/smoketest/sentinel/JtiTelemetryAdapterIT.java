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
import static org.opennms.smoketest.minion.JtiTelemetryIT.sendJtiTelemetryMessage;

import java.net.InetSocketAddress;
import java.nio.file.Path;
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
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.smoketest.utils.TargetRoot;
import org.opennms.test.system.api.NewTestEnvironment;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class JtiTelemetryAdapterIT {

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
                    .newts()
                    .sentinel();

            // Enable JTI Adapter
            final Path opennmsSourceEtcDirectory = new TargetRoot(getClass()).getPath("system-test-resources", "etc");
            builder.withSentinelEnvironment()
                    .addFile(getClass().getResource("/sentinel/features-newts-jti.xml"), "deploy/features.xml")
                    .addFile(getClass().getResource("/sentinel/junos-telemetry-interface.groovy"), "etc/junos-telemetry-interface.groovy")
                    .addFiles(opennmsSourceEtcDirectory.resolve("resource-types.d"), "etc/resource-types.d")
                    .addFiles(opennmsSourceEtcDirectory.resolve("datacollection"), "etc/datacollection")
                    .addFile(opennmsSourceEtcDirectory.resolve("datacollection-config.xml"), "etc/datacollection-config.xml");

            // Enable NXOS-Listener
            builder.withMinionEnvironment()
                    .addFile(getClass().getResource("/sentinel/org.opennms.features.telemetry.listeners-udp-50000-jti.cfg"), "etc/org.opennms.features.telemetry.listeners-udp-jti.cfg")
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
    public void verifyAdapter() throws Exception {
        // Determine endpoints
        final InetSocketAddress sentinelSshAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.SENTINEL, 8301);
        final InetSocketAddress minionListenerAddress = testEnvironment.getServiceAddress(NewTestEnvironment.ContainerAlias.MINION, 50000, "udp");
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
        new KarafShell(sentinelSshAddress).verifyLog((output) -> output.contains("Route: Sink.Server.Telemetry-JTI started and consuming from: queuingservice://OpenNMS.Sink.Telemetry-JTI"));

        // Now sentinel is up and running, we should re-sync the datasource,as the
        // earlier created node may not be visible to sentinel yet.
        new KarafShell(sentinelSshAddress).runCommand("nodecache:sync");

        // Ensure no measurement data available
        final Response response = RestAssured.given().accept(ContentType.JSON)
                .get("/measurements/node[telemetry-jti:dummy-node].interfaceSnmp[eth0_system_test]/ifOutOctets");
        Assert.assertEquals(404, response.statusCode());

        // Send nxos packet to minion
        sendJtiTelemetryMessage(minionListenerAddress);

        await().atMost(3, TimeUnit.MINUTES).pollInterval(10, TimeUnit.SECONDS).until(
                () -> {
                    final Response theResponse = RestAssured.given().accept(ContentType.JSON)
                            .get("/measurements/node[telemetry-jti:dummy-node].interfaceSnmp[eth0_system_test]/ifOutOctets");
                    return theResponse.statusCode() == 200;
                }
        );
    }

    public static OnmsNode createRequisition(InetSocketAddress opennmsHttp, InetSocketAddress postgresAddress) {
        final RestClient client = new RestClient(opennmsHttp);
        final Requisition requisition = new Requisition("telemetry-jti");
        List<RequisitionInterface> interfaces = new ArrayList<>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr("192.168.1.1");
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        node.setNodeLabel("dummy-node");
        node.setForeignId("dummy-node");
        node.setInterfaces(interfaces);
        node.setLocation("MINION");
        requisition.insertNode(node);
        client.addOrReplaceRequisition(requisition);
        client.importRequisition("telemetry-jti");

        HibernateDaoFactory daoFactory = new HibernateDaoFactory(postgresAddress);
        NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);

        final OnmsNode onmsNode = await().atMost(3, MINUTES).pollInterval(30, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class)
                        .eq("foreignSource", "telemetry-jti")
                        .eq("label", "dummy-node").toCriteria()), notNullValue());

        assertNotNull(onmsNode);

        return onmsNode;

    }
}
