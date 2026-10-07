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

package org.opennms.web.rest.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

public class WriteOnlyPropertyGuardTest {

    @Test
    public void detectsWriteOnlyAssetProperties() {
        assertTrue(WriteOnlyPropertyGuard.isWriteOnly("assetRecord.password"));
        assertTrue(WriteOnlyPropertyGuard.isWriteOnly("assetRecord.enable"));
        assertTrue(WriteOnlyPropertyGuard.isWriteOnly("assetRecord.snmpcommunity"));
        assertTrue(WriteOnlyPropertyGuard.isWriteOnly("node.assetRecord.password"));
        assertTrue(WriteOnlyPropertyGuard.isWriteOnly(" AssetRecord.SnmpCommunity "));
    }

    @Test
    public void allowsOtherProperties() {
        assertFalse(WriteOnlyPropertyGuard.isWriteOnly(null));
        assertFalse(WriteOnlyPropertyGuard.isWriteOnly("password"));
        assertFalse(WriteOnlyPropertyGuard.isWriteOnly("assetRecord.description"));
        assertFalse(WriteOnlyPropertyGuard.isWriteOnly("assetRecord.username"));
        assertFalse(WriteOnlyPropertyGuard.isWriteOnly("snmpInterface.enable"));
    }

    @Test
    public void checkRejectsWithBadRequest() {
        WriteOnlyPropertyGuard.check("assetRecord.description");
        try {
            WriteOnlyPropertyGuard.check("assetRecord.password");
            fail("expected WebApplicationException");
        } catch (final WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }
}
