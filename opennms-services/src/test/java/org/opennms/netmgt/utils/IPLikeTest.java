/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.core.utils.IPLike;

public class IPLikeTest {

    @Test
    public void testCountChar() {
        assertEquals(2, IPLike.countChar('-', "test-this-please"));
        assertEquals(3, IPLike.countChar('-', "test-this-please-"));
        assertEquals(4, IPLike.countChar('-', "-test-this-please-"));
    }
    
    @Test
    public void testMatchRange() {
        assertTrue(IPLike.matchRange("192", "191-193"));
        assertTrue(IPLike.matchRange("192", "192"));
        assertTrue(IPLike.matchRange("192", "192-200"));
        assertTrue(IPLike.matchRange("192", "1-255"));
        assertTrue(IPLike.matchRange("192", "*"));
        assertFalse(IPLike.matchRange("192", "1-9"));
    }
    
    @Test
    public void testMatchOctet() {
        assertTrue(IPLike.matchNumericListOrRange("192", "191,192,193"));
        assertFalse(IPLike.matchNumericListOrRange("192", "190,191,194"));
        assertTrue(IPLike.matchNumericListOrRange("192", "10,172,190-193"));
        assertFalse(IPLike.matchNumericListOrRange("192", "10,172,193-199"));
        assertTrue(IPLike.matchNumericListOrRange("205", "200-300,400-500"));
        assertTrue(IPLike.matchNumericListOrRange("405", "200-300,400-500"));
        assertFalse(IPLike.matchNumericListOrRange("505", "200-300,400-500"));
    }
    
    @Test
    public void testVerifyIpMatch() {
        assertTrue(IPLike.matches("192.168.0.1", "*.*.*.*"));
        assertTrue(IPLike.matches("192.168.0.1", "192.*.*.*"));
        assertTrue(IPLike.matches("192.168.0.1", "*.168.*.*"));
        assertTrue(IPLike.matches("192.168.0.1", "*.*.0.*"));
        assertTrue(IPLike.matches("192.168.0.1", "*.*.*.1"));
        assertTrue(IPLike.matches("192.168.0.1", "*.*.*.0-7"));
        assertTrue(IPLike.matches("192.168.0.1", "192.168.0.0-7"));
        assertTrue(IPLike.matches("192.168.0.1", "192.166,167,168.*.0,1,5-10"));
        assertFalse(IPLike.matches("192.168.0.1", "10.0.0.1"));
        assertFalse(IPLike.matches("192.168.0.1", "*.168.*.2"));
        assertFalse(IPLike.matches("192.168.0.1", "10.168.0.1"));
        assertTrue(IPLike.matches("10.1.1.1", "10.1.1.1"));
        
    }
    

}
