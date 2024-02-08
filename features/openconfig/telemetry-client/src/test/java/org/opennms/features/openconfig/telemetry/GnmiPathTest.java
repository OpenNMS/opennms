/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.openconfig.telemetry;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;

public class GnmiPathTest {


    @Test
    public void testGnmiPath()
    {
        String path = "/interfaces/interface[name=Ethernet1][ifIndex=25]/state/counters";
        Gnmi.Path gnmiPath = OpenConfigClientImpl.buildGnmiPath(path, null);
        Assert.assertEquals(gnmiPath.getElemCount(), 4);
        Assert.assertEquals("interface", gnmiPath.getElemList().get(1).getName());
        Assert.assertEquals("Ethernet1", gnmiPath.getElemList().get(1).getKeyOrDefault("name", "nothing"));
        Assert.assertEquals("25", gnmiPath.getElemList().get(1).getKeyOrDefault("ifIndex", "nothing"));
        path = "/a/b[c=45]/e[d=25]/";
        gnmiPath = OpenConfigClientImpl.buildGnmiPath(path, null);
        Assert.assertEquals("b", gnmiPath.getElemList().get(1).getName());
        Assert.assertEquals("45", gnmiPath.getElemList().get(1).getKeyOrDefault("c", "nothing"));
        Assert.assertEquals("25", gnmiPath.getElemList().get(2).getKeyOrDefault("d", "nothing"));
        Assert.assertEquals(gnmiPath.getElemCount(), 3);
        path = "/";
        gnmiPath = OpenConfigClientImpl.buildGnmiPath(path, null);
        Assert.assertEquals(gnmiPath.getElemCount(), 0);

        // Path where there are inner /
        path = "/interfaces/interface[name=Ethernet1/2/3][ifIndex=25]/state/counters";
        gnmiPath = OpenConfigClientImpl.buildGnmiPath(path, null);
        Assert.assertEquals(gnmiPath.getElemCount(), 4);
        Assert.assertEquals("interface", gnmiPath.getElemList().get(1).getName());
        Assert.assertEquals("Ethernet1/2/3", gnmiPath.getElemList().get(1).getKeyOrDefault("name", "nothing"));
        Assert.assertEquals("25", gnmiPath.getElemList().get(1).getKeyOrDefault("ifIndex", "nothing"));
    }
}
