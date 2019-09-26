/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.rest.internal.classification;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class FilenameHelperTest {

    @Test
    public void shouldAllowOnlyValidFileNames() {
        FilenameHelper h = new FilenameHelper();
        assertTrue(h.isValidFileName("x"));
        assertTrue(h.isValidFileName("xY"));
        assertTrue(h.isValidFileName("xY"));
        assertTrue(h.isValidFileName("x_Y-"));
        assertTrue(h.isValidFileName("x_Y -"));
        assertTrue(h.isValidFileName("aa.csv"));
        assertTrue(h.isValidFileName("9"));
        assertTrue("Default Filename should be accepted", h.isValidFileName(h.createFilenameForGroupExport(1, null)));
        assertFalse(h.isValidFileName(" aa.csv"));
        assertFalse(h.isValidFileName("aa "));
        assertFalse(h.isValidFileName(null));
        assertFalse(h.isValidFileName(""));
        assertFalse(h.isValidFileName(" "));
        assertFalse(h.isValidFileName("          "));
        assertFalse(h.isValidFileName("ä"));
        assertFalse(h.isValidFileName("a%"));
        assertFalse(h.isValidFileName("%"));

    }

    @Test
    public void verifyDefaultFileNameForGroupExport(){
        FilenameHelper helper = new FilenameHelper();
        assertEquals("1_rules.csv", helper.createFilenameForGroupExport(1, null));
        assertEquals("1_rules.csv", helper.createFilenameForGroupExport(1, ""));
    }

    @Test
    public void verifyCustomFileNameForGroupExport(){
        FilenameHelper helper = new FilenameHelper();
        assertEquals("abc.csv", helper.createFilenameForGroupExport(1, "abc.csv"));
        assertEquals("hello bello.csv", helper.createFilenameForGroupExport(1, "hello bello.csv"));
    }
}