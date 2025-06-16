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
package org.opennms.netmgt.config.reporting;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.config.reporting.OpennmsReports;
import org.opennms.netmgt.config.reporting.Parameters;
import org.opennms.netmgt.config.reporting.Report;

public class OpennmsReportsTest extends XmlTestNoCastor<OpennmsReports> {

    public OpennmsReportsTest(OpennmsReports sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/reporting.xsd");
    }

    @org.junit.runners.Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getReports(),
                "<opennms-reports xmlns=\"http://xmlns.opennms.org/xsd/config/reporting\">\n" + 
                "   <report id=\"my-report\" type=\"pdf\">\n" + 
                "      <parameters>\n" + 
                "         <string-parm name=\"k\" display-name=\"key\" input-type=\"freeText\">\n" + 
                "            <default>v</default>\n" + 
                "         </string-parm>\n" + 
                "      </parameters>\n" + 
                "      <logo>ulf</logo>\n" + 
                "   </report>\n" + 
                "</opennms-reports>"
            },
            {
                new OpennmsReports(),
                "<opennms-reports/>"
            }
        });
    }

    private static OpennmsReports getReports() {
        OpennmsReports reports = new OpennmsReports();

        Report report = new Report();
        report.setType("pdf");
        report.setId("my-report");
        report.setLogo("ulf");
        reports.addReport(report);
        
        Parameters parms = new Parameters();
        report.setParameters(parms);
        
        StringParm sp = new StringParm();
        sp.setDisplayName("key");
        sp.setInputType("freeText");
        sp.setName("k");
        sp.setDefault("v");
        parms.addStringParm(sp);

        return reports;
    }
}
