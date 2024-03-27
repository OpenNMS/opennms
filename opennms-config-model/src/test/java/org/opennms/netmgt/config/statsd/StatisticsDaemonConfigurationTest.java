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
