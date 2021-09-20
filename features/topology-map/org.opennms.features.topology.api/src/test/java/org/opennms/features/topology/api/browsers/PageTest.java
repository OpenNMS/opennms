/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.browsers;


import org.junit.Assert;
import org.junit.Test;

public class PageTest {

    @Test
    public void testUpdateOffset() {
        OnmsVaadinContainer.Page p = new OnmsVaadinContainer.Page(30, new OnmsVaadinContainer.Size(new OnmsVaadinContainer.SizeReloadStrategy() {
            @Override
            public int reload() {
                return 400;
            }
        }));

        // first page
        Assert.assertFalse(p.updateOffset(0));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(0, p.offset);

        // somewhere in between
        Assert.assertTrue(p.updateOffset(210));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(210 / 30 * 30, p.offset);

        // last page
        Assert.assertTrue(p.updateOffset(399));
        Assert.assertEquals(30, p.length);
        Assert.assertEquals(399 / 30 * 30, p.offset);
    }
}
