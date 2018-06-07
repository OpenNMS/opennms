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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.hibernate.NotificationDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;

public class NotifdIT {

    private static TestEnvironment m_testEnvironment;
    private static RestClient restClient;

    private static final String NODE_LABEL = "node1";
    private static final String IP_ADDRESS = "192.168.1.1";

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().opennms();
            builder.withOpenNMSEnvironment().addFile(NotifdIT.class.getResource("/notifd-configuration.xml"),
                    "etc/notifd-configuration.xml");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
        if (m_testEnvironment == null) {
            return;
        }
        final InetSocketAddress opennmsHttp = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8980);
        restClient = new RestClient(opennmsHttp);
    }

    @Test
    public void testNotifdAutoAcknowledgeAlarm() {
        //Add a node with interface
        OnmsNode node = new OnmsNode();
        node.setLabel(NODE_LABEL);
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource("test");
        node.setForeignId(NODE_LABEL);
        Response response = restClient.addNode(node);
        assertEquals(201, response.getStatus());
        node = restClient.getNode("test:" + NODE_LABEL);
        OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setNode(node);
        ipInterface.setIpAddress(InetAddressUtils.getInetAddress(IP_ADDRESS));
        ipInterface.setIpHostName(IP_ADDRESS);
        response = restClient.addInterface("test:" + NODE_LABEL, ipInterface);
        assertEquals(201, response.getStatus());

        // Send a node down event
        EventBuilder builder = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, "test", new Date());
        builder.setNode(node);
        restClient.sendEvent(builder.getEvent());

        // Check that a notification is generated for node down
        InetSocketAddress pgsql = m_testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        NotificationDao notificationDao = daoFactory.getDao(NotificationDaoHibernate.class);
        Criteria criteria = new CriteriaBuilder(OnmsNotification.class).createAlias("node", "node")
                .eq("node.id", node.getId()).toCriteria();
        await().atMost(30, TimeUnit.SECONDS).pollInterval(10, TimeUnit.SECONDS)
                .until(DaoUtils.countMatchingCallable(notificationDao, criteria), greaterThan(0));

        // Send a node Up event
        EventBuilder builder1 = new EventBuilder(EventConstants.NODE_UP_EVENT_UEI, "test", new Date());
        builder1.setNode(node);
        restClient.sendEvent(builder1.getEvent());

        // Check that notification is auto-acknowledged
        criteria = new CriteriaBuilder(OnmsNotification.class).eq("answeredBy", "auto-acknowledged")
                .createAlias("node", "node").eq("node.id", node.getId()).toCriteria();

        await().atMost(1, TimeUnit.MINUTES).pollInterval(5, TimeUnit.SECONDS)
                .until(DaoUtils.countMatchingCallable(notificationDao, criteria), greaterThan(0));
    }

}
