/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.kscReports;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ReportsListTest extends XmlTestNoCastor<ReportsList> {

    public ReportsListTest(ReportsList sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/ksc-performance-reports.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getReportsList(),
                "<ReportsList>\n" + 
                "    <Report id=\"99\" title=\"New Report Title\" show_timespan_button=\"false\"\n" + 
                "        show_graphtype_button=\"true\" graphs_per_line=\"1\">\n" + 
                "        <Graph title=\"\"\n" + 
                "            resourceId=\"node[NODES:1488813909996].interfaceSnmp[docker0-0242b34c4a03]\"\n" + 
                "            timespan=\"7_day\" graphtype=\"mib2.HCbits\"/>\n" + 
                "    </Report>\n" + 
                "</ReportsList>"
            },
            {
                new ReportsList(),
                "<ReportsList/>"
            }
        });
    }

    private static ReportsList getReportsList() {
        ReportsList reports = new ReportsList();
        Report report = new Report();
        report.setId(99);
        report.setTitle("New Report Title");
        report.setShowTimespanButton(false);
        report.setShowGraphtypeButton(true);
        report.setGraphsPerLine(1);
        reports.addReport(report);
        
        Graph graph = new Graph();
        graph.setTitle("");
        graph.setResourceId("node[NODES:1488813909996].interfaceSnmp[docker0-0242b34c4a03]");
        graph.setTimespan("7_day");
        graph.setGraphtype("mib2.HCbits");
        report.addGraph(graph);

        return reports;
    }
}
