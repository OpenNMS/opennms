/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
