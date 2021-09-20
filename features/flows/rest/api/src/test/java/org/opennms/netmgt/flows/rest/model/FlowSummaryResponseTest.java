/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.rest.model;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.opennms.core.test.xml.JsonTest;

import com.google.common.collect.Lists;

public class FlowSummaryResponseTest {

    @Test
    public void testMarshalJson() throws IOException {
        FlowSummaryResponse response = new FlowSummaryResponse();
        response.setStart(1);
        response.setEnd(100);
        response.setHeaders(Lists.newArrayList("Application", "Bytes In", "Bytes Out"));
        response.setRows(Arrays.asList(
                Lists.newArrayList("SSH", 500L, 501L),
                Lists.newArrayList("RDP", 400L, 401L)
        ));

        String responseString = JsonTest.marshalToJson(response);
        JsonTest.assertJsonEquals("{\n" +
                "  \"start\" : 1,\n" +
                "  \"end\" : 100,\n" +
                "  \"headers\" : [ \"Application\", \"Bytes In\", \"Bytes Out\" ],\n" +
                "  \"rows\" : [ [ \"SSH\", 500, 501 ], [ \"RDP\", 400, 401 ] ]\n" +
                "}", responseString);
    }
}
