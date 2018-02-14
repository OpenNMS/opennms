/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
