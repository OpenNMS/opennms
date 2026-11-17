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

package org.opennms.web.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class AngularJsUtilsTest {

    @Test
    public void keepsCommonValues() {
        assertEquals("node[fs:fid].interfaceSnmp[eth0-001122334455]", AngularJsUtils.escapeString("node[fs:fid].interfaceSnmp[eth0-001122334455]"));
        assertEquals("Response Time for 192.0.2.1", AngularJsUtils.escapeString("Response Time for 192.0.2.1"));
        assertEquals("", AngularJsUtils.escapeString(null));
    }

    @Test
    public void escapesCharactersThatBreakOutOrInterpolate() {
        final String encoded = AngularJsUtils.escapeString("x'\"<>&{{constructor.constructor('alert(1)')()}}\\");
        for (final String unsafe : new String[] {"'", "\"", "<", ">", "&", "{", "}"}) {
            assertFalse(unsafe, encoded.contains(unsafe));
        }
        assertEquals("\\u0027\\u007b\\u007d\\u005c", AngularJsUtils.escapeString("'{}\\"));
    }
}
