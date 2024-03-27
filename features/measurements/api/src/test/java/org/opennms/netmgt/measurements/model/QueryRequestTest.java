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
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

import com.google.common.collect.Lists;

public class QueryRequestTest extends XmlTestNoCastor<QueryRequest> {

    public QueryRequestTest(QueryRequest sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        QueryRequest request = new QueryRequest();
        request.setStep(300);
        request.setStart(1000);
        request.setEnd(2000);
        request.setMaxRows(1);

        Source source = new Source("ping1Micro", "node[1].responseTime[127.0.0.1]", "strafeping", "ping1", true);
        request.setSources(Lists.newArrayList(source));

        Expression expression = new Expression("ping1Millis", "ping1Micro / 1000", false);
        request.setExpressions(Lists.newArrayList(expression));

        FilterDef filter = new FilterDef("name", "key", "val");
        request.setFilters(Lists.newArrayList(filter));

        return Arrays.asList(new Object[][]{{
                request,
                "<query-request step=\"300\" start=\"1000\" end=\"2000\" maxrows=\"1\" relaxed=\"false\">" +
                    "<expression transient=\"false\" label=\"ping1Millis\">ping1Micro / 1000</expression>" +
                    "<filter name=\"name\">" +
                        "<parameter key=\"key\">val</parameter>" +
                    "</filter>" +
                    "<source aggregation=\"AVERAGE\" attribute=\"strafeping\" datasource=\"ping1\" transient=\"true\" label=\"ping1Micro\" resourceId=\"node[1].responseTime[127.0.0.1]\"/>" +
                "</query-request>",
                null
        }});
    }
}
