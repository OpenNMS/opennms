//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.poller.pollables;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.PollStatus;
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
