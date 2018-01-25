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

public class FlowSeriesResponseTest {

    @Test
    public void testMarshalJson() throws IOException {
        FlowSeriesResponse response = new FlowSeriesResponse();
        response.setStart(1);
        response.setEnd(100);
        response.setColumns(Lists.newArrayList(new FlowSeriesColumn("SSH", true) , new FlowSeriesColumn("SSH", false)));
        response.setTimestamps(Arrays.asList(1L, 10L, 100L));
        response.setValues(Arrays.asList(
                Lists.newArrayList(1d, 1d, 1d),
                Lists.newArrayList(2d, 2d, 2d)
        ));

        String responseString = JsonTest.marshalToJson(response);
        JsonTest.assertJsonEquals("{\n" +
                "  \"start\" : 1,\n" +
                "  \"end\" : 100,\n" +
                "  \"columns\" : [ {\n" +
                "    \"label\" : \"SSH\",\n" +
                "    \"ingress\" : true\n" +
                "  }, {\n" +
                "    \"label\" : \"SSH\",\n" +
                "    \"ingress\" : false\n" +
                "  } ]," +
                "  \"timestamps\" : [ 1, 10, 100 ],\n" +
                "  \"values\" : [ [ 1.0, 1.0, 1.0 ], [ 2.0, 2.0, 2.0 ] ]" +
                "}", responseString);
    }
}
