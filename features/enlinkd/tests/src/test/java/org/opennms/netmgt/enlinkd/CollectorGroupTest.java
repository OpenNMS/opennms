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
import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.enlinkd.common.AbstractExecutable;
import org.opennms.netmgt.enlinkd.common.LegacyPriorityExecutor;
import org.opennms.netmgt.enlinkd.common.SchedulableNodeCollectorGroup;
import org.opennms.netmgt.scheduler.LegacyScheduler;

public class CollectorGroupTest {

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.enlinkd.service.api", "DEBUG");
        MockLogAppender.setupLogging(p);
    }

    public static class ExecutableTest extends AbstractExecutable {

        private final String m_name;
        public ExecutableTest(String name) {
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

            ExecutableTest that = (ExecutableTest) o;

            return Objects.equals(m_name, that.m_name);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
            return result;
        }

        @Override
        public boolean isReady() {
            return true;
        }
    }

    @Test
    public void testAddAndRemove() {
        LegacyPriorityExecutor executor= new LegacyPriorityExecutor("CollectorGroupTest",5,100);
        SchedulableNodeCollectorGroup collectorGroup = new SchedulableNodeCollectorGroup(1000,1000,executor,7,"collectorGroupA");
        assertEquals(0,collectorGroup.getExecutables().size());
        assertEquals(7,collectorGroup.getPriority().intValue());
        ExecutableTest discoveryTestA = new ExecutableTest("testA");
        assertNull(discoveryTestA.getPriority());
        collectorGroup.add(discoveryTestA);
        assertEquals(7, discoveryTestA.getPriority().intValue());
        assertEquals(1,collectorGroup.getExecutables().size());
        ExecutableTest discoveryTestB = new ExecutableTest("testB");
        assertNull(discoveryTestB.getPriority());
        collectorGroup.add(discoveryTestB);
        assertEquals(7,discoveryTestA.getPriority().intValue());
        assertEquals(2,collectorGroup.getExecutables().size());

        collectorGroup.remove(discoveryTestA);
        assertEquals(1,collectorGroup.getExecutables().size());
        assertEquals("testB", collectorGroup.getExecutables().iterator().next().getName());
        collectorGroup.remove(discoveryTestB);
        assertEquals(0,collectorGroup.getExecutables().size());
        assertNull(discoveryTestA.getPriority());
        assertNull(discoveryTestB.getPriority());
    }

    @Test
    public void testRun() {
        ExecutableTest discoveryTestA = new ExecutableTest("testA");
        discoveryTestA.setPriority(9);
        assertTrue(discoveryTestA.isReady());
        discoveryTestA.run();
    }

    @Test
    public void testSchedule() throws InterruptedException {
        LegacyScheduler scheduler = new LegacyScheduler("CollectorGroupTest", 1);
        LegacyPriorityExecutor executor= new LegacyPriorityExecutor("CollectorGroupTest",2,100);
        SchedulableNodeCollectorGroup collectorGroupA = new SchedulableNodeCollectorGroup(1500,1000,executor,0,"collectorGroupA");
        ExecutableTest discoveryTestAA = new ExecutableTest("testAA");
        collectorGroupA.add(discoveryTestAA);
        ExecutableTest discoveryTestAB = new ExecutableTest("testAB");
        collectorGroupA.add(discoveryTestAB);
        collectorGroupA.setScheduler(scheduler);
        collectorGroupA.schedule();

        SchedulableNodeCollectorGroup collectorGroupB = new SchedulableNodeCollectorGroup(1500,1000,executor,0,"collectorGroupB");
        ExecutableTest discoveryTestB = new ExecutableTest("testB");
        collectorGroupB.add(discoveryTestB);
        collectorGroupB.setScheduler(scheduler);
        collectorGroupB.schedule();

        executor.start();
        scheduler.start();
        Thread.sleep(4000);
        scheduler.stop();
        executor.stop();
    }


}
