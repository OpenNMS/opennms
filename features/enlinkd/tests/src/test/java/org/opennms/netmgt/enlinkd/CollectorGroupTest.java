/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Objects;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.enlinkd.common.SchedulableGroupExecutor;
import org.opennms.netmgt.enlinkd.common.SchedulableNodeCollectorGroup;
import org.opennms.netmgt.enlinkd.common.Executable;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectorGroupTest {

    private final static Logger LOG = LoggerFactory.getLogger(CollectorGroupTest.class);

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.enlinkd.service.api", "DEBUG");
        MockLogAppender.setupLogging(p);
    }

    public static class DiscoveryTest extends Executable {

        private final String m_name;
        public DiscoveryTest(String name) {
            super();
            m_name=name;
        }

        @Override
        public String getName() {
            return m_name;
        }

        @Override
        public void runExecutable() {
            System.out.println(m_name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            DiscoveryTest that = (DiscoveryTest) o;

            return Objects.equals(m_name, that.m_name);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
            return result;
        }
    }

    @Test
    public void testAddAndRemove() {
        SchedulableGroupExecutor executor= new SchedulableGroupExecutor(5,100);
        SchedulableNodeCollectorGroup collectorGroup = new SchedulableNodeCollectorGroup(1000,1000,executor,0,"collectorGroupA");
        assertEquals(0,collectorGroup.getExecutables().size());
        assertEquals(0,collectorGroup.getPriority());
        DiscoveryTest discoveryTestA = new DiscoveryTest("testA");
        collectorGroup.add(discoveryTestA);
        assertEquals(1,collectorGroup.getExecutables().size());
        assertEquals(collectorGroup,discoveryTestA.getCollectorGroup());
        assertEquals(0,discoveryTestA.getPriority());
        DiscoveryTest discoveryTestB = new DiscoveryTest("testB");
        collectorGroup.add(discoveryTestB);
        assertEquals(2,collectorGroup.getExecutables().size());
        assertEquals(collectorGroup,discoveryTestB.getCollectorGroup());

        collectorGroup.remove(discoveryTestA);
        assertEquals(1,collectorGroup.getExecutables().size());
        assertNull(discoveryTestA.getCollectorGroup());
        assertEquals("testB", collectorGroup.getExecutables().iterator().next().getName());
        collectorGroup.remove(discoveryTestB);
        assertNull(discoveryTestB.getCollectorGroup());
        assertEquals(0,collectorGroup.getExecutables().size());
    }

    @Test
    public void testSchedule() throws InterruptedException {
        LegacyScheduler scheduler = new LegacyScheduler("CollectorGroupTest", 1);
        SchedulableGroupExecutor executor= new SchedulableGroupExecutor(2,100);
        SchedulableNodeCollectorGroup collectorGroupA = new SchedulableNodeCollectorGroup(1500,1000,executor,0,"collectorGroupA");
        DiscoveryTest discoveryTestAA = new DiscoveryTest("testAA");
        collectorGroupA.add(discoveryTestAA);
        DiscoveryTest discoveryTestAB = new DiscoveryTest("testAB");
        collectorGroupA.add(discoveryTestAB);
        collectorGroupA.setScheduler(scheduler);
        collectorGroupA.schedule();

        SchedulableNodeCollectorGroup collectorGroupB = new SchedulableNodeCollectorGroup(1500,1000,executor,0,"collectorGroupB");
        DiscoveryTest discoveryTestB = new DiscoveryTest("testB");
        collectorGroupB.add(discoveryTestB);
        collectorGroupB.setScheduler(scheduler);
        collectorGroupB.schedule();

        scheduler.start();
        Thread.sleep(4000);
        scheduler.stop();
    }


}
