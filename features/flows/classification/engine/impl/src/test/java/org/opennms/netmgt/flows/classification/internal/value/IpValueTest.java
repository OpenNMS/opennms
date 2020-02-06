/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal.value;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;

public class IpValueTest {

    @Test
    public void verifyRangedValues() {
        final IpValue ipValue = new IpValue("10.1.1.1-10.1.1.100");
        final IPAddressRange range = new IPAddressRange("10.1.1.1", "10.1.1.100");
        for (IPAddress address : range) {
            assertThat(ipValue.isInRange(address.toUserString()), is(true));
        }
    }

    @Test
    public void verifySingleValue() {
        final IpValue ipValue = new IpValue("192.168.0.1");
        assertThat(ipValue.isInRange("192.168.0.0"), is(false));
        assertThat(ipValue.isInRange("192.168.0.1"), is(true));
        assertThat(ipValue.isInRange("192.168.0.2"), is(false));
    }

    @Test
    public void verifyMultiValues() {
        final IpValue ipValue = new IpValue("192.168.0.1, 192.168.0.2, 192.168.0.10");
        assertThat(ipValue.isInRange("192.168.0.0"), is(false));
        assertThat(ipValue.isInRange("192.168.0.1"), is(true));
        assertThat(ipValue.isInRange("192.168.0.2"), is(true));
        assertThat(ipValue.isInRange("192.168.0.3"), is(false));
        assertThat(ipValue.isInRange("192.168.0.4"), is(false));
        assertThat(ipValue.isInRange("192.168.0.5"), is(false));
        assertThat(ipValue.isInRange("192.168.0.6"), is(false));
        assertThat(ipValue.isInRange("192.168.0.7"), is(false));
        assertThat(ipValue.isInRange("192.168.0.8"), is(false));
        assertThat(ipValue.isInRange("192.168.0.9"), is(false));
        assertThat(ipValue.isInRange("192.168.0.10"), is(true));
    }

    @Test
    public void verifyCIDRValue() {
        final IpValue ipValue = new IpValue("10.0.0.5,192.168.0.0/24");
        for (IPAddress ipAddress : new IPAddressRange("192.168.0.0", "192.168.0.255")) {
            assertThat(ipValue.isInRange(ipAddress.toUserString()), is(true));
        }
        assertThat(ipValue.isInRange("10.0.0.5"), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyCIDRValueNotAllowedInRange() {
        new IpValue("192.0.0.0/8-192.168.0.0/24");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyWildcard() {
        new IpValue("*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyInvalidIpAddress() {
        new IpValue("300.400.500.600");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyInvalidIpAddressRanges() {
        new IpValue("192.168.0.1-a.b.c.d");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyInvalidIpAddressRangeEndIsBefore() {
        new IpValue("192.168.10.255-192.168.0.1");
    }

    @Test
    public void verifySingleValueIpV6() {
        final IpValue value = new IpValue("2001:0DB8:0:CD30::1");
        assertThat(value.isInRange("2001:0DB8:0:CD30::1"), is(true));
        assertThat(value.isInRange("2001:0DB8:0:CD30::2"), is(false));
        assertThat(value.isInRange("192.168.0.1"), is(false)); // incompatible, should be false
    }

    @Test
    public void verifyRangedValueIpV6() {
        final IpValue value = new IpValue("2001:0DB8:0:CD30::1-2001:0DB8:0:CD30::FFFF");
        for (IPAddress address : new IPAddressRange("2001:0DB8:0:CD30::1", "2001:0DB8:0:CD30::FFFF")) {
            assertThat(value.isInRange(address.toUserString()), is(true));
        }
    }
}
