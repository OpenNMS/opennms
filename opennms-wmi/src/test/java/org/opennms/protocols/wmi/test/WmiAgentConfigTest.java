/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
        @Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * Tear down simply resets the mock object.
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
        @Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    /**
	 * Test that the isValidMatchType object works for the expected values of
	 * "all", "none", "some" and "one" but does not work for other arbitrary values.
	 *
	 * Test method for
	 * {@link org.opennms.netmgt.config.wmi.WmiAgentConfig#isValidMatchType(java.lang.String)}.
	 */
	public final void testIsValidMatchType() {

    }

}
