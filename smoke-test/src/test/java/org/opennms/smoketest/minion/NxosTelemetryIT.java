/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
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
import org.opennms.smoketest.telemetry.Packet;
import org.opennms.smoketest.telemetry.Packets;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NxosTelemetryIT {

    private static final Logger LOG = LoggerFactory.getLogger(NxosTelemetryIT.class);

    private static TestEnvironment m_testEnvironment;
    private static Executor executor;
    private static InetSocketAddress opennmsHttp;

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
            builder.withOpenNMSEnvironment().addFile(
                    JtiTelemetryIT.class.getResource("/telemetry/nxos-telemetryd-configuration.xml"),
                    "etc/telemetryd-configuration.xml");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDockerAndLoadExecutor() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
        if (m_testEnvironment == null) {
            return;
        }
        opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);
        final HttpHost opennmsHttpHost = new HttpHost(opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort());
        // Ignore 302 response to the POST
        HttpClient instance = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

        executor = Executor.newInstance(instance).auth(opennmsHttpHost, "admin", "admin")
                .authPreemptive(opennmsHttpHost);

    }

    @Test
    public void verifyNxosTelemetryOnOpenNMS() throws Exception {

        Date startOfTest = new Date();

        OnmsNode onmsNode = addRequisition(opennmsHttp, false, startOfTest);
        final InetSocketAddress opennmsUdp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 50001, "udp");
        sendNxosTelemetryMessage(opennmsUdp);

        await().atMost(30, SECONDS).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(matchRrdFileFromNodeResource(onmsNode.getId()));

    }


    @Test
    public void verifyNxosTelemetryOnMinion() throws Exception {

        Date startOfTest = new Date();

        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 8201);
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // Modify minion configuration for telemetry
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.features.telemetry.listeners-udp-50001");
            pipe.println("config:property-set name NXOS");
            pipe.println("config:property-set class-name org.opennms.netmgt.telemetry.listeners.UdpListener");
            pipe.println("config:property-set parameters.port 50001");
            pipe.println("config:property-set parsers.1.name NXOS");
            pipe.println("config:property-set parsers.1.class-name org.opennms.netmgt.telemetry.protocols.common.parser.ForwardParser");
            pipe.println("config:update");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        OnmsNode onmsNode = addRequisition(opennmsHttp, true, startOfTest);
        final InetSocketAddress minionUdp = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 50001, "udp");
        sendNxosTelemetryMessage(minionUdp);

        await().atMost(2, MINUTES).pollDelay(0, SECONDS).pollInterval(15, SECONDS)
                .until(matchRrdFileFromNodeResource(onmsNode.getId()));
    }

    public static void sendNxosTelemetryMessage(InetSocketAddress udpAddress) throws IOException {
        try {
            new Packet(Packets.NXOS.getResource(), udpAddress).send();
        } catch (IOException e) {
            LOG.error("Exception while sending nxos packets", e);
        }
    }

    public static Callable<Boolean> matchRrdFileFromNodeResource(Integer id)
            throws ClientProtocolException, IOException {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                HttpResponse response = executor
                        .execute(Request.Get(String.format("http://%s:%d/opennms/rest/resources/fornode/%d",
                                opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort(), id)))
                        .returnResponse();

                String message = EntityUtils.toString(response.getEntity());
                LOG.info(message);
                if (message.contains("rrdFile=\"load_avg_1min")) {
                    return true;
                } else {
                    return false;
                }

            }
        };
    }

    public static OnmsNode addRequisition(InetSocketAddress opennmsHttp, boolean isMinion, Date startOfTest) {

        RestClient client = new RestClient(opennmsHttp);
        Requisition requisition = new Requisition("telemetry");
        List<RequisitionInterface> interfaces = new ArrayList<>();
        RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr("192.168.0.1");
        requisitionInterface.setManaged(true);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        interfaces.add(requisitionInterface);
        RequisitionNode node = new RequisitionNode();
        String label = "nexus9k";
        node.setNodeLabel(label);
        node.setForeignId("nxos");
        node.setInterfaces(interfaces);
        if (isMinion) {
            // For a requisition, foreignId needs to be unique, change foreignId
            node.setLocation("MINION");
            node.setForeignId("nexus9k");
            // Change label so that node matches with foreignId
            label = "nxos";
            node.setNodeLabel(label);
        }
        requisition.insertNode(node);
        client.addOrReplaceRequisition(requisition);
        client.importRequisition("telemetry");

        InetSocketAddress pgsql = m_testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);

        final OnmsNode onmsNode = await().atMost(3, MINUTES).pollInterval(30, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class)
                        .ge("createTime", startOfTest).eq("label", label).toCriteria()), notNullValue());

        assertNotNull(onmsNode);

        return onmsNode;

    }

}
