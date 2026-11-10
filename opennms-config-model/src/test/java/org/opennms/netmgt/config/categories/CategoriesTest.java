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
package org.opennms.netmgt.config.categories;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class CategoriesTest extends XmlTestNoCastor<Catinfo> {

    public CategoriesTest(final Catinfo sampleObject, final Object sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Catinfo ci = new Catinfo("1.3", "Wednesday, February 6, 2002 10:10:00 AM EST", "checkers");

        final CategoryGroup cg = new CategoryGroup("WebConsole");
        cg.setComment("Service Level Availability by Functional Group");
        cg.setCommonRule("IPADDR != '0.0.0.0'");

        final List<Category> cats = new ArrayList<>();
        cats.add(new Category("Overall Service Availability", "This category reflects availability of all services currently being monitored by OpenNMS.", 99.99d, 97d, "IPADDR != '0.0.0.0'"));
        cats.add(new Category("Network Interfaces", "This category reflects the ability to 'ping' managed devices and SNMP agents.  'Ping', using the ICMP protocol, tests a devices network connectivity/availability.", 99.99d, 97d, "(isICMP | isSNMP) & (IPADDR != '0.0.0.0')", "ICMP", "SNMP"));
        cg.setCategories(cats);

        ci.addCategoryGroup(cg);

        return Arrays.asList(new Object[][] {
            {
                ci,
                "<catinfo>\n" + 
                        "    <header>\n" + 
                        "        <rev>1.3</rev>\n" + 
                        "        <created>Wednesday, February 6, 2002 10:10:00 AM EST</created>\n" + 
                        "        <mstation>checkers</mstation>\n" + 
                        "    </header>\n" + 
                        "    <categorygroup>\n" + 
                        "        <name>WebConsole</name>\n" + 
                        "        <comment>Service Level Availability by Functional Group</comment>\n" + 
                        "        <common>\n" + 
                        "            <rule><![CDATA[IPADDR != '0.0.0.0']]></rule>\n" + 
                        "        </common>\n" + 
                        "        <categories>\n" + 
                        "            <category>\n" + 
                        "                <label><![CDATA[Overall Service Availability]]></label>\n" + 
                        "                <comment>This category reflects availability of all services currently being monitored by OpenNMS.</comment>\n" + 
                        "                <normal>99.99</normal>\n" + 
                        "                <warning>97.0</warning>\n" + 
                        "                <rule><![CDATA[IPADDR != '0.0.0.0']]></rule>\n" + 
                        "            </category>\n" + 
                        "            <category>\n" + 
                        "                <label><![CDATA[Network Interfaces]]></label>\n" + 
                        "                <comment>This category reflects the ability to 'ping' managed devices and SNMP agents.  'Ping', using the ICMP protocol, tests a devices network connectivity/availability.</comment>\n" + 
                        "                <normal>99.99</normal>\n" + 
                        "                <warning>97.0</warning>\n" + 
                        "                <service>ICMP</service>\n" + 
                        "                <service>SNMP</service>\n" + 
                        "                <rule><![CDATA[(isICMP | isSNMP) & (IPADDR != '0.0.0.0')]]></rule>\n" + 
                        "            </category>\n" + 
                        "        </categories>\n" + 
                        "    </categorygroup>\n" + 
                        "</catinfo>",
                        "src/main/resources/xsds/categories.xsd",
            }
        });
    }
}
