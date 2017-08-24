/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery;

import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.Date;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * A simple Spring context unit test for Discovery.
 * 
 * @author Seth
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-discovery.xml",

        // Override the Pinger with a Pinger that always returns true
        "classpath:/applicationContext-testPinger.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DiscoveryIntegrationIT {

    private static final String CUSTOM_LOCATION = "my-custom-location";

    @Autowired
    private Discovery m_discovery;

    @Autowired
    private DiscoveryConfigFactory m_discoveryConfig;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private DiscoveryTaskExecutor m_taskExecutor;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "INFO");

        // Replace the default event forwarder with our mock
        m_discovery.setEventForwarder(m_eventIpcManager);
    }

    @Test
    public void testDiscovery() throws Exception {
        // Add a range of localhost IP addresses to ping
        IncludeRange range = new IncludeRange();
        range.setBegin("127.0.5.1");
        range.setEnd("127.0.5.254");
        range.setTimeout(5000l);
        range.setRetries(0);
        range.setLocation(CUSTOM_LOCATION);

        DiscoveryConfiguration config = m_discoveryConfig.getConfiguration();
        // Start immediately
        config.setInitialSleepTime(0l);

        // Discover 255 address ~= 10 seconds
        config.setPacketsPerSecond(25.5);

        // Add a discovery range to the config
        config.clearIncludeRanges();
        config.addIncludeRange(range);

        // Don't actually save the config or we'll overwrite the 
        // opennms-base-assembly XML file
        //m_discoveryConfig.saveConfiguration(config);
        //m_discoveryConfig.reload();

        // Anticipate newSuspect events for all of the addresses
        EventAnticipator anticipator = m_eventIpcManager.getEventAnticipator();
        StreamSupport.stream(m_discoveryConfig.getConfiguredAddresses().spliterator(), false).forEach(addr -> {
            System.out.println("ANTICIPATING: " + str(addr.getAddress()));
            Event event = new Event();
            event.setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
            event.setInterfaceAddress(addr.getAddress());
            anticipator.anticipateEvent(event);
        });

        // Don't re-init Discovery or it will reload the 
        // DiscoveryConfigFactory and erase our changes to 
        // the config
        //m_discovery.init();
        m_discovery.start();

        anticipator.waitForAnticipated(120000);
        anticipator.verifyAnticipated();
        anticipator.getAnticipatedEventsReceived().stream().forEach(eachEvent -> {
            Assert.assertNotNull(eachEvent.getParm("location"));
            Assert.assertEquals(CUSTOM_LOCATION, eachEvent.getParm("location").getValue().getContent());
        });

        m_discovery.stop();
    }

    @Test
    public void testDiscoveryTaskExecutor() throws Exception {
        // Add a range of localhost IP addresses to ping
        IncludeRange range = new IncludeRange();
        //range.setBegin("127.0.5.1");
        //range.setEnd("127.0.5.254");
        range.setBegin("192.168.99.1");
        range.setEnd("192.168.99.100");
        range.setTimeout(5000l);
        range.setRetries(0);

        DiscoveryConfiguration config = new DiscoveryConfiguration();
        config.setInitialSleepTime(0l);
        // 100 addresses at 10 per second should take at least 10 seconds
        config.setPacketsPerSecond(10d);
        config.clearIncludeRanges();
        config.addIncludeRange(range);

        // Anticipate newSuspect events for all of the addresses
        EventAnticipator anticipator = m_eventIpcManager.getEventAnticipator();
        StreamSupport.stream(new DiscoveryConfigFactory(config).getConfiguredAddresses().spliterator(), false).forEach(addr -> {
            System.out.println("ANTICIPATING: " + str(addr.getAddress()));
            Event event = new Event();
            event.setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
            event.setInterfaceAddress(addr.getAddress());
            anticipator.anticipateEvent(event);
        });

        Date beforeTime = new Date();
        // Invoke a one-time scan via the DiscoveryTaskExecutor service
        m_taskExecutor.handleDiscoveryTask(config);
        Date afterTime = new Date();
        // Make sure that this call returns quickly as an async. call
        long timespan = (afterTime.getTime() - beforeTime.getTime());
        System.out.println("Task executor invocation took " + timespan + "ms");
        assertTrue("Timespan was not less than 8 seconds: " + timespan, timespan < 8000L);

        anticipator.waitForAnticipated(60000);
        anticipator.verifyAnticipated();
    }
}
