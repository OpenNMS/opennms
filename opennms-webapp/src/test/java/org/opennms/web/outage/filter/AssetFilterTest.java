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

package org.opennms.web.outage.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * NMS-20383: the outage AssetFilter splices the asset column identifier into SQL and cannot bind it
 * as a parameter, so the column must be validated against the known asset schema. This test confirms
 * a real column is accepted (and its value is still bound) while an unknown or injected column is
 * rejected before any SQL is produced.
 */
public class AssetFilterTest {

    @Test
    public void acceptsAKnownAssetColumnAndBindsTheValue() {
        final AssetFilter filter = new AssetFilter("asset.serialNumber", "abc123");
        assertTrue("column must appear in the SQL", filter.getSQLTemplate().contains("assets.serialnumber="));
        // value is bound, not inlined: JDBC template uses a placeholder, Hibernate path uses '?'
        assertTrue(filter.getSQLTemplate().contains("=%s)"));
        assertFalse(filter.getSQLTemplate().contains("abc123"));
        assertNotNull(filter.getCriterion());
    }

    @Test
    public void acceptsColumnsOutsideTheUiListAndIgnoresCase() {
        for (final String column : new String[] { "managedObjectType", "managedobjectinstance", "SERIALNUMBER", "city" }) {
            assertTrue(column + " must be an allowed asset column", AssetFilter.isValidAssetColumn(column));
            new AssetFilter(AssetFilter.TYPE + column, "x");
        }
    }

    @Test
    public void rejectsAFieldWithoutTheAssetPrefix() {
        // "asset." used to be stripped as a regex, so "assetXserialNumber" slipped through as serialNumber
        assertThrowsIllegalArgument("assetXserialNumber");
        assertThrowsIllegalArgument("serialNumber");
        assertThrowsIllegalArgument(null);
    }

    @Test
    public void rejectsSqlInjectionInTheColumn() {
        for (final String malicious : new String[] {
                "asset.serialNumber=1) OR true--",
                "asset.nodeid) OR 1=1--",
                "asset.serialNumber,(SELECT 1)",
        }) {
            assertThrowsIllegalArgument(malicious);
        }
    }

    private static void assertThrowsIllegalArgument(final String field) {
        try {
            new AssetFilter(field, "x");
            fail("expected field to be rejected: " + field);
        } catch (final IllegalArgumentException expected) {
            // correct: only real asset columns, carrying the asset. prefix, are allowed
        }
    }
}
