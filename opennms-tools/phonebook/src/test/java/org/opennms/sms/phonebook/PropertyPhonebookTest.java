/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.sms.phonebook;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.sms.phonebook.PropertyPhonebook;

public class PropertyPhonebookTest {

    @Test
    public void testPropertyFile() throws Exception {
        String fileName = getClass().getResource("/phonebook-test.properties").getFile();
        PropertyPhonebook p = new PropertyPhonebook();
        p.setPropertyFile(fileName);
        
        assertEquals("919-533-0160", p.getTargetForAddress("192.168.0.1"));
        assertEquals("877-666-7911", p.getTargetForAddress("192.168.0.2"));
        assertEquals("sms@example.com", p.getTargetForAddress("192.168.0.3"));
    }

    @Test
    public void testResourceFile() throws Exception {
        PropertyPhonebook p = new PropertyPhonebook();
        p.setPropertyFile("phonebook-test.properties");
        
        assertEquals("919-533-0160", p.getTargetForAddress("192.168.0.1"));
        assertEquals("877-666-7911", p.getTargetForAddress("192.168.0.2"));
        assertEquals("sms@example.com", p.getTargetForAddress("192.168.0.3"));
    }

}
