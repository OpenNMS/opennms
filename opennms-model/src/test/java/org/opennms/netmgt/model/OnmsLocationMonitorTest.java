/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.util.UUID;

import junit.framework.TestCase;

public class OnmsLocationMonitorTest extends TestCase {

    private static final String LOCATION_MONITOR_ID_A = "A" + UUID.randomUUID().toString();
    private static final String LOCATION_MONITOR_ID_B = "B" + UUID.randomUUID().toString();

    public void testCompareToEqual() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setLocation("a");
        o1.setId(LOCATION_MONITOR_ID_A);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setLocation("a");
        o2.setId(LOCATION_MONITOR_ID_A);
        
        assertEquals("compareTo should return 0 as the two objects are equal", 0, o1.compareTo(o2));
    }
    
    public void testCompareToFirstHigherId() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setLocation("a");
        o1.setId(LOCATION_MONITOR_ID_B);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setLocation("a");
        o2.setId(LOCATION_MONITOR_ID_A);
        
        assertEquals("compareTo should return 1 as the first object has a higher ID", 1, o1.compareTo(o2));
    }
    
    public void testCompareToFirstLowerId() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setLocation("a");
        o1.setId(LOCATION_MONITOR_ID_A);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setLocation("a");
        o2.setId(LOCATION_MONITOR_ID_B);
        
        assertEquals("compareTo should return 1 as the first object has a lower ID", -1, o1.compareTo(o2));
    }
    
    public void testCompareToFirstHigherName() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setLocation("b");
        o1.setId(LOCATION_MONITOR_ID_A);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setLocation("a");
        o2.setId(LOCATION_MONITOR_ID_A);
        
        assertEquals("compareTo should return 1 as the first object has a higher name", 1, o1.compareTo(o2));
    }
    
    public void testCompareToFirstLowerName() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setLocation("a");
        o1.setId(LOCATION_MONITOR_ID_A);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setLocation("b");
        o2.setId(LOCATION_MONITOR_ID_A);
        
        assertEquals("compareTo should return 1 as the first object has a lower name", -1, o1.compareTo(o2));
    }
}
