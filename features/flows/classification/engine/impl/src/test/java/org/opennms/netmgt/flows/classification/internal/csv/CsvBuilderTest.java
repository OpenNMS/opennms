/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.flows.classification.internal.csv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

public class CsvBuilderTest {

    @Test
    public void verifyBuilder() {
        final String expectedCsv = new CsvBuilder()
                .withRule(new RuleBuilder().withName("http2").withProtocol("TCP,UDP").withDstAddress("127.0.0.1"))
                .withRule(new RuleBuilder().withName("google").withDstAddress("8.8.8.8"))
                .withRule(new RuleBuilder().withName("opennms").withDstPort(8980))
                .withRule(new RuleBuilder()
                        .withName("opennms-monitor")
                        .withSrcAddress("10.0.0.1").withSrcPort(10000)
                        .withDstAddress("10.0.0.2").withDstPort(8980))
                .withRule(new RuleBuilder().withName("http").withProtocol("TCP"))
                .withRule(new RuleBuilder()
                        .withName("xxx")
                        .withProtocol("tcp,udp")
                        .withSrcAddress("10.0.0.1").withSrcPort(10000)
                        .withDstAddress("10.0.0.2").withDstPort(8980)
                        .withExporterFilter("some-filter-value")
                )
                .build();

        final StringBuilder builder = new StringBuilder();
        builder.append(CsvServiceImpl.HEADERS_STRING);
        builder.append("http2;TCP,UDP;;;127.0.0.1;;\n");
        builder.append("google;;;;8.8.8.8;;\n");
        builder.append("opennms;;;;;8980;\n");
        builder.append("opennms-monitor;;10.0.0.1;10000;10.0.0.2;8980;\n");
        builder.append("http;TCP;;;;;\n");
        builder.append("xxx;tcp,udp;10.0.0.1;10000;10.0.0.2;8980;some-filter-value");

        final String actualCsv = builder.toString();
        assertEquals(expectedCsv, actualCsv);
    }
}