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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
