
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author <a href="mailto:seth@opennms.org">Seth</a>
 */
public class DefaultGraphResultsServiceTest {

    /**
     * Test the parseResourceId() function, mainly to make sure that the 
     * issue in bug 3366 can be tested some.
     */
    @Test
    public void testParseResourceId() {
        String[] values = DefaultGraphResultsService.parseResourceId("node[1].responseTime[127.0.0.1]");
        assertEquals("node[1]", values[0]);
        assertEquals("responseTime", values[1]);
        assertEquals("127.0.0.1", values[2]);
    }

    /**
     * Ensure that invalid resource IDs return a null string array.
     */
    @Test
    public void testUnparsableeResourceId() {
        String[] values = DefaultGraphResultsService.parseResourceId("node[1.responseTime[127.0.0.1]");
        assertNull(values);
    }
}
