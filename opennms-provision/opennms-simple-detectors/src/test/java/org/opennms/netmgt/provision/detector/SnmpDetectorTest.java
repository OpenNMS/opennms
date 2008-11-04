/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector;

import org.junit.Test;



public class SnmpDetectorTest {
    /*
     * FIXME: Assertions are not checked
     * 
     * Set this flag to false before checking in code.  Use this flag to
     * test against a v3 compatible agent running on the localhost
     * until the MockAgent code is finished.
     */
    
    @Test
    public void testBogus() {
        
    }
//    private boolean m_runAssertions = false;
//    
//    private SnmpDetector m_detector = null;
//    
//    public static TestSuite suite() {
//        return SnmpTestSuiteUtils.createSnmpStrategyTestSuite(SnmpPluginTest.class);
//    }
//
//    /**
//     * Required method for TestCase
//     */
//    protected void setUp() throws Exception {
//        assertNotNull("The org.opennms.snmp.strategyClass must be set to run this test", System.getProperty("org.opennms.snmp.strategyClass"));
//        super.setUp();
//        if (m_detector == null) {
//            m_detector = new SnmpDetector();
//        }
//        m_runSupers = false;
//    }
//
//    /**
//     * Required method for TestCase
//     */
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }
//            
//    /**
//     * This test works against a live v1/2c compatible agent until
//     * the MockAgent code is completed.
//     * @throws UnknownHostException 
//     */
//    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
//        InetAddress address = InetAddress.getByName(myLocalHost());
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("forced version", "snmpv1");
//        
//        m_detector.setForceVersion("snmpv1");
//        if (m_runAssertions) {
//            assertTrue("protocol is not supported", m_detector.isServiceDetected(address, new NullDetectorMonitor()));
//        }
//    }
//
//    /**
//     * This test works against a live v1/2c compatible agent until
//     * the MockAgent code is completed.
//     * @throws UnknownHostException 
//     */
//    public void testIsExpectedValue() throws UnknownHostException {
//        InetAddress address = InetAddress.getByName(myLocalHost());
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("vbvalue", "\\.1\\.3\\.6\\.1\\.4\\.1.*");
//        m_detector.setVbvalue("\\.1\\.3\\.6\\.1\\.4\\.1.*");
//        if (m_runAssertions) {
//            assertTrue("protocol is not supported", m_detector.isServiceDetected(address, new NullDetectorMonitor()));
//        }
//    }
//    
//    /*
//     * Class under test for boolean isProtocolSupported(InetAddress)
//     */
//    public final void testIsProtocolSupportedInetAddress() throws UnknownHostException {
//        if (m_runAssertions) {
//            assertTrue("protocol is not supported", m_detector.isServiceDetected(InetAddress.getByName(myLocalHost()), new NullDetectorMonitor()));
//        }
//    }
//    
//    public final void testIsV3ProtocolSupported() throws ValidationException, IOException, IOException, MarshalException {
//        setVersion(SnmpAgentConfig.VERSION3);
//        Reader rdr = new StringReader(getSnmpConfig());
//        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
//        
//        m_detector.setAgentConfig(SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(myLocalHost())));
//        
//        if (m_runAssertions) {
//            assertTrue("protocol is not supported", m_detector.isServiceDetected(InetAddress.getByName(myLocalHost()), new NullDetectorMonitor()));
//        }
//    }
//
//    public final void testIsV3ForcedToV1Supported() throws ValidationException, IOException, IOException, MarshalException {
//        setVersion(SnmpAgentConfig.VERSION3);
//        Reader rdr = new StringReader(getSnmpConfig());
//        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
//        
//        m_detector.setAgentConfig(SnmpPeerFactory.getInstance().getAgentConfig(InetAddress.getByName(myLocalHost())));
//        
//        Map<String, Object> qualifiers = new HashMap<String, Object>();
//        qualifiers.put("force version", "snmpv1");
//        
//        m_detector.setForceVersion("snmpv1");
//
//        if (m_runAssertions) {
//            assertTrue("protocol is not supported", m_detector.isServiceDetected(InetAddress.getByName(myLocalHost()), new NullDetectorMonitor()));
//        }
//    }
}
