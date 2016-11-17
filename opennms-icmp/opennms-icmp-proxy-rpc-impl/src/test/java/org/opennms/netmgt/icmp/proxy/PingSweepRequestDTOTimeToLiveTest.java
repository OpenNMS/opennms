/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.netmgt.icmp.PingConstants;

public class PingSweepRequestDTOTimeToLiveTest {

    @Test
    public void testCalculateTaskTimeout() throws Exception {
        PingSweepRequestDTO request = new PingSweepRequestDTO();

        // Make 3 ranges of 5 addresses each
        for (int i = 0; i < 3; i++) {
            request.addIpRange(new IPRangeDTO("127.0.1." + ((i * 5) + 1), "127.0.1." + ((i * 5) + 5), 2, 50));
        }

        // Each task is taking 750 ms so totalTaskTimeout = 750 ms * 3 (number of tasks) = 2250 ms
        // With a (default) rate of 1 packet per second, this should be extended
        // by 1000 milliseconds per all 15 IP addresses
        assertEquals(new Long(Math.round(2250 * 1.5) + (15 * 1000)), request.getTimeToLiveMs()); 
    }

    @Test
    public void testTimeoutCloseToLongMaxValue() throws Exception {
        PingSweepRequestDTO request = new PingSweepRequestDTO();
        // Verifies that we generate TTLs that approach Long.MAX_VALUE
        request.addIpRange(new IPRangeDTO("::1", "::1f:ffff:ffff:ffff", 0, 1));
        assertEquals(new Long(9020710053623102000L), request.getTimeToLiveMs());
    }

    @Test
    public void testTimeoutLargerThanLongMaxValue() throws Exception {
        PingSweepRequestDTO request = new PingSweepRequestDTO();
        // Verifies that we don't generate TTLs that exceed Long.MAX_VALUE
        request.addIpRange(new IPRangeDTO("2000::", "3FFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF", 0, 1));
        assertEquals(new Long(Long.MAX_VALUE), request.getTimeToLiveMs());
    }

    @Test
    public void testTimeoutLessThanLongMaxValueWithDefaultTimeoutRetries() throws Exception {
        PingSweepRequestDTO request = new PingSweepRequestDTO();
        request.addIpRange(new IPRangeDTO("127.0.0.1", "127.5.118.25", PingConstants.DEFAULT_TIMEOUT, PingConstants.DEFAULT_RETRIES));
        assertTrue(Long.MAX_VALUE > request.getTimeToLiveMs()); 
    }

    @Test
    public void testTimeoutIntegerMaxValueWithDefaultTimeoutRetries() throws Exception {
        PingSweepRequestDTO request = new PingSweepRequestDTO();
        request.addIpRange(new IPRangeDTO("2000::", "3FFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF", PingConstants.DEFAULT_TIMEOUT, PingConstants.DEFAULT_RETRIES));
        assertEquals(new Long(Long.MAX_VALUE), request.getTimeToLiveMs()); 
    }
}
