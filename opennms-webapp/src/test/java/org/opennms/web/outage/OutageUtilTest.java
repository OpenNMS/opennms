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


package org.opennms.web.outage;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opennms.web.outage.filter.AssetFilter;

/**
 * NMS-20383: an outage filter naming an unknown asset column is dropped instead of surfacing an
 * exception (and the attacker-controlled column name) on the outage list page.
 */
public class OutageUtilTest {

    @Test
    public void knownAssetColumnProducesAnAssetFilter() {
        assertTrue(OutageUtil.getFilter("asset.serialNumber=abc", null) instanceof AssetFilter);
    }

    @Test
    public void unknownAssetColumnIsIgnored() {
        assertNull(OutageUtil.getFilter("asset.not_a_column=abc", null));
        assertNull(OutageUtil.getFilter("asset.serialNumber) OR 1=1--=abc", null));
    }
}
