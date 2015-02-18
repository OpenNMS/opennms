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

package org.opennms.web.rest.measurements.model;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.web.rest.measurements.model.QueryResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

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

        Map<String, Double> values = Maps.newHashMap();
        values.put("x", 1.0d);
        Measurement measurement = new Measurement(0, values);
        response.setMeasurements(Lists.newArrayList(measurement));

        return Arrays.asList(new Object[][]{{
                response,
                "<query-response step=\"300\" start=\"1000\" end=\"2000\">" +
                    "<measurements timestamp=\"0\">" +
                        "<values>" +
                           "<entry>" +
                              "<key>x</key>" +
                              "<value>1.0</value>" +
                           "</entry>" +
                        "</values>" +
                     "</measurements>" +
                "</query-response>",
                null
        }});
    }
}
