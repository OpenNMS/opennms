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
package org.opennms.netmgt.measurements.model;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.xml.JaxbUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class QueryResponseTest extends XmlTestNoCastor<QueryResponse> {

    public QueryResponseTest(QueryResponse sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        QueryResponse response = new QueryResponse();
        response.setStep(300);
        response.setStart(1000);
        response.setEnd(2000);

        response.setTimestamps(Lists.newArrayList(1L, 2L));

        final Map<String, double[]> columns = Maps.newLinkedHashMap();
        columns.put("x", new double[]{1.0d, 1.1d});
        columns.put("y", new double[]{2.0d, 2.1d});
        response.setColumns(columns);

        // add them out of order, but they should always end up ordered by source label
        final List<QueryResource> resources = new ArrayList<>();
        resources.add(new QueryResource("idY", "parentY", "labelY", "nameY", new QueryNode(2, "test", "nodeY", "nodeY")));
        resources.add(new QueryResource("idX", null, "labelX", "nameX",  new QueryNode(1, "test", "nodeX", "nodeX")));
        response.setMetadata(new QueryMetadata(resources));

        System.err.println(JaxbUtils.marshal(response));
        return Arrays.asList(new Object[][]{{
                response,
                "<query-response step=\"300\" start=\"1000\" end=\"2000\">" +
                    "<columns>" +
                       "<values>1.0</values>" +
                       "<values>1.1</values>" +
                    "</columns>" +
                    "<columns>" +
                       "<values>2.0</values>" +
                       "<values>2.1</values>" +
                    "</columns>" +
                    "<labels>x</labels>" +
                    "<labels>y</labels>" +
                    "<metadata>" +
                       "<resources>" +
                          "<resource id=\"idY\" parent-id=\"parentY\" label=\"labelY\" name=\"nameY\" node-id=\"2\" />" +
                          "<resource id=\"idX\" label=\"labelX\" name=\"nameX\" node-id=\"1\" />" +
                       "</resources>" +
                       "<nodes>" +
                          "<node id=\"1\" foreign-source=\"test\" foreign-id=\"nodeX\" label=\"nodeX\" />" +
                          "<node id=\"2\" foreign-source=\"test\" foreign-id=\"nodeY\" label=\"nodeY\" />" +
                       "</nodes>" +
                    "</metadata>" +
                    "<timestamps>1</timestamps>" +
                    "<timestamps>2</timestamps>" +
                "</query-response>",
                null
        }});
    }
}
