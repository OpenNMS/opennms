/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestSuite;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.test.snmp.SnmpTestSuiteUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.core.io.ByteArrayResource;

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
    @Override
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
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
            
    /**
     * This test works against a live v1/2c compatible agent until
     * the MockAgent code is completed.
     * @throws UnknownHostException 
     */
    public void testIsForcedV1ProtocolSupported() throws UnknownHostException {
        InetAddress address = myLocalHost();
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
        InetAddress address = myLocalHost();
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
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(myLocalHost()));
        }
    }
    
    public final void testIsV3ProtocolSupported() throws ValidationException, IOException, IOException, MarshalException {
        setVersion(SnmpAgentConfig.VERSION3);
        ByteArrayResource rsrc = new ByteArrayResource(getSnmpConfig().getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rsrc));

        if (m_runAssertions) {
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(myLocalHost()));
        }
    }

    public final void testIsV3ForcedToV1Supported() throws ValidationException, IOException, IOException, MarshalException {
        setVersion(SnmpAgentConfig.VERSION3);
        ByteArrayResource rsrc = new ByteArrayResource(getSnmpConfig().getBytes());
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(rsrc));
        
        Map<String, Object> qualifiers = new HashMap<String, Object>();
        qualifiers.put("force version", "snmpv1");

        if (m_runAssertions) {
            assertTrue("protocol is not supported", m_plugin.isProtocolSupported(myLocalHost(), qualifiers));
        }
    }

}
