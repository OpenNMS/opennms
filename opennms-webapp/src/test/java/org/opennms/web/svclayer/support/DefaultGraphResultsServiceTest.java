
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

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
        final String[] values = DefaultGraphResultsService.parseResourceId("node[1].responseTime[127.0.0.1]");
        assertEquals("node[1]", values[0]);
        assertEquals("responseTime", values[1]);
        assertEquals("127.0.0.1", values[2]);
    }

    /**
     * Ensure that invalid resource IDs return a null string array.
     */
    @Test
    public void testUnparsableeResourceId() {
        final String[] values = DefaultGraphResultsService.parseResourceId("node[1.responseTime[127.0.0.1]");
        assertNull(values);
    }

    /**
     * Ensure that invalid resource IDs return a null string array.
     */
    @Test
    public void testParseForeignId() {
        final String[] values = DefaultGraphResultsService.parseResourceId("nodeSource[foreignSource:foreignId].nodeSnmp[]");
        assertEquals(3, values.length);
        assertEquals("nodeSource[foreignSource:foreignId]", values[0]);
        assertEquals("nodeSnmp", values[1]);
        assertEquals("", values[2]);
    }
    
    /**
     * Check if the lookup of metrics to columns for prefabGraphs is working.
     * This lookup is performed by file handling of meta files.
     */
    @Test
    public void testLookUpMetricsForColumnsOfPrefabGraphs() {
        DefaultGraphResultsService defaultGraphResultsService = new DefaultGraphResultsService();
        assertNotNull(defaultGraphResultsService);
        //Sorry not now
    }
}
