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
import org.opennms.netmgt.flows.classification.IpAddr;

public class IpValueTest {

    private static boolean isInRange(IpValue value, String addr) {
        return value.isInRange(IpAddr.of(addr));
    }

    @Test
    public void verifyRangedValues() {
        final IpValue ipValue = IpValue.of("10.1.1.1-10.1.1.100");
        final IpRange range = IpRange.of("10.1.1.1", "10.1.1.100");
        for (var address : range) {
            assertThat(ipValue.isInRange(address), is(true));
        }
    }

    @Test
    public void verifySingleValue() {
        final IpValue ipValue = IpValue.of("192.168.0.1");
        assertThat(isInRange(ipValue, "192.168.0.0"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.1"), is(true));
        assertThat(isInRange(ipValue, "192.168.0.2"), is(false));
    }

    @Test
    public void verifyMultiValues() {
        final IpValue ipValue = IpValue.of("192.168.0.1, 192.168.0.2, 192.168.0.10");
        assertThat(isInRange(ipValue, "192.168.0.0"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.1"), is(true));
        assertThat(isInRange(ipValue, "192.168.0.2"), is(true));
        assertThat(isInRange(ipValue, "192.168.0.3"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.4"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.5"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.6"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.7"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.8"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.9"), is(false));
        assertThat(isInRange(ipValue, "192.168.0.10"), is(true));
    }

    @Test
    public void verifyParseCIDR() {
        assertThat(IpValue.parseCIDR("192.168.23.0/24"), is(IpRange.of("192.168.23.0", "192.168.23.255")));
        assertThat(IpValue.parseCIDR("192.168.42.23/22"), is(IpRange.of("192.168.40.0", "192.168.43.255")));

        assertThat(IpValue.parseCIDR("192.168.23.42/31"), is(IpRange.of("192.168.23.42", "192.168.23.43")));
        assertThat(IpValue.parseCIDR("192.168.23.42/32"), is(IpRange.of("192.168.23.42", "192.168.23.42")));

        assertThat(IpValue.parseCIDR("fe80::243d:e3ff:fe31:7660/64"), is(IpRange.of("fe80::", "fe80::ffff:ffff:ffff:ffff")));
    }

    @Test
    public void verifyCIDRValue() {
        final IpValue ipValue = IpValue.of("10.0.0.5,192.168.0.0/24");
        for (var ipAddress : IpRange.of("192.168.0.0", "192.168.0.255")) {
            assertThat(ipValue.isInRange(ipAddress), is(true));
        }

        assertThat(isInRange(ipValue, "192.168.1.0"), is(false));
        assertThat(isInRange(ipValue, "192.168.2.0"), is(false));

        assertThat(isInRange(ipValue, "10.0.0.0"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.1"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.2"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.3"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.4"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.5"), is(true));
        assertThat(isInRange(ipValue, "10.0.0.7"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.8"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.9"), is(false));
        assertThat(isInRange(ipValue, "10.0.0.10"), is(false));
    }

    @Test
    public void verifyCIDRValue_2() {
        final IpValue ipValue = IpValue.of("192.168.0.17/16");
        for (var ipAddress : IpRange.of("192.168.0.0", "192.168.255.255")) {
            assertThat(ipValue.isInRange(ipAddress), is(true));
        }
        assertThat(isInRange(ipValue, "192.169.0.0"), is(false));
        assertThat(isInRange(ipValue, "192.0.0.0"), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyCIDRValueNotAllowedInRange() {
        IpValue.of("192.0.0.0/8-192.168.0.0/24");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyWildcard() {
        IpValue.of("*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyInvalidIpAddress() {
        IpValue.of("300.400.500.600");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyInvalidIpAddressRanges() {
        IpValue.of("192.168.0.1-a.b.c.d");
    }

    @Test(expected = IllegalArgumentException.class)
    public void verifyInvalidIpAddressRangeEndIsBefore() {
        IpValue.of("192.168.10.255-192.168.0.1");
    }

    @Test
    public void verifySingleValueIpV6() {
        final IpValue value = IpValue.of("2001:0DB8:0:CD30::1");
        assertThat(value.isInRange(IpAddr.of("2001:0DB8:0:CD30::1")), is(true));
        assertThat(value.isInRange(IpAddr.of("2001:0DB8:0:CD30::2")), is(false));
        assertThat(value.isInRange(IpAddr.of("192.168.0.1")), is(false)); // incompatible, should be false
    }

    @Test
    public void verifyRangedValueIpV6() {
        final IpValue value = IpValue.of("2001:0DB8:0:CD30::1-2001:0DB8:0:CD30::FFFF");
        for (var address : IpRange.of("2001:0DB8:0:CD30::1", "2001:0DB8:0:CD30::FFFF")) {
            assertThat(value.isInRange(address), is(true));
        }
    }

    @Test
    public void verifyCIDRValueIpV6() {
        final IpValue value = IpValue.of("2001:0DB8:0:CD30::1/120");
        for (var ipAddress : IpRange.of("2001:0DB8:0:CD30::0", "2001:0DB8:0:CD30::FF")) {
            assertThat(value.isInRange(ipAddress), is(true));
        }
        assertThat(value.isInRange(IpAddr.of("192.168.0.1")), is(false)); // incompatible, should be false
    }

    @Test
    public void verifyCIDRValueIpV6_2() {
        final IpValue value = IpValue.of("2001:0DB8:0:CD30::1/127");
        for (var ipAddress : IpRange.of("2001:0DB8:0:CD30::0", "2001:0DB8:0:CD30::1")) {
            assertThat(value.isInRange(ipAddress), is(true));
        }
        assertThat(value.isInRange(IpAddr.of("2001:0DB8:0:CD30::2")), is(false));
    }
}
