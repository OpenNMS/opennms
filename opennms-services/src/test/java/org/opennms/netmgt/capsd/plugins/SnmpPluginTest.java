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
// 2008 Oct 26: Use SnmpTestSuiteUtils to create our TestSuite since the same thing is used in other classes. - dj@opennms.org
// 2007 Apr 06: Make the tests not complain when a host doesn't have an agent. - dj@opennms.org
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
package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpTestSuiteUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class SnmpPluginTest extends OpenNMSTestCase {

    /*
     * FIXME: Assertions are not checked
     * 
     * Set this flag to false before checking in code.  Use this flag to
     * test against a v3 compatible agent running on the localhost
     * until the MockAgent code is finished.
     */
    private boolean m_runAssertions = false;
    
    private SnmpPlugin m_plugin = null;
    
    public static TestSuite suite() {
        return SnmpTestSuiteUtils.createSnmpStrategyTestSuite(SnmpPluginTest.class);
    }

    /**
     * Required method for TestCase
     */
    protected void setUp() throws Exception {
        assertNotNull("The org.opennms.snmp.strategyClass must be set to run this test", System.getProperty("org.opennms.snmp.strategyClass"));
        super.setUp();
        if (m_plugin == null) {
            m_plugin = new SnmpPlugin();
        }
        m_runSupers = false;
    }

    /**
     * Required method for TestCase
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
            
    /**
     * This test works against a live v1/2c compatible agent until
     * the MockAgent code is completed.
     * @throws UnknownHostException 
     */
    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(myLocalHost());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("forced version", "snmpv1");
        
        if (m_runAssertions) {
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(address, map));
        }
    }

    /**
     * This test works against a live v1/2c compatible agent until
     * the MockAgent code is completed.
     * @throws UnknownHostException 
     */
    public void testIsExpectedValue() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(myLocalHost());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("vbvalue", "\\.1\\.3\\.6\\.1\\.4\\.1.*");
        
        if (m_runAssertions) {
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(address, map));
        }
    }
    
    /*
     * Class under test for boolean isProtocolSupported(InetAddress)
     */
    public final void testIsProtocolSupportedInetAddress() throws UnknownHostException {
        if (m_runAssertions) {
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(InetAddress.getByName(myLocalHost())));
        }
    }
    
    public final void testIsV3ProtocolSupported() throws ValidationException, IOException, IOException, MarshalException {
        setVersion(SnmpAgentConfig.VERSION3);
        final Resource rdr = new ByteArrayResource(getSnmpConfig().getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));

        if (m_runAssertions) {
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(InetAddress.getByName(myLocalHost())));
        }
    }

    public final void testIsV3ForcedToV1Supported() throws ValidationException, IOException, IOException, MarshalException {
        setVersion(SnmpAgentConfig.VERSION3);
        final Resource rdr = new ByteArrayResource(getSnmpConfig().getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rdr));
        
        Map<String, Object> qualifiers = new HashMap<String, Object>();
        qualifiers.put("force version", "snmpv1");

        if (m_runAssertions) {
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(InetAddress.getByName(myLocalHost()), qualifiers));
        }
    }

}
