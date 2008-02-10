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
// Modifications:
//
// 2008 Feb 10: Eliminate warnings. - dj@opennms.org
// 2007 Jan 29: Modify to work with TestCase changes; rename to show that it's an integration test. - dj@opennms.org
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
package org.opennms.netmgt.threshd;

import java.io.File;

import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.mock.MockLogAppender;

/**
 * FIXME: Should this test case go away now that we use ThresholdingVisitor?
 */
public class SnmpThresholderIntegrationTest extends ThresholderTestCase {
    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
        
        setupDatabase();
        
        createMockRrd();

        setupEventManager();
        
        replayMocks();
       
        String rrdRepository = "target/threshd-test";
        String fileName = "cpuUtilization"+RrdUtils.getExtension();
        int nodeId = 1;
        String ipAddress = "192.168.1.1";
        String serviceName = "SNMP";
        String groupName = "default-snmp";
        
        setupThresholdConfig(rrdRepository+File.separator+nodeId, fileName, nodeId, ipAddress, serviceName, groupName);

        
        m_thresholder = new SnmpThresholder();
        m_thresholder.initialize(m_serviceParameters);
        m_thresholder.initialize(m_iface, m_parameters);
        
        verifyMocks();
        
        expectRrdStrategyCalls();

    }


    protected void tearDown() throws Exception {
        RrdUtils.setStrategy(null);
        MockLogAppender.assertNoWarningsOrGreater();
        super.tearDown();
    }
    
    public void testNormalValue() throws Exception {
        
        setupFetchSequence("cpuUtilization", 69.0, 79.0, 74.0, 74.0);
        
        
        replayMocks();
        ensureNoEventAfterFetches("cpuUtilization", 4);
        verifyMocks();
        
    }
    
    public void testBigValue() throws Exception {
        
        setupFetchSequence("cpuUtilization", 99.0, 98.0, 97.0, 96.0, 95.0);
        
        replayMocks();
        ensureExceededAfterFetches("cpuUtilization", 3);
        ensureNoEventAfterFetches("cpuUtilization", 2);
        verifyMocks();
    }
    
    public void testRearm() throws Exception {
        double values[] = {
                99.0,
                91.0,
                93.0, // expect exceeded
                96.0,
                15.0, // expect rearm
                98.0,
                98.0,
                98.0 // expect exceeded
        };
        
        setupFetchSequence("cpuUtilization", values);
        
        replayMocks();
        ensureExceededAfterFetches("cpuUtilization", 3);
        ensureRearmedAfterFetches("cpuUtilization", 2);
        ensureExceededAfterFetches("cpuUtilization", 3);
        verifyMocks();
    }

}
