/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.Lists;

public class StressCommandTest {
    @Test
    public void testEventGenerator() {
        new StressCommand() {
            {
                eventNodeId = 42;
                eventIpInterface = "192.168.255.254";

                assertEquals(42L, new EventGenerator()
                        .getNextEvent()
                        .getNodeid()
                        .longValue());

                assertEquals("192.168.255.254", new EventGenerator()
                        .getNextEvent()
                        .getInterface());
            }
        };
    }

    @Test
    public void testJexlGenerator() {
        new StressCommand() {
            {
                eventNodeId = 42;
                eventIpInterface = "192.168.255.254";

                assertEquals(42L, new JexlEventGenerator(Lists.newArrayList("eb.addParam('node', math:floor(math:random() * 100))"))
                        .getNextEvent()
                        .getNodeid()
                        .longValue());

                assertEquals("192.168.255.254", new JexlEventGenerator(Lists.newArrayList("eb.addParam('node', math:floor(math:random() * 100))"))
                        .getNextEvent()
                        .getInterface());
            }
        };
    }
}
