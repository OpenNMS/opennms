/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.nsclient.capsd;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.protocols.nsclient.AbstractNsclientTest;
import org.opennms.protocols.nsclient.capsd.NsclientPlugin;

/**
 * <p>JUnit Test Class for NsclientPlugin.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NsclientPluginTest extends AbstractNsclientTest {

    @Test
    public void testPluginSuccess() throws Exception {
        startServer("None&1", "NSClient++ 0.3.8.75 2010-05-27");
        NsclientPlugin plugin = new NsclientPlugin();
        Map<String, Object> qualifiers = new HashMap<String, Object>();
        qualifiers.put("port", getServer().getLocalPort());
        Assert.assertTrue(plugin.isProtocolSupported(getServer().getInetAddress(), qualifiers));
        stopServer();
    }

    @Test
    public void testPluginFail() throws Exception {
        startServer("None&1", "ERROR: I don't know what you mean");
        NsclientPlugin plugin = new NsclientPlugin();
        Map<String, Object> qualifiers = new HashMap<String, Object>();
        qualifiers.put("port", getServer().getLocalPort());
        Assert.assertFalse(plugin.isProtocolSupported(getServer().getInetAddress(), qualifiers));
        stopServer();
    }

}
