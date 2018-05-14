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

package org.opennms.netmgt.threshd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.Level;
import org.opennms.core.test.LoggingEvent;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.rrd.RrdRepository;

public class ThresholdingSetTest {
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        MockDatabase m_db = new MockDatabase();
        final MockNetwork network = new MockNetwork();
        network.addNode(1, "localhost").addInterface("127.0.0.1").addService("SNMP", 1);
        m_db.populate(network);
        DataSourceFactory.setInstance(m_db);
    }

    @Test(expected=ThresholdInitializationException.class)
    public void testBadThresholdingConfigInitialize() throws Exception {
        System.setProperty("opennms.home", getClass().getResource("testBadThresholdingConfig").getFile());
        PollOutagesConfigFactory.init();
        new ThresholdingSet(1, "127.0.0.1", "SNMP", new RrdRepository());
    }

    @Test
    public void testBadThresholdingConfigReinitialize() throws Exception {
        final String opennmsHome = getClass().getResource("testBadThresholdingConfigReinitialize").getFile();
        final Path goodXml = Paths.get(opennmsHome, "etc", "good-thresholds.xml");
        final Path badXml = Paths.get(opennmsHome, "etc", "bad-thresholds.xml");
        final Path targetXml = Paths.get(opennmsHome,  "etc", "thresholds.xml");

        if (targetXml.toFile().exists()) {
            Files.delete(targetXml);
        }
        Files.copy(goodXml, targetXml);

        System.setProperty("opennms.home", opennmsHome);
        PollOutagesConfigFactory.init();
        final ThresholdingSet set = new ThresholdingSet(1, "127.0.0.1", "SNMP", new RrdRepository());

        LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
        assertEquals("There should be no warnings or higher after initializing with a good config.", 0, events.length);

        // there are no outages configured for the node
        assertFalse(set.isNodeInOutage());
        // the config is not empty
        assertTrue(set.hasThresholds());

        /* thresholding config has changed, reinitialize */
        Files.delete(targetXml);
        Files.copy(badXml, targetXml);
        set.reinitialize(true);

        // there is no information about the node, so it should say it does not have an outage
        assertFalse(set.isNodeInOutage());
        // the config is not empty
        assertTrue(set.hasThresholds());

        events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
        assertEquals("There should be one error after reinitializing with a bad config.", 1, events.length);
        assertEquals("It should have an error-level log message.", Level.ERROR, events[0].getLevel());
        assertTrue("It should say it is reverting to previous configuration.", events[0].getMessage().contains("Reverting to previous"));
    }

}
