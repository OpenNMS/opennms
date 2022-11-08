/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.features.openconfig.telemetry;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;

public class GnmiPathTest {


    @Test
    public void testGnmiPath()
    {
        String path = "/interfaces/interface[name=Ethernet1][ifIndex=25]/state/counters";
        Gnmi.Path gnmiPath = OpenConfigClientImpl.buildGnmiPath(path);
        Assert.assertEquals(gnmiPath.getElemCount(), 4);
        Assert.assertEquals("interface", gnmiPath.getElemList().get(1).getName());
        Assert.assertEquals("Ethernet1", gnmiPath.getElemList().get(1).getKeyOrDefault("name", "nothing"));
        Assert.assertEquals("25", gnmiPath.getElemList().get(1).getKeyOrDefault("ifIndex", "nothing"));
        path = "/a/b[c=45]/e[d=25]/";
        gnmiPath = OpenConfigClientImpl.buildGnmiPath(path);
        Assert.assertEquals("b", gnmiPath.getElemList().get(1).getName());
        Assert.assertEquals("45", gnmiPath.getElemList().get(1).getKeyOrDefault("c", "nothing"));
        Assert.assertEquals("25", gnmiPath.getElemList().get(2).getKeyOrDefault("d", "nothing"));
        Assert.assertEquals(gnmiPath.getElemCount(), 3);
        path = "/";
        gnmiPath = OpenConfigClientImpl.buildGnmiPath(path);
        Assert.assertEquals(gnmiPath.getElemCount(), 0);

        // Path where there are inner /
        path = "/interfaces/interface[name=Ethernet1/2/3][ifIndex=25]/state/counters";
        gnmiPath = OpenConfigClientImpl.buildGnmiPath(path);
        Assert.assertEquals(gnmiPath.getElemCount(), 4);
        Assert.assertEquals("interface", gnmiPath.getElemList().get(1).getName());
        Assert.assertEquals("Ethernet1/2/3", gnmiPath.getElemList().get(1).getKeyOrDefault("name", "nothing"));
        Assert.assertEquals("25", gnmiPath.getElemList().get(1).getKeyOrDefault("ifIndex", "nothing"));
    }
}
