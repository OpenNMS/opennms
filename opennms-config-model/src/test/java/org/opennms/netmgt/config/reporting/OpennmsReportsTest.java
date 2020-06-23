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
