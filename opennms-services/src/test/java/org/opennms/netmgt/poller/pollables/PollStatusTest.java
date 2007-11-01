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


import junit.framework.TestCase;

import org.opennms.netmgt.model.PollStatus;
public class PollStatusTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.getPollStatus(int)'
     */
    public void testGetPollStatusInt() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.getPollStatus(int, String)'
     */
    public void testGetPollStatusIntString() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.PollStatus(int, String, String)'
     */
    public void FIXMEtestPollStatus() {
        
        //Compare construction via constant
        PollStatus statusDown1 = PollStatus.down("test down 1");
        PollStatus statusDown2 = PollStatus.down("test down 2");
        
        assertTrue(statusDown1 == statusDown2);
        assertTrue(statusDown1.equals(statusDown2));
        
        statusDown2 = PollStatus.get(PollStatus.SERVICE_UNAVAILABLE, (String)null);
        assertFalse(statusDown1 == statusDown2);
        assertTrue(statusDown1.equals(statusDown2));
        
    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.isUp()'
     */
    public void testIsUp() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.isDown()'
     */
    public void testIsDown() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.toString()'
     */
    public void testToString() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.getReason()'
     */
    public void testGetReason() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.setReason(String)'
     */
    public void testSetReason() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.getStatusCode()'
     */
    public void testGetStatusCode() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.setStatusCode(int)'
     */
    public void testSetStatusCode() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.getStatusName()'
     */
    public void testGetStatusName() {

    }

    /*
     * Test method for 'org.opennms.netmgt.poller.pollables.PollStatus.setStatusName(String)'
     */
    public void testSetStatusName() {

    }

}
