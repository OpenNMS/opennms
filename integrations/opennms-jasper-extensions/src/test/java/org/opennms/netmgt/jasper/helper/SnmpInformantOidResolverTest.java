/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.helper;

import junit.framework.Assert;
import org.junit.Test;

/**
 * <p>SnmpInformantOidResolverTest class.</p>
 *
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version $Id: $
 * @since 1.0-SNAPSHOT
 */
public class SnmpInformantOidResolverTest {

    @Test
    public void testStringToAsciiOid() {
        SnmpInformantOidResolver oidResolver = new SnmpInformantOidResolver();
        Assert.assertEquals("2.67.58", oidResolver.stringToAsciiOid("C:"));
        Assert.assertEquals("38.66.114.111.97.100.99.111.109.32.78.101.116.76.105.110.107.32.91.84.77.93.45.71.105.103.97.98.105.116.45.69.116.104.101.114.110.101.116", oidResolver.stringToAsciiOid("Broadcom NetLink [TM]-Gigabit-Ethernet"));
    }

    @Test
    public void testAsciiOidToString() {
        SnmpInformantOidResolver oidResolver = new SnmpInformantOidResolver();
        Assert.assertEquals("C:", oidResolver.asciiOidToString("2.67.58"));
        Assert.assertEquals("Broadcom NetLink [TM]-Gigabit-Ethernet", oidResolver.asciiOidToString("38.66.114.111.97.100.99.111.109.32.78.101.116.76.105.110.107.32.91.84.77.93.45.71.105.103.97.98.105.116.45.69.116.104.101.114.110.101.116"));
        Assert.assertEquals("Broadcom NetLink (TM)-Gigabit-Ethernet", oidResolver.asciiOidToString("38.66.114.111.97.100.99.111.109.32.78.101.116.76.105.110.107.32.40.84.77.41.45.71.105.103.97.98.105.116.45.69.116.104.101.114.110.101.116"));
    }
}
