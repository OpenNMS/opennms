/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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
