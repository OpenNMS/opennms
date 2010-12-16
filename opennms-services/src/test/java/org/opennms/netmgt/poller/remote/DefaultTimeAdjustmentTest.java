/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;


/**
 * DefaultTimeAdjustmentTest
 *
 * @author brozow
 */
public class DefaultTimeAdjustmentTest {
    private static final long m_jitter = 2;
    
    @Test
    public void testNoTimeDifference() {
       
        TimeAdjustment timeAdjustment = new DefaultTimeAdjustment();
        
        long time = System.currentTimeMillis();
        
        long adjusted = timeAdjustment.adjustTimeToMasterTime(time);
        
        assertEqualsWithin(time, adjusted, m_jitter);
    }
    
    @Test
    public void testServerTimeMatches() {
        
        TimeAdjustment timeAdjustment = new DefaultTimeAdjustment();
        
        timeAdjustment.setMasterTime(System.currentTimeMillis());
        
        long time = System.currentTimeMillis();
        
        long adjusted = timeAdjustment.adjustTimeToMasterTime(time);
        
        assertEqualsWithin(time, adjusted, m_jitter);

    }

    @Test
    public void testServerBehind() {
        
        TimeAdjustment timeAdjustment = new DefaultTimeAdjustment();
        
        timeAdjustment.setMasterTime(System.currentTimeMillis()-60000);
        
        long time = System.currentTimeMillis();
        
        long adjusted = timeAdjustment.adjustTimeToMasterTime(time);
        
        assertEqualsWithin(time-60000, adjusted, m_jitter);

    }

    @Test
    public void testServerAhead() {
        
        TimeAdjustment timeAdjustment = new DefaultTimeAdjustment();
        
        timeAdjustment.setMasterTime(System.currentTimeMillis()+60000);
        
        long time = System.currentTimeMillis();
        
        long adjusted = timeAdjustment.adjustTimeToMasterTime(time);
        
        assertEqualsWithin(time+60000, adjusted, m_jitter);

    }

    @Test
    public void testAssertEqualsWithin() {
        assertEqualsWithin(5, 7, 2);
        assertEqualsWithin(7, 5, 2);
        
        try {
            assertEqualsWithin(5, 8, 2);
        } catch (AssertionError e) {
            assertTrue(e != null);
        }
        
        try {
            assertEqualsWithin(8, 5, 2);
        } catch (AssertionError e) {
            assertTrue(e != null);
        }
    }

    private void assertEqualsWithin(final long a, final long b, final long distance) {
        boolean fail = false;
        if (a + distance < b) {
            fail = true;
        } else if (b + distance < a) {
            fail = true;
        }
        if (fail) {
            fail(String.format("%d and %d were not within %d of each other", a, b, distance));
        }
    }

}
