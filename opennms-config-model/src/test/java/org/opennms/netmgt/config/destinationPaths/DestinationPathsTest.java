/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.destinationPaths;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class DestinationPathsTest extends XmlTestNoCastor<DestinationPaths> {

    public DestinationPathsTest(final DestinationPaths sampleObject, final Object sampleXml) {
        super(sampleObject, sampleXml, "src/main/resources/xsds/destinationPaths.xsd");
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final DestinationPaths dp = new DestinationPaths();

        final Header header = new Header("1.2", "Wednesday, February 6, 2002 10:10:00 AM EST", "localhost");
        dp.setHeader(header);

        final Path emailReporting = new Path("Email-Reporting");
        final Target reporting = new Target("Reporting", "javaEmail");
        emailReporting.addTarget(reporting);
        dp.addPath(emailReporting);

        final Path pageManagement = new Path("Page-Management");
        final Target management = new Target("Management", "textPage", "javaPagerEmail", "javaEmail");
        pageManagement.addTarget(management);
        dp.addPath(pageManagement);

        final Path pageNetwork = new Path("Page-Network/Systems/Management");
        final Target networkSystems = new Target("Network/Systems", "textPage", "javaPagerEmail", "javaEmail");
        networkSystems.setInterval("15m");
        pageNetwork.addTarget(networkSystems);
        final Escalate networkSystemsEscalate = new Escalate();
        networkSystemsEscalate.setDelay("15m");
        networkSystemsEscalate.addTarget(management);
        pageNetwork.addEscalate(networkSystemsEscalate);
        dp.addPath(pageNetwork);

        return Arrays.asList(new Object[][] { {
            dp,
            "<destinationPaths>\n" + 
                    "    <header>\n" + 
                    "        <rev>1.2</rev>\n" + 
                    "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                    "        <mstation>localhost</mstation>\n" + 
                    "    </header>\n" + 
                    "    <path name=\"Email-Reporting\">\n" + 
                    "        <target>\n" + 
                    "                <name>Reporting</name>\n" + 
                    "                <command>javaEmail</command>\n" + 
                    "        </target>\n" + 
                    "    </path>\n" + 
                    "    <path name=\"Page-Management\">\n" + 
                    "        <target>\n" + 
                    "                <name>Management</name>\n" + 
                    "                <command>textPage</command>\n" + 
                    "                <command>javaPagerEmail</command>\n" + 
                    "                <command>javaEmail</command>\n" + 
                    "        </target>\n" + 
                    "    </path>\n" + 
                    "    <path name=\"Page-Network/Systems/Management\">\n" + 
                    "        <target interval=\"15m\">\n" + 
                    "                <name>Network/Systems</name>\n" + 
                    "                <command>textPage</command>\n" + 
                    "                <command>javaPagerEmail</command>\n" + 
                    "                <command>javaEmail</command>\n" + 
                    "        </target>\n" + 
                    "        <escalate delay=\"15m\">\n" + 
                    "            <target>\n" + 
                    "                <name>Management</name>\n" + 
                    "                <command>textPage</command>\n" + 
                    "                <command>javaPagerEmail</command>\n" + 
                    "                <command>javaEmail</command>\n" + 
                    "            </target>\n" + 
                    "        </escalate>\n" + 
                    "    </path>\n"
                    + "</destinationPaths>"
        }, });
    }
}
