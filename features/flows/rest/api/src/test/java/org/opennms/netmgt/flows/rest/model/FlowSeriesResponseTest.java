/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
