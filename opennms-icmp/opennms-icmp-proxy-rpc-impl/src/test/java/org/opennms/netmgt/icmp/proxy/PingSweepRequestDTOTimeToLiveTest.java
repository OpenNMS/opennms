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
