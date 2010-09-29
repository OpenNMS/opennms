
/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Jan 26: Modified findResults and createGraphResultSet - part of ksc performance improvement. - ayres@opennms.org
 * 2008 Oct 22: Use new ResourceDao methods. - dj@opennms.org
 * 2007 Apr 05: Add the graph offets to the model object. - dj@opennms.org
 * 
 * Created: November 12, 2006
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
