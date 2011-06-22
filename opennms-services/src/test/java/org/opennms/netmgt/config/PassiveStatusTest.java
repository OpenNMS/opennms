/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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
