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

package org.opennms.netmgt.flows.elastic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.opennms.netmgt.flows.api.ConversationKey;

public class ConversationKeyUtilsTest {

    @Test
    public void canCreateAndParseConversationKey() {
        FlowDocument flowIn = new FlowDocument();
        flowIn.setDirection(Direction.INGRESS);
        flowIn.setLocation("SomeLoc");
        flowIn.setProtocol(1);
        flowIn.setSrcAddr("1.1.1.1");
        flowIn.setSrcPort(9999);
        flowIn.setDstAddr("2.2.2.2");
        flowIn.setDstPort(10000);

        FlowDocument flowOut = new FlowDocument();
        flowOut.setDirection(Direction.EGRESS);
        flowOut.setLocation(flowIn.getLocation());
        flowOut.setProtocol(flowIn.getProtocol());
        flowOut.setSrcAddr(flowIn.getDstAddr());
        flowOut.setSrcPort(flowIn.getDstPort());
        flowOut.setDstAddr(flowIn.getSrcAddr());
        flowOut.setDstPort(flowIn.getSrcPort());

        String inKey = ConversationKeyUtils.getConvoKeyAsJsonString(flowIn);
        String outKey = ConversationKeyUtils.getConvoKeyAsJsonString(flowOut);

        // We should have generated some key, and both should match
        assertThat(inKey, notNullValue());
        assertThat(inKey, equalTo(outKey));

        ConversationKey expectedKey = new ConversationKey(
                flowIn.getLocation(),
                flowIn.getProtocol(),
                flowIn.getSrcAddr(),
                flowIn.getSrcPort(),
                flowIn.getDstAddr(),
                flowIn.getDstPort());
        ConversationKey actualKey = ConversationKeyUtils.fromJsonString(inKey);
        assertThat(actualKey, equalTo(expectedKey));
    }

}
