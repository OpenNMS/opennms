/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2026 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2026 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Test;

public class ConfigFileNamesTest {

    private static final File DIR = new File("/opt/opennms/etc/datacollection");

    @Test
    public void testAcceptsPlainNames() {
        assertEquals(new File(DIR, "cisco.xml"), ConfigFileNames.resolve(DIR, "cisco.xml"));
        assertEquals(new File(DIR, "My_Group-1.xml"), ConfigFileNames.resolve(DIR, "My_Group-1.xml"));
        assertEquals(new File(DIR, "IF-MIB.events.xml"), ConfigFileNames.resolve(DIR, "IF-MIB.events.xml"));
    }

    @Test
    public void testRejectsNamesThatLeaveTheDirectory() {
        for (final String name : new String[] {
                "../users.xml",
                "../../etc/users.xml",
                "..",
                "/etc/passwd.xml",
                "sub/group.xml",
                "..\\users.xml",
                "group\\..\\x.xml",
                ".hidden.xml",
                "a..b.xml",
                "nul\0.xml",
                "",
                null }) {
            try {
                ConfigFileNames.resolve(DIR, name);
                fail("Expected an IllegalArgumentException for the name: " + name);
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }
}
