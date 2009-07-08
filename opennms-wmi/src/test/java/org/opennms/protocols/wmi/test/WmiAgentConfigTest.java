/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.protocols.wmi.test;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: CE136452
 * Date: Sep 17, 2008
 * Time: 7:29:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class WmiAgentConfigTest  extends TestCase {
    	/*
	 * Create a placeholder mock object. We will reset() this in each test
	 * so that we can reuse it.
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Tear down simply resets the mock object.
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    /**
	 * Test that the isValidMatchType object works for the expected values of
	 * "all", "none", "some" and "one" but does not work for other arbitrary values.
	 *
	 * Test method for
	 * {@link org.opennms.protocols.wmi.WmiAgentConfig#isValidMatchType(java.lang.String)}.
	 */
	public final void testIsValidMatchType() {

    }

}
