/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.StreamSupport;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.provision.detector.registry.impl.ServiceDetectorRegistryImpl;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
        "classpath*:/META-INF/opennms/provisiond-extensions.xml",
        "classpath:/applicationContext-discovery-mock.xml",
        // Override the Pinger with a Pinger that always returns true
        "classpath:/applicationContext-testPinger.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DiscoveryWithDetectorsIT {

    private static final String CUSTOM_LOCATION = "my-custom-location";

    @Autowired
    private Discovery m_discovery;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private DiscoveryTaskExecutor m_taskExecutor;

    @Autowired
    private ServiceDetectorRegistryImpl serviceDetectorRegistry;


    @Test(timeout = 30000)
    public void testDiscoveryWithMockDetector() throws IOException, InterruptedException {
        MockLogAppender.setupLogging(true, "INFO");
        m_discovery.setEventForwarder(m_eventIpcManager);
        String resourcePath = DiscoveryConfigDetectorsTest.class.getResource("/etc/discovery-configuration.xml").getPath();
        Path etcPath = Paths.get(resourcePath).getParent().getParent();
        System.setProperty("opennms.home", etcPath.toString());
        DiscoveryConfigFactory configFactory = new DiscoveryConfigFactory();
        m_discovery.setDiscoveryFactory(configFactory);
        serviceDetectorRegistry.onBind(new MockServiceDetectorFactory1(), new HashMap());
        serviceDetectorRegistry.onBind(new MockServiceDetectorFactory2(), new HashMap());
        // Anticipate newSuspect events for all of the addresses
        EventAnticipator anticipator = m_eventIpcManager.getEventAnticipator();
        StreamSupport.stream(configFactory.getConfiguredAddresses().spliterator(), false).forEach(addr -> {
            // Detection fails for IPAddresses greater than 192.168.0.120 in MockServiceDetector.
            if (InetAddressUtils.isInetAddressInRange(InetAddressUtils.str(addr.getAddress()),
                    "192.168.0.1", "192.168.0.120") &&
                    !InetAddressUtils.isInetAddressInRange(InetAddressUtils.str(addr.getAddress()),
                            "192.168.0.50", "192.168.0.59")) {
                System.out.println("ANTICIPATING: " + str(addr.getAddress()));
                Event event = new Event();
                event.setUei(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
                event.setInterfaceAddress(addr.getAddress());
                anticipator.anticipateEvent(event);
            }
        });

        m_discovery.start();
        // Scan executor has 200 threads. So it should handle two jobs in sequence taking ~ 10 secs.
        anticipator.waitForAnticipated(15000);
        anticipator.verifyAnticipated();
        m_discovery.stop();
    }
}
