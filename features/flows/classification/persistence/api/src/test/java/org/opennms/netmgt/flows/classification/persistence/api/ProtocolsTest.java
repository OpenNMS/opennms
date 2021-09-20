/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.persistence.api;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class ProtocolsTest {

    @Test
    public void verifyProtocolExistance() {
        Assert.assertThat(Protocols.getProtocols(), Matchers.hasSize(147));
    }

    @Test
    public void verifyDecimalLookup() {
        Assert.assertEquals("HOPOPT", Protocols.getProtocol(0).getKeyword());
        Assert.assertEquals("ICMP", Protocols.getProtocol(1).getKeyword());
        Assert.assertEquals("TCP", Protocols.getProtocol(6).getKeyword());
        Assert.assertEquals("UDP", Protocols.getProtocol(17).getKeyword());
        Assert.assertEquals("HMP", Protocols.getProtocol(20).getKeyword());
        Assert.assertEquals("Reserved", Protocols.getProtocol(255).getKeyword());
    }

    @Test
    public void verifyCaseInsensitiveLookup() {
        Assert.assertEquals(6, Protocols.getProtocol("tcp").getDecimal());
        Assert.assertEquals(6, Protocols.getProtocol("tCp").getDecimal());
        Assert.assertEquals(6, Protocols.getProtocol("TCP").getDecimal());
    }

    @Test(expected=IllegalArgumentException.class)
    public void verifyEmptyStringLookupFails() {
        Protocols.getProtocol("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void verifyNullStringLookupFails() {
        Protocols.getProtocol(null);
    }

}