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
package org.opennms.netmgt.poller.pollables;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.PollStatus;
public class PollStatusTest {
    protected PollStatus statusDown1      = null;
    protected PollStatus statusDown2      = null;
    protected PollStatus statusDown3      = null;
    protected PollStatus statusAvailable1 = null;
    
    @Before
    public void setUp() {
        statusDown1      = PollStatus.down("test down 1");
        statusDown2      = PollStatus.down("test down 2");
        statusDown3      = PollStatus.get(PollStatus.SERVICE_UNRESPONSIVE, "test down 3");
        statusAvailable1 = PollStatus.get(PollStatus.SERVICE_AVAILABLE, "test up 1");
        
    }
    
    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.PollStatus(int, String, String)'
     */
    @Test
    public void testPollStatus() {
        //Compare construction via constant
        PollStatus statusDown1 = PollStatus.down("test down 1");
        PollStatus statusDown2 = PollStatus.down("test down 2");

        assertTrue(statusDown1.getStatusCode() == statusDown2.getStatusCode());
        assertFalse(statusDown1.getStatusCode() == statusDown3.getStatusCode());
        assertTrue(statusDown1.isDown());
        assertTrue(statusDown3.isUp());
        assertTrue(statusAvailable1.isUp());
        assertFalse(statusAvailable1.isDown());
    }

}
