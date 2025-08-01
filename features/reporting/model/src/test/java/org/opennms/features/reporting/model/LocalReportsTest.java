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
package org.opennms.features.reporting.model;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class LocalReportsTest extends XmlTestNoCastor<LocalReports> {

    public LocalReportsTest(final LocalReports sampleObject,
            final String sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/database-reports.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
                {
                    getReports(),
                    "<database-reports>\n" +
                    "    <report id=\"defaultCalendarReport\" display-name=\"default calendar report\"\n" +
                    "        report-service=\"availabilityReportService\" description=\"standard opennms report in calendar format\" />\n" +
                    "    <report id=\"defaultClassicReport\" display-name=\"default classic report\"\n" +
                    "        report-service=\"availabilityReportService\" description=\"standard opennms report in calendar format\" \n" +
                    "        online=\"true\" />\n" +
                    "</database-reports>"
                },
                {
                    new LocalReports(),
                    "<database-reports/>",
                }
            });
    }

    private static LocalReports getReports() {
        final LocalReports reports = new LocalReports();

        Report report = new Report();
        report.setId("defaultCalendarReport");
        report.setDisplayName("default calendar report");
        report.setReportService("availabilityReportService");
        report.setDescription("standard opennms report in calendar format");
        reports.getReportList().add(report);

        report = new Report();
        report.setId("defaultClassicReport");
        report.setDisplayName("default classic report");
        report.setReportService("availabilityReportService");
        report.setDescription("standard opennms report in calendar format");
        report.setOnline(true);
        reports.getReportList().add(report);

        return reports;
    }
}
