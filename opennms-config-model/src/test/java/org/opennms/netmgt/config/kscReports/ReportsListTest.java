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
