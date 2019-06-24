/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.generator.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.enlinkd.generator.TopologySettings;


public class BridgeProtocolTest {

    @Test
    public void testAdoptAndVerifySettings() {
        testAmountNodes(11, -1);
        testAmountNodes(11, 0);
        testAmountNodes(11, 10);
        testAmountNodes(11, 11);
        testAmountNodes(21, 12);
        testAmountNodes(21, 20);
        testAmountNodes(21, 21);
        testAmountNodes(31, 22);
    }

    private void testAmountNodes(int expected, int initialSetting) {
        BridgeProtocol protocol = new BridgeProtocol(TopologySettings.builder().build(), null);
        TopologySettings settings = protocol.adoptAndVerifySettings(TopologySettings.builder().amountNodes(initialSetting).build());
        assertEquals(expected, settings.getAmountNodes());
    }
}
