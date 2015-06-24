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
