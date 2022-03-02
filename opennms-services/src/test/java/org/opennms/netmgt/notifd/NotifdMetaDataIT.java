/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.net.InetAddress;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-notifdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class NotifdMetaDataIT {
    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    protected BroadcastEventProcessor m_eventProcessor;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        databasePopulator.populateDatabase();
    }
    
    @Test
    @Transactional
    public void testMetaData() throws Exception {
        // get the entities
        final OnmsNode node = databasePopulator.getNodeDao().get(1);
        final OnmsIpInterface ipInterface = node.getInterfaceWithAddress(InetAddress.getByName("192.168.1.1"));
        final OnmsMonitoredService monitoredService = ipInterface.getMonitoredServiceByServiceType("ICMP");

        // assure that all are non-null
        Assert.assertNotNull(node);
        Assert.assertNotNull(ipInterface);
        Assert.assertNotNull(monitoredService);

        // add specific meta-data to node, interface and service
        node.addMetaData("test", "virtual", "awesome");
        ipInterface.addMetaData("test", "devjam", "text");
        monitoredService.addMetaData("test", "2020", "message");

        // create a dummy notification
        final Notification notification = new Notification();
        notification.setTextMessage("This is an ${test:virtual} ${test:devjam} ${test:2020}.");

        // construct the event with node, interface and service associated
        final Event event = new EventBuilder()
                .setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
                .setMonitoredService(monitoredService)
                .getEvent();

        // process the data
        final Map<String, String> map = m_eventProcessor.buildParameterMap(notification, event, 42);

        // check for correct message
        Assert.assertEquals("This is an awesome text message.", map.get(NotificationManager.PARAM_TEXT_MSG));
    }
}
