/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.rrd.RrdUtils;

public class LatencyThresholderIntegrationTest extends ThresholderTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        MockLogAppender.setupLogging();
        
        setupDatabase();
        
        createMockRrd();

        setupEventManager();

        replayMocks();

        String dirName = "target/threshd-test/192.168.1.1";
        String fileName = "icmp"+RrdUtils.getExtension();
        int nodeId = 1;
        String ipAddress = "192.168.1.1";
        String serviceName = "ICMP";
        String groupName = "icmp-latency";

		setupThresholdConfig(dirName, fileName, nodeId, ipAddress, serviceName, groupName);

        m_thresholder = new LatencyThresholder();
        m_thresholder.initialize(m_serviceParameters);
        m_thresholder.initialize(m_iface, m_parameters);

        verifyMocks();
        expectRrdStrategyCalls();
    }

    @Override
	protected void tearDown() throws Exception {
        RrdUtils.setStrategy(null);
        MockLogAppender.assertNoWarningsOrGreater();
        super.tearDown();
    }
    
    public void xtestIcmpDouble() throws Exception {
        setupFetchSequence("icmp-double", new double[] { 69000.0, 79000.0, 74999.0, 74998.0 });
        replayMocks();
        ensureExceededAfterFetches("icmp-double", 3);
        verifyMocks();
    }
    
    public void testNormalValue() throws Exception {
        setupFetchSequence("icmp", new double[] { 69000.0, 79000.0, 74999.0, 74998.0 });
		
        replayMocks();
        ensureNoEventAfterFetches("icmp", 4);
        verifyMocks();
    }
    
    public void testBigValue() throws Exception {
        setupFetchSequence("icmp", new double[] {79000.0, 80000.0, 84999.0, 84998.0, 97000.0 });
        
        replayMocks();
        ensureExceededAfterFetches("icmp", 3);
        ensureNoEventAfterFetches("icmp", 2);
        verifyMocks();
    }
    
    public void testRearm() throws Exception {
        double values[] = { 
                79000.0,
                80000.0,
                84999.0, // expect exceeded
                84998.0,
                15000.0, // expect rearm
                77000.0,
                77000.0,
                77000.0 // expect exceeded
        };
        
        setupFetchSequence("icmp", values);
        
        replayMocks();
        ensureExceededAfterFetches("icmp", 3);
        ensureRearmedAfterFetches("icmp", 2);
        ensureExceededAfterFetches("icmp", 3);
        verifyMocks();
    }
}
