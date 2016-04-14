/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.model.discovery.IPPollRange;

public class DiscoveryJobTest {

    @Test
    public void testCalculateTaskTimeout() throws Exception {
        final List<IPPollRange> m_ranges = new ArrayList<IPPollRange>();
        // Make 3 ranges of 5 addresses each
        for (int i = 0; i < 3; i++) {
            IPPollRange ipPollRange = new IPPollRange("127.0.1." + ((i * 5) + 1), "127.0.1." + ((i * 5) + 5), 50, 2);
            m_ranges.add(ipPollRange);
        }
        DiscoveryJob discoveryJob = new DiscoveryJob(m_ranges, "Bogus FS", "Bogus Location", Double.MAX_VALUE);
        // Each task is taking 750 ms so totalTaskTimeout = 750 ms * 3 (number of tasks) = 2250 ms
        assertEquals(Math.round(2250 * 1.5), discoveryJob.calculateTaskTimeout()); 

        // Set the rate to 1 per second and make sure that the timeout is extended
        // by 1000 milliseconds per all 15 IP addresses
        discoveryJob = new DiscoveryJob(m_ranges, "Bogus FS", "Bogus Location", 1.0);
        assertEquals(Math.round(2250 * 1.5) + (15 * 1000), discoveryJob.calculateTaskTimeout()); 
    }

    @Test
    public void testTimeoutIntegerMaxValueMinus1() throws Exception {
        List<IPPollRange> m_ranges = new ArrayList<IPPollRange>();

        // Minimum value that should be smaller than Integer.MAX_VALUE:
        //
        // (Integer.MAX_VALUE - 1) * timeout * retries * fudge_factor / fudge_factor
        //       (2^31 - 1)        *    1    *    1    *     1.5      /     1.5
        //  ((2^31 - 1) / 3 * 2)   *    1    *    1    *     1.5

        // Start the range at 0.0.0.1 so that we don't count zero as a value
        IPPollRange ipPollRange = new IPPollRange("0.0.0.1", "85.85.85.84", 1, 0);
        m_ranges.add(ipPollRange);
        DiscoveryJob discoveryJob = new DiscoveryJob(m_ranges, "Bogus FS", "Bogus Location", Double.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE - 1, discoveryJob.calculateTaskTimeout());
    }

    @Test
    public void testTimeoutIntegerMaxValue() throws Exception {
        List<IPPollRange> m_ranges = new ArrayList<IPPollRange>();

        // Start the range at 0.0.0.1 so that we don't count zero as a value
        IPPollRange ipPollRange = new IPPollRange("0.0.0.1", "85.85.85.85", 1, 0);
        m_ranges.add(ipPollRange);
        DiscoveryJob discoveryJob = new DiscoveryJob(m_ranges, "Bogus FS", "Bogus Location", Double.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, discoveryJob.calculateTaskTimeout());
    } 

    @Test
    public void testTimeoutLargerThanIntegerMaxValue1() throws Exception {
        List<IPPollRange> m_ranges = new ArrayList<IPPollRange>();
        IPPollRange ipPollRange = new IPPollRange("0.0.0.0", "89.0.0.0", 1, 0);
        m_ranges.add(ipPollRange);
        DiscoveryJob discoveryJob = new DiscoveryJob(m_ranges, "Bogus FS", "Bogus Location", Double.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, discoveryJob.calculateTaskTimeout());
    } 

    @Test
    public void testTimeoutLessThanIntegerMaxValueWithDefaultTimeoutRetries() throws Exception {
        List<IPPollRange> m_ranges = new ArrayList<IPPollRange>();
        // TODO: Replace defaults with constants
        IPPollRange ipPollRange = new IPPollRange("127.0.0.1", "127.5.118.25", 2000, 1);
        m_ranges.add(ipPollRange);
        DiscoveryJob discoveryJob = new DiscoveryJob(m_ranges, "Bogus FS", "Bogus Location", Double.MAX_VALUE);
        assertTrue(Integer.MAX_VALUE > discoveryJob.calculateTaskTimeout()); 
    }

    @Test
    public void testTimeoutIntegerMaxValueWithDefaultTimeoutRetries() throws Exception {
        List<IPPollRange> m_ranges = new ArrayList<IPPollRange>();
        // TODO: Replace defaults with constants
        IPPollRange ipPollRange = new IPPollRange("127.0.0.1", "127.5.118.26", 2000, 1);
        m_ranges.add(ipPollRange);
        DiscoveryJob discoveryJob = new DiscoveryJob(m_ranges, "Bogus FS", "Bogus Location", Double.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, discoveryJob.calculateTaskTimeout()); 
    }
}
