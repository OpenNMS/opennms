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
package org.opennms.netmgt.config.reportd;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class ReportdConfigurationTest extends XmlTestNoCastor<ReportdConfiguration> {

    public ReportdConfigurationTest(ReportdConfiguration sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/reportd-configuration.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getConfig(),
                "<reportd-configuration \n" + 
                "  storage-location=\"${install.share.dir}/reports/\"\n" + 
                "  persist-reports=\"yes\"\n" + 
                ">\n" + 
                "    <report report-name=\"sample-report\" report-template=\"sample-report.jrxml\" report-engine=\"jdbc\">\n" + 
                "        <cron-schedule>0 0 0 * * ? *</cron-schedule>\n" + 
                "    </report> \n" + 
                "</reportd-configuration>"
            }
        });
    }

    private static ReportdConfiguration getConfig() {
        ReportdConfiguration config = new ReportdConfiguration();
        config.setStorageLocation("${install.share.dir}/reports/");
        config.setPersistReports(true);
        
        Report report = new Report();
        report.setReportName("sample-report");
        report.setReportTemplate("sample-report.jrxml");
        report.setReportEngine("jdbc");
        report.setCronSchedule("0 0 0 * * ? *");
        config.addReport(report);

        return config;
    }
}
