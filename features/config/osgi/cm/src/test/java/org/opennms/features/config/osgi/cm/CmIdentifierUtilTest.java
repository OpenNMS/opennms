/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.config.osgi.cm;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.features.config.service.api.ConfigUpdateInfo;

public class CmIdentifierUtilTest {

    @Test
    public void shouldParse() {
        checkParse("", "", CmIdentifierUtil.CONFIG_ID);
        checkParse("abc", "abc", CmIdentifierUtil.CONFIG_ID);
        checkParse("abc-def", "abc", "def");
    }

    private void checkParse(String pid, String expectedName, String expectedId) {
        ConfigUpdateInfo id = CmIdentifierUtil.pidToCmIdentifier(pid);
        assertEquals(expectedName, id.getConfigName());
        assertEquals(expectedId, id.getConfigId());
    }

    @Test
    public void shouldCreatePid() {
        checkCreate("abc", "def", "abc-def");
        checkCreate("abc", "default", "abc");
    }

    private void checkCreate(String name, String id, String expectedPid) {
        String pid = CmIdentifierUtil.cmIdentifierToPid(new ConfigUpdateInfo(name, id));
        assertEquals(expectedPid, pid);
    }
}