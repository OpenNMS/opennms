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
