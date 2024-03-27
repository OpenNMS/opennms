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
