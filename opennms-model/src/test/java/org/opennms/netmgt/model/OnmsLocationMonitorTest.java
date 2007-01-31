/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
 * reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included
 * code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.model;

import junit.framework.TestCase;

public class OnmsLocationMonitorTest extends TestCase {
    public void testCompareToEqual() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setDefinitionName("a");
        o1.setId(1);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setDefinitionName("a");
        o2.setId(1);
        
        assertEquals("compareTo should return 0 as the two objects are equal", 0, o1.compareTo(o2));
    }
    
    public void testCompareToFirstHigherId() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setDefinitionName("a");
        o1.setId(2);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setDefinitionName("a");
        o2.setId(1);
        
        assertEquals("compareTo should return 1 as the first object has a higher ID", 1, o1.compareTo(o2));
    }
    
    public void testCompareToFirstLowerId() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setDefinitionName("a");
        o1.setId(1);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setDefinitionName("a");
        o2.setId(2);
        
        assertEquals("compareTo should return 1 as the first object has a lower ID", -1, o1.compareTo(o2));
    }
    
    public void testCompareToFirstHigherName() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setDefinitionName("b");
        o1.setId(1);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setDefinitionName("a");
        o2.setId(1);
        
        assertEquals("compareTo should return 1 as the first object has a higher name", 1, o1.compareTo(o2));
    }
    
    public void testCompareToFirstLowerName() {
        OnmsLocationMonitor o1 = new OnmsLocationMonitor();
        o1.setDefinitionName("a");
        o1.setId(1);
        
        OnmsLocationMonitor o2 = new OnmsLocationMonitor();
        o2.setDefinitionName("b");
        o2.setId(1);
        
        assertEquals("compareTo should return 1 as the first object has a lower name", -1, o1.compareTo(o2));
    }
}
