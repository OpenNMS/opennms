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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.junit.Test;

@org.junit.Ignore("this test reaches out to the internet, we should change it to use a fake/test server")
public class WhoIsClientTest {


    @Test
    public void testWhoIsAsn() throws IOException {

        Optional<AsnInfo> output = BmpWhoIsClient.getAsnInfo(701L);
        assertTrue(output.isPresent());
        output = BmpWhoIsClient.getAsnInfo(33353L);
        assertTrue(output.isPresent());
        output = BmpWhoIsClient.getAsnInfo(132827L);
        assertTrue(output.isPresent());
        output = BmpWhoIsClient.getAsnInfo(5650L);
        assertTrue(output.isPresent());
        assertEquals("US", output.get().getCountry());
        output = BmpWhoIsClient.getAsnInfo(8319L);
        assertTrue(output.isPresent());
        assertTrue(output.get().getAddress().contains("NETHINKS"));

    }

    @Test
    public void testWhoIsPrefix() throws IOException {

        Optional<RouteInfo> output = BmpWhoIsClient.getRouteInfo("207.248.113.0");
        assertTrue(output.isPresent());
        assertEquals(263127L, output.get().getOriginAs().longValue());
    }

}
