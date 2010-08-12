/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 06: The default manager is now DefaultCapsdConfigManager. - dj@opennms.org
 * 2007 Apr 20: Created this file. - dj@opennms.org
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.Reader;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.capsd.Property;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.test.ConfigurationTestUtils;
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
        Reader reader = ConfigurationTestUtils.getReaderForResource(this, "/org/opennms/netmgt/config/capsd-configuration-bad-class.xml");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new ValidationException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            new DefaultCapsdConfigManager(reader);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();

        // This last assert here would fail if the constructor didn't throw an exception
//        ProtocolInfo[] plugins = m_factory.getProtocolSpecification(InetAddress.getByName("127.0.0.1"));
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
