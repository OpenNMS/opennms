/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;


import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
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
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;

import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;

/**
 * Verifies that Telemetry listeners can receive proto buffers and generate rrd
 * files
 *
 * @author cgorantla
 */

public class JtiTelemetryIT {

    private static final Logger LOG = LoggerFactory.getLogger(JtiTelemetryIT.class);
    public static final String SENDER_IP = "192.168.1.1";

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
            builder.withOpenNMSEnvironment()
                    .addFile(JtiTelemetryIT.class.getResource("/telemetry/jti-telemetryd-configuration.xml"),
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
    public void verifyJtiTelemetryOnOpenNMS() throws Exception {

        Date startOfTest = new Date();

        OnmsNode onmsNode = sendnewSuspectEvent(executor, opennmsHttp, m_testEnvironment, false, startOfTest);

        final InetSocketAddress opennmsUdp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 50000, "udp");

        sendJtiTelemetryMessage(opennmsUdp);

        await().atMost(30, SECONDS).pollDelay(0, SECONDS).pollInterval(5, SECONDS)
                .until(matchRrdFileFromNodeResource(onmsNode.getId()));

    }

    @Test
    public void verifyJtiTelemetryOnMinion() throws Exception {

        Date startOfTest = new Date();

        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 8201);
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // Modify minion configuration for telemetry
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.features.telemetry.listeners-udp-50000");
            pipe.println("config:property-set name JTI");
            pipe.println("config:property-set class-name org.opennms.netmgt.telemetry.listeners.udp.UdpListener");
            pipe.println("config:property-set listener.port 50000");
            pipe.println("config:update");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
        }

        OnmsNode onmsNode = sendnewSuspectEvent(executor, opennmsHttp, m_testEnvironment, true, startOfTest);

        final InetSocketAddress minionUdp = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 50000, "udp");

        sendJtiTelemetryMessage(minionUdp);

        await().atMost(2, MINUTES).pollDelay(0, SECONDS).pollInterval(15, SECONDS)
                .until(matchRrdFileFromNodeResource(onmsNode.getId()));
    }

    private void sendJtiTelemetryMessage(InetSocketAddress udpAddress) throws IOException {
       
        byte[] jtiOutBytes = Resources.toByteArray(Resources.getResource("telemetry/jti-proto.raw"));
        DatagramPacket packet = new DatagramPacket(jtiOutBytes, jtiOutBytes.length, udpAddress);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.send(packet);
        } catch (IOException e) {
            LOG.error("Exception while sending jti packets", e);
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
                if (message.contains("rrdFile=\"ifOutOctets") && message.contains("eth0_system_test")) {
                    return true;
                } else {
                    return false;
                }

            }
        };
    }


    public static OnmsNode sendnewSuspectEvent(Executor executor, InetSocketAddress opennmsHttp,
            TestEnvironment m_testEnvironment, boolean isMinion, Date startOfTest)
            throws ClientProtocolException, IOException {

        Event minionEvent = new Event();
        minionEvent.setUei("uei.opennms.org/internal/discovery/newSuspect");
        minionEvent.setHost(SENDER_IP);
        minionEvent.setInterface(SENDER_IP);
        minionEvent.setInterfaceAddress(Inet4Address.getByName(SENDER_IP));
        minionEvent.setSource("system-test");
        minionEvent.setSeverity("4");
        if (isMinion) {
            Parm parm = new Parm();
            parm.setParmName("location");
            Value minion = new Value("MINION");
            parm.setValue(minion);
            List<Parm> parms = new ArrayList<>();
            parms.add(parm);
            minionEvent.setParmCollection(parms);
        }

        String xmlString = JaxbUtils.marshal(minionEvent);

        executor.execute(Request.Post(String.format("http://%s:%d/opennms/rest/events",
                opennmsHttp.getAddress().getHostAddress(), opennmsHttp.getPort()))
                .bodyString(xmlString, ContentType.APPLICATION_XML)).returnContent();

        InetSocketAddress pgsql = m_testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        EventDao eventDao = daoFactory.getDao(EventDaoHibernate.class);
        NodeDao nodeDao = daoFactory.getDao(NodeDaoHibernate.class);

        Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI).ge("eventTime", startOfTest)
                .eq("ipAddr", Inet4Address.getByName(SENDER_IP)).toCriteria();

        await().atMost(1, MINUTES).pollInterval(10, SECONDS).until(DaoUtils.countMatchingCallable(eventDao, criteria),
                greaterThan(0));

        final OnmsNode onmsNode = await().atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(nodeDao, new CriteriaBuilder(OnmsNode.class).eq("label", SENDER_IP)
                        .ge("createTime", startOfTest).toCriteria()), notNullValue());

        assertNotNull(onmsNode);

        if (isMinion) {
            assertThat(onmsNode.getLocation().getLocationName(), is("MINION"));
        }

        LOG.info(" New suspect event has been sent and node has been created for IP : {}", SENDER_IP);
        return onmsNode;
    }
}
