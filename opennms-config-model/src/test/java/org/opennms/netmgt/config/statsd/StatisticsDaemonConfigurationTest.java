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

package org.opennms.netmgt.config.statsd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class StatisticsDaemonConfigurationTest extends XmlTestNoCastor<StatisticsDaemonConfiguration> {

    public StatisticsDaemonConfigurationTest(StatisticsDaemonConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/statistics-daemon-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<statistics-daemon-configuration xmlns=\"http://xmlns.opennms.org/xsd/config/statsd\">\n" + 
                "   <package name=\"ThroughputReports\">\n" + 
                "      <packageReport name=\"TopN_InOctets\" description=\"Top 20 ifInOctets across all nodes\" schedule=\"0 20 1 * * ?\" retainInterval=\"2592000000\" status=\"off\">\n" + 
                "         <parameter key=\"count\" value=\"20\"/>\n" + 
                "         <parameter key=\"consolidationFunction\" value=\"AVERAGE\"/>\n" + 
                "         <parameter key=\"relativeTime\" value=\"YESTERDAY\"/>\n" + 
                "         <parameter key=\"resourceTypeMatch\" value=\"interfaceSnmp\"/>\n" + 
                "         <parameter key=\"attributeMatch\" value=\"ifInOctets\"/>\n" + 
                "      </packageReport>\n" + 
                "   </package>\n" +
                "   <report name=\"TopN_InOctets\" class-name=\"org.opennms.netmgt.dao.support.TopNAttributeStatisticVisitor\"/>" + 
                "</statistics-daemon-configuration>"
            }
        });
    }

    private static StatisticsDaemonConfiguration getConfig() {
        StatisticsDaemonConfiguration config = new StatisticsDaemonConfiguration();
        
        Package pkg = new Package();
        pkg.setName("ThroughputReports");
        config.addPackage(pkg);

        PackageReport pkgReport = new PackageReport();
        pkgReport.setName("TopN_InOctets");
        pkgReport.setDescription("Top 20 ifInOctets across all nodes");
        pkgReport.setSchedule("0 20 1 * * ?");
        pkgReport.setRetainInterval("2592000000");
        pkgReport.setStatus(PackageReportStatus.off);

        pkgReport.addParameter("count", "20");
        pkgReport.addParameter("consolidationFunction", "AVERAGE");
        pkgReport.addParameter("relativeTime", "YESTERDAY");
        pkgReport.addParameter("resourceTypeMatch", "interfaceSnmp");
        pkgReport.addParameter("attributeMatch", "ifInOctets");
        pkg.addPackageReport(pkgReport);

        Report report = new Report();
        report.setName("TopN_InOctets");
        report.setClassName("org.opennms.netmgt.dao.support.TopNAttributeStatisticVisitor");
        config.addReport(report);

        return config;
    }

}
