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

package org.opennms.report.configuration;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class RancidListReportTest extends XmlTestNoCastor<RwsRancidlistreport> {

    public RancidListReportTest(RwsRancidlistreport sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/rws-rancidlistreport.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getMinimalReport(),
                    "<rws-rancidlistreport/>"
                },
                {
                    getReport(),
                    "<rws-rancidlistreport>\n" +
                    "   <groupXSet>\n" + 
                    "      <groupXSetName>some-name</groupXSetName>\n" + 
                    "      <nodeSet>\n" + 
                    "         <groupname>some-other-name</groupname>\n" + 
                    "      </nodeSet>" + 
                    "   </groupXSet>\n" + 
                    "</rws-rancidlistreport>"
                }
        });
    }

    private static RwsRancidlistreport getMinimalReport() {
        return new RwsRancidlistreport();
    }

    private static RwsRancidlistreport getReport() {
        RwsRancidlistreport report = new RwsRancidlistreport();
        GroupXSet groupXSet = new GroupXSet();
        groupXSet.setGroupXSetName("some-name");
        NodeSet nodeSet = new NodeSet();
        nodeSet.setGroupname("some-other-name");
        groupXSet.addNodeSet(nodeSet);
        report.addGroupXSet(groupXSet);
        return report;
    }
}
