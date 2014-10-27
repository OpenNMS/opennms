/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.opennms.web.rest.support.TimeChunker;


public class ResolutionTest {

    @Test
    public void testResolution() {
        Date startDate = new Date(new Date().getTime() - 300000);
        long startTime = startDate.getTime();
        long endTime = startDate.getTime() + 300000;
        
        TimeChunker resolution = new TimeChunker(TimeChunker.MINUTE, startDate, new Date(endTime));
        
        assertEquals(1, resolution.getSegmentCount());
        Date startDate1 = resolution.getNextSegment().getStartDate();
        while(resolution.hasNext()) {
            System.err.println("startDate segment1: " + startDate1);
            assertEquals(startDate, startDate1);
        }
    }
    
    @Test
    public void testGetTimeIndex() {
        Date startDate = new Date(new Date().getTime() - 300000);
        long endTime = startDate.getTime() + 300000;
        
        TimeChunker resolution = new TimeChunker(60000, startDate, new Date(endTime));
        Date date = new Date(startDate.getTime() + 150000);
        assertEquals(2, resolution.getIndexContaining(date));
        
    }
}
