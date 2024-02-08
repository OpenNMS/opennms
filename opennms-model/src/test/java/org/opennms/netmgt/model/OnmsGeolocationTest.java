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
package org.opennms.netmgt.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class OnmsGeolocationTest {
    private OnmsGeolocation m_geolocation;
    private String m_expectedAddress;

    public OnmsGeolocationTest(final String address1, final String address2, final String city, final String state, final String zip, final String country, final String expectedAddress) {
        m_geolocation = new OnmsGeolocation();
        m_geolocation.setAddress1(address1);
        m_geolocation.setAddress2(address2);
        m_geolocation.setCity(city);
        m_geolocation.setState(state);
        m_geolocation.setZip(zip);
        m_geolocation.setCountry(country);
        m_expectedAddress = expectedAddress;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                "NC",
                "27312",
                "USA",
                "220 Chatham Business Dr, Pittsboro, NC 27312, USA"
            },
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                "NC",
                "27312",
                null,
                "220 Chatham Business Dr, Pittsboro, NC 27312"
            },
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                "NC",
                null,
                null,
                "220 Chatham Business Dr, Pittsboro, NC"
            },
            {
                "220 Chatham Business Dr",
                null,
                "Pittsboro",
                null,
                "27312",
                null,
                "220 Chatham Business Dr, Pittsboro, 27312"
            },
            {
                null,
                null,
                null,
                null,
                "PR7 3JE",
                "UK",
                "PR7 3JE, UK"
            },
            {
                null,
                null,
                null,
                null,
                " ",
                " ",
                null
            },
            {
                null,
                null,
                null,
                null,
                "",
                "",
                null
            }
        });
    }

    @Test
    public void testAddress() {
        assertEquals(m_expectedAddress, m_geolocation.asAddressString());
    }
}
