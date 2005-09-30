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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.passive;

import org.jmock.cglib.MockObjectTestCase;
import org.opennms.netmgt.poller.pollables.PollStatus;

public class PassiveStatusKeeperTest extends MockObjectTestCase {


    private PassiveStatusKeeper m_psk;


    protected void setUp() throws Exception {
        super.setUp();

        m_psk = new PassiveStatusKeeper();
        m_psk.init();
        m_psk.start();
        
}

    protected void tearDown() throws Exception {
        m_psk.stop();

        super.tearDown();
    }
    

    public void testSetStatus() {
        String nodeLabel = "localhost";
        String ipAddr = "127.0.0.1";
        String svcName = "PSV";
        testSetStatus(nodeLabel, ipAddr, svcName);
        
    }

    private void testSetStatus(String nodeLabel, String ipAddr, String svcName) {
        PollStatus pollStatus = PollStatus.STATUS_UP;
        
        PassiveStatusKeeper.setStatus(nodeLabel, ipAddr, svcName, pollStatus);
        assertEquals(pollStatus, PassiveStatusKeeper.getStatus(nodeLabel, ipAddr, svcName));
    }
    
    public void testRestart() {
        String nodeLabel = "localhost";
        String ipAddr = "127.0.0.1";
        String svcName = "PSV";
        
        testSetStatus(nodeLabel, ipAddr, svcName);

        m_psk.stop();
        
        m_psk.init();
        m_psk.start();
        
     //   assertEquals(PollStatus.STATUS_UNKNOWN, PassiveStatusKeeper.getStatus(nodeLabel, ipAddr, svcName));
        
    }

}
