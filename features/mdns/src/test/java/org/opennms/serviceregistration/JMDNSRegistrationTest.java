/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.serviceregistration;

import java.util.Hashtable;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.opennms.serviceregistration.strategies.JMDNSStrategy;

public class JMDNSRegistrationTest extends TestCase {
	private ServiceRegistrationStrategy a;
	private static int timeout = 8000;

	@Before
	public void setUp() throws Exception {
	    a = new JMDNSStrategy();
	}

	@Test
	public void testMdnsShort() throws Exception {
		a.initialize("http", "Short Test", 1010);
		a.register();
		Thread.sleep(timeout);
		a.unregister();
	}
	
    @Test
	public void testMdnsLong() throws Exception {
		Hashtable<String,String> properties = new Hashtable<String,String>();
		properties.put("path", "/opennms");
		a.initialize("http", "Long Test", 1011, properties);
		a.register();
		Thread.sleep(timeout);
		a.unregister();
	}
}
