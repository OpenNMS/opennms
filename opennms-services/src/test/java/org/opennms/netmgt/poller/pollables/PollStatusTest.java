/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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
