/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.config;

import junit.framework.TestCase;

public class PassiveStatusTest extends TestCase {

    /*
     * Test method for 'org.opennms.netmgt.config.PassiveStatus.equals(Object)'
     */
    public void testEqualsObject() {
        PassiveStatusKey ps = new PassiveStatusKey("node1", "1.1.1.1", "ICMP");
        PassiveStatusKey ps2 = new PassiveStatusKey("node1", "2.1.1.1", "HTTP");
        PassiveStatusKey ps3 = new PassiveStatusKey("node1", "1.1.1.1", "ICMP");
        
        assertEquals(ps, ps3);
        assertFalse(ps.equals(ps2));
        assertFalse(ps2.equals(ps3));


    }

}
