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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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
