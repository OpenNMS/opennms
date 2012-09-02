/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.InputStream;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.exolab.castor.xml.ValidationException;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.netmgt.config.capsd.Property;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.test.ThrowableAnticipator;

/**
 * Test for CapsdConfigManager.
 * 
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 * @see CapsdConfigManager
 */
public class CapsdConfigManagerTest extends TestCase {
    
    public void testBogus() {
        // Don't do anything... this is a place holder so we have at least one test
    }
    
    /*
     * This is disabled because the plugin instantiation isn't done in
     * CapsdConfigManager anymore.  It's now in PluginManager.
     */ 
    /**
     * Make sure that the constructor throws an exception when one of the
     * plugins cannot be loaded.
     */
    public void DISABLEDtestBadPlugin() throws Exception {
        InputStream reader = ConfigurationTestUtils.getInputStreamForResource(this, "/org/opennms/netmgt/config/capsd-configuration-bad-class.xml");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new ValidationException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            new DefaultCapsdConfigManager(reader);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();

        // This last assert here would fail if the constructor didn't throw an exception
//        ProtocolInfo[] plugins = m_factory.getProtocolSpecification(InetAddressUtils.addr("127.0.0.1"));
//
//        assertNotNull("plugin list", plugins);
//        assertEquals("plugin list size", 1, plugins.length);
//        
//        ProtocolInfo plugin = plugins[0];
//        assertNotNull("PluginInfo object for plugin zero", plugin);
//        
//        assertNotNull("plugin for zero", plugin.getPlugin());
    }
    
    public final void testHttpRegex() throws Exception {
        CapsdConfigManager config = new DefaultCapsdConfigManager(Thread.currentThread().getContextClassLoader().getResourceAsStream("testHttpRegex.xml"));
        ProtocolPlugin http = config.getProtocolPlugin("HTTP");
        Property regexProperty = null;
        for (Property prop : http.getPropertyCollection()) {
            if ("response-text".equals(prop.getKey())) {
                regexProperty = prop;
                break;
            }
        }
        assertEquals("HTTP regex does not match expected value", "~\\{.nodes.: \\[\\{.nodeid.:.*", regexProperty.getValue());

        // This code approximates how this parameter is used inside
        // {@link HttpPlugin#checkResponseBody(ConnectionConfig config, String response)
        Pattern bodyPat = Pattern.compile(regexProperty.getValue().substring(1), Pattern.DOTALL);
        assertTrue(bodyPat.matcher("{\"nodes\": [{\"nodeid\": \"19\", \"nodelabel\": \"\"},{\"nodeid\": \"21\", \"nodelabel\": \"\"}]}").matches());
        assertFalse(bodyPat.matcher("{\"nodes\": [ ]}").matches());
    }
}
